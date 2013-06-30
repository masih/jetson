package com.staticiser.jetson;

import com.staticiser.jetson.exception.InternalServerException;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.exception.TransportException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Client implements InvocationHandler {

    public static final AttributeKey<Client> CLIENT_ATTRIBUTE_KEY = new AttributeKey<Client>("client");
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private final InetSocketAddress address;
    private final AtomicInteger next_request_id;
    private final ChannelPool channel_pool;
    private final Method[] dispatch;
    private final Executor executor;
    private final Map<Integer, FutureResponse> future_responses = new HashMap<Integer, FutureResponse>();

    protected Client(final InetSocketAddress address, final Method[] dispatch, final Bootstrap bootstrap, final Executor executor) {

        this.address = address;
        this.dispatch = dispatch;
        this.executor = executor;
        next_request_id = new AtomicInteger();
        channel_pool = new ChannelPool(bootstrap, address);
    }

    public InetSocketAddress getProxiedAddress() {

        return address;
    }

    public Request getPendingRequestById(Integer id) {
        if (future_responses.containsKey(id)) {
            return future_responses.get(id).getRequest();
        }
        //TODO fix readability
        LOGGER.info("MUST NOT BE NULLLL");
        return null;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] params) throws Throwable {

        if (dispatchContains(method)) {

            final Channel channel = borrowChannel();
            channel.attr(CLIENT_ATTRIBUTE_KEY).set(this);
            try {
                final Request request = createRequest(method, params);
                final FutureResponse future_response;
                synchronized (future_responses) {
                    future_response = new FutureResponse(request, channel);
                    future_responses.put(request.getId(), future_response);
                    writeRequest(channel, request);
                }

                try {
                    return future_response.get();
                }
                catch (InterruptedException e) {
                    throw new InternalServerException(e);
                }
                catch (ExecutionException e) {
                    throw e.getCause();
                }
                catch (CancellationException e) {
                    throw new InternalServerException(e);
                }
            }
            finally {
                returnChannel(channel);
            }
        }
        else {
            // FIXME check if method is hashcode, equals or tostring
            return method.invoke(this, params);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("DefaultInvocationHandler{").append("address=").append(address).append('}').toString();
    }

    public void handle(final ChannelHandlerContext context, final Response response) {
        executor.execute(new Runnable() {

            @Override
            public void run() {

                synchronized (future_responses) {
                    final Integer id = response.getId();
                    if (future_responses.containsKey(id)) {
                        future_responses.remove(id).setResponse(response);
                    }
                    else {
                        LOGGER.info("received unknown response: {},from {}", response, context.channel());
                    }
                }
            }
        });

    }

    public void notifyChannelInactivation(final Channel channel) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                synchronized (future_responses) {
                    for (Map.Entry<Integer, FutureResponse> entry : future_responses.entrySet()) {
                        final FutureResponse future_response = entry.getValue();
                        if (future_response.getChannel().equals(channel)) {
                            future_response.setException(new TransportException("connection closed"));
                        }
                    }
                }
            }
        });

    }

    protected boolean dispatchContains(Method target) {
        for (Method method : dispatch) {
            if (method.equals(target)) {
                return true;
            }
        }
        return false;
    }

    protected Request createRequest(final Method method, final Object[] args) {

        final Request request = new Request();
        request.setId(generateRequestId());
        request.setMethod(method);
        request.setArguments(args);
        return request;
    }

    private Integer generateRequestId() {

        return next_request_id.getAndIncrement();
    }

    private Channel borrowChannel() throws InternalServerException, TransportException {

        try {
            return channel_pool.borrowObject();
        }
        catch (final InterruptedException e) {
            throw new InternalServerException(e);
        }
        catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) { throw new TransportException(cause); }
            throw new InternalServerException(e);
        }
        catch (final Exception e) {
            throw new InternalServerException(e);
        }
    }

    private void returnChannel(final Channel channel) throws InternalServerException {

        try {
            channel_pool.returnObject(channel);
        }
        catch (final Exception e) {
            throw new InternalServerException(e);
        }
    }

    private void writeRequest(final Channel channel, final Request request) throws RPCException {

        try {
            channel.write(request).sync();
        }
        catch (final Exception e) {
            if (e instanceof RPCException) { throw RPCException.class.cast(e); }
            if (e instanceof IOException) { throw new TransportException(e); }
            final Throwable cause = e.getCause();
            if (cause != null) {
                if (cause instanceof RPCException) { throw RPCException.class.cast(cause); }
                if (cause instanceof IOException) { throw new TransportException(cause); }
            }
            throw new InternalServerException(e);
        }
    }
}
