package com.staticiser.jetson;

import com.staticiser.jetson.exception.InternalServerException;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.exception.TransportException;
import io.netty.channel.Channel;
import io.netty.channel.MessageList;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Client implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private final InetSocketAddress address;
    private final ChannelPool channel_pool;
    private final Method[] dispatch;

    protected Client(final InetSocketAddress address, final Method[] dispatch, final ChannelPool channel_pool) {

        this.address = address;
        this.dispatch = dispatch;
        this.channel_pool = channel_pool;
    }

    public InetSocketAddress getAddress() {

        return address;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] params) throws Throwable {

        if (dispatchContains(method)) {
            final FutureResponse future_response = writeRequest(method, params);

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
        else {
            LOGGER.debug("method {} was not found in dispatch; executing method on proxy object", method);
            return method.invoke(this, params);
        }
    }

    @Override
    public String toString() {

        return new StringBuilder("Client{").append("address=").append(address).append('}').toString();
    }

    public FutureResponse newFutureResponse(final Method method, final Object[] params) {

        final FutureResponse response = new FutureResponse();
        response.setMethod(method);
        response.setArguments(params);
        return response;
    }

    protected void addExtras(final MessageList<FutureResponse> messages) throws RPCException {

    }

    FutureResponse writeRequest(final Method method, final Object[] params) throws RPCException {

        final FutureResponse future_response = newFutureResponse(method, params);
        return writeRequest(future_response);
    }

    protected FutureResponse writeRequest(FutureResponse future_response) throws RPCException {

        final MessageList<FutureResponse> messages = MessageList.newInstance(future_response);
        addExtras(messages);
        writeMessageList(messages);
        return future_response;
    }

    void writeMessageList(final MessageList<FutureResponse> messages) throws RPCException {

        try {
            if (messages.size() > 0) {
                final Channel channel = borrowChannel();
                try {
                    channel.write(messages);
                }
                finally {
                    returnChannel(channel);
                }
            }
        }
        finally {
            messages.releaseAllAndRecycle();
        }
    }

    boolean dispatchContains(final Method target) {

        for (final Method method : dispatch) {
            if (method.equals(target)) { return true; }
        }
        return false;
    }

    private synchronized Channel borrowChannel() throws InternalServerException, TransportException {

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
        catch (final NoSuchElementException e) {
            throw new TransportException(e);
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
}
