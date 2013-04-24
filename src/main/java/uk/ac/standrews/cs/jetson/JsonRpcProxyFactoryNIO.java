package uk.ac.standrews.cs.jetson;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseError;
import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseResult;
import uk.ac.standrews.cs.jetson.exception.InternalException;
import uk.ac.standrews.cs.jetson.exception.InvocationException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcExceptions;
import uk.ac.standrews.cs.jetson.exception.TransportException;
import uk.ac.standrews.cs.jetson.exception.UnexpectedException;
import uk.ac.standrews.cs.jetson.pool.ChannelPool;
import uk.ac.standrews.cs.jetson.util.ReflectionUtil;

import com.fasterxml.jackson.core.JsonFactory;

public class JsonRpcProxyFactoryNIO {

    private final Map<Method, String> dispatch;
    private final AtomicLong next_request_id;
    private final Bootstrap bootstrap;
    private final ClassLoader class_loader;
    private final Class<?>[] interfaces;

    private static final Map<InetSocketAddress, Object> PROXY_MAP = new HashMap<InetSocketAddress, Object>();

    private static final EventLoopGroup group = new NioEventLoopGroup(10);

    /**
     * Instantiates a new JSON RPC proxy factory. The {@link ClassLoader#getSystemClassLoader() system class loader} used for constructing new proxy instances.
     *
     * @param service_interface the interface presenting the remote service
     * @param json_factory the provider of JSON serialiser and deserialisers
     */
    public JsonRpcProxyFactoryNIO(final Class<?> service_interface, final JsonFactory json_factory) {

        this(service_interface, json_factory, ClassLoader.getSystemClassLoader());
    }

    public JsonRpcProxyFactoryNIO(final Class<?> service_interface, final JsonFactory json_factory, final ClassLoader class_loader) {

        dispatch = ReflectionUtil.mapMethodsToNames(service_interface);
        next_request_id = new AtomicLong();
        this.class_loader = class_loader;
        this.interfaces = new Class<?>[]{service_interface};

        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new JsonRpcProxyInitializer(json_factory));

    }

    public synchronized <T> T get(final InetSocketAddress address) {

        if (PROXY_MAP.containsKey(address)) { return (T) PROXY_MAP.get(address); }

        final JsonRpcInvocationHandler handler = createJsonRpcInvocationHandler(address);
        final Object proxy = createProxy(handler);
        PROXY_MAP.put(address, proxy);
        return (T) proxy;
    }

    @SuppressWarnings("unchecked")
    protected Object createProxy(final JsonRpcInvocationHandler handler) {

        return Proxy.newProxyInstance(class_loader, interfaces, handler);
    }

    protected JsonRpcInvocationHandler createJsonRpcInvocationHandler(final InetSocketAddress address) {

        return new JsonRpcInvocationHandler(address);
    }

    private class JsonRpcInvocationHandler implements InvocationHandler {

        private final InetSocketAddress address;
        private final ChannelPool channel_pool;

        protected JsonRpcInvocationHandler(final InetSocketAddress address) {

            this.address = address;
            channel_pool = new ChannelPool(bootstrap, address);

        }

        public InetSocketAddress getProxiedAddress() {

            return address;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] params) throws Throwable {

            final Channel channel;
            try {
                channel = channel_pool.borrowObject();
            }
            catch (final InterruptedException e) {
                throw new InternalException(e);
            }
            catch (final ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof IOException) { throw new TransportException(cause); }
                throw new InternalException(e);
            }
            catch (final Exception e) {
                throw new InternalException(e);
            }
            try {
                final JsonRpcRequest request = createJsonRpcRequest(method, params);
                final ChannelFuture lastWriteFuture = channel.write(request);
                try {
                    lastWriteFuture.sync();
                    channel.attr(JsonRpcRequestEncoder.RESPONSE_LATCH).get().await();
                }
                catch (final InterruptedException e) {
                    throw new InternalException(e);
                }
                final JsonRpcResponse response = channel.attr(JsonRpcClientHandler.RESPONSE_ATTRIBUTE).get();
                if (isResponseError(response)) {
                    final JsonRpcResponseError response_error = toJsonRpcResponseError(response);
                    final JsonRpcException exception = JsonRpcExceptions.fromJsonRpcError(response_error.getError());
                    throw !isInvocationException(exception) ? exception : reconstructException(method.getExceptionTypes(), castToInvovationException(exception));
                }

                return JsonRpcResponseResult.class.cast(response).getResult();
            }
            finally {
                try {
                    channel_pool.returnObject(channel);
                }
                catch (final Exception e) {
                    throw new InternalException(e);
                }
            }
        }
    }

    private InvocationException castToInvovationException(final JsonRpcException exception) {

        assert InvocationException.class.isInstance(exception);
        return InvocationException.class.cast(exception);
    }

    private Throwable reconstructException(final Class<?>[] expected_types, final InvocationException exception) {

        final Object exception_data = exception.getData();
        final Throwable throwable;
        if (Throwable.class.isInstance(exception_data)) {
            final Throwable cause = Throwable.class.cast(exception_data);
            throwable = reconstructExceptionFromExpectedTypes(expected_types, cause);
        }
        else {
            throwable = new UnexpectedException("cannot determine the cause of remote invocation exception : " + exception_data);
        }

        return throwable;
    }

    private Throwable reconstructExceptionFromExpectedTypes(final Class<?>[] expected_types, final Throwable cause) {

        return !isExpected(expected_types, cause) ? new UnexpectedException(cause) : cause;
    }

    private boolean isExpected(final Class<?>[] expected_types, final Throwable cause) {

        return ReflectionUtil.containsAnyAssignableFrom(cause.getClass(), expected_types);
    }

    private boolean isInvocationException(final JsonRpcException exception) {

        return InvocationException.class.isInstance(exception);
    }

    private JsonRpcResponseError toJsonRpcResponseError(final JsonRpcResponse response) {

        return JsonRpcResponseError.class.cast(response);
    }

    private boolean isResponseError(final JsonRpcResponse response) {

        return JsonRpcResponseError.class.isInstance(response);
    }

    private JsonRpcRequest createJsonRpcRequest(final Method method, final Object[] params) {

        final Long request_id = generateRequestId();
        final String json_rpc_method_name = getJsonRpcMethodName(method);
        return new JsonRpcRequest(request_id, method, json_rpc_method_name, params);
    }

    private Long generateRequestId() {

        return next_request_id.getAndIncrement();
    }

    private String getJsonRpcMethodName(final Method method) {

        return dispatch.get(method);
    }
}
