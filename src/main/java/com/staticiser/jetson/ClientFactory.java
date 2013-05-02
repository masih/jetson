/*
 * Copyright 2013 Masih Hajiarabderkani
 * 
 * This file is part of Jetson.
 * 
 * Jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jetson.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.staticiser.jetson;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.staticiser.jetson.exception.InternalException;
import com.staticiser.jetson.exception.InvocationException;
import com.staticiser.jetson.exception.JsonRpcException;
import com.staticiser.jetson.exception.JsonRpcExceptions;
import com.staticiser.jetson.exception.TransportException;
import com.staticiser.jetson.exception.UnexpectedException;
import com.staticiser.jetson.util.NamingThreadFactory;
import com.staticiser.jetson.util.ReflectionUtil;

/**
 * A factory for creating JSON RPC clients. The created clients are cached for future reuse. This class is thread-safe.
 *
 * @param <Service> the type of the remote service
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ClientFactory<Service> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);
    private final Map<Method, String> dispatch;
    private final Bootstrap bootstrap;
    private final ClassLoader class_loader;
    private final Class<?>[] interfaces;

    private final Map<InetSocketAddress, Service> address_to_proxy_map = new HashMap<InetSocketAddress, Service>();
    private static final EventLoopGroup GLOBAL_CLIENT_WORKER_GROUP = new NioEventLoopGroup(8, new NamingThreadFactory("client_event_loop_"));

    /**
     * Instantiates a new JSON RPC client factory. The {@link ClassLoader#getSystemClassLoader() system class loader} used for constructing new proxy instances.
     *
     * @param service_interface the interface presenting the remote service
     * @param json_factory the provider of JSON serialiser and deserialiser
     */
    public ClientFactory(final Class<?> service_interface, final JsonFactory json_factory) {

        dispatch = ReflectionUtil.mapMethodsToNames(service_interface);

        this.class_loader = ClassLoader.getSystemClassLoader();
        this.interfaces = new Class<?>[]{service_interface};
        bootstrap = new Bootstrap();
        configure(json_factory);
    }

    /**
     * Gets a proxy to the remote service.
     *
     * @param address the address
     * @return the service
     */
    public synchronized Service get(final InetSocketAddress address) {

        if (address_to_proxy_map.containsKey(address)) { return address_to_proxy_map.get(address); }
        final JsonRpcInvocationHandler handler = createJsonRpcInvocationHandler(address);
        final Service proxy = createProxy(handler);
        address_to_proxy_map.put(address, proxy);
        return proxy;
    }

    protected void configure(final JsonFactory json_factory) {

        bootstrap.group(GLOBAL_CLIENT_WORKER_GROUP);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(createClientChannelInitializer(json_factory));
    }

    protected ClientChannelInitializer createClientChannelInitializer(final JsonFactory json_factory) {

        return new ClientChannelInitializer(json_factory);
    }

    @SuppressWarnings("unchecked")
    protected Service createProxy(final JsonRpcInvocationHandler handler) {

        return (Service) Proxy.newProxyInstance(class_loader, interfaces, handler);
    }

    protected JsonRpcInvocationHandler createJsonRpcInvocationHandler(final InetSocketAddress address) {

        return new JsonRpcInvocationHandler(address);
    }

    protected String getJsonRpcMethodName(final Method method) {

        return dispatch.get(method);
    }

    protected class JsonRpcInvocationHandler implements InvocationHandler {

        private final InetSocketAddress address;
        private final AtomicLong next_request_id;
        private final ChannelPool channel_pool;

        protected JsonRpcInvocationHandler(final InetSocketAddress address) {

            this.address = address;
            next_request_id = new AtomicLong();
            channel_pool = new ChannelPool(bootstrap, address);

        }

        private Long generateRequestId() {

            return next_request_id.getAndIncrement();
        }

        public InetSocketAddress getProxiedAddress() {

            return address;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] params) throws Throwable {

            final Channel channel = borrowChannel();
            try {
                final Request request = createRequest(channel, method, params);
                writeRequest(channel, request);
                final Response response = readResponse(channel);
                if (response.isError()) {
                    final JsonRpcException exception = JsonRpcExceptions.fromJsonRpcError(response.getError());
                    throw !isInvocationException(exception) ? exception : reconstructException(method.getExceptionTypes(), castToInvovationException(exception));
                }
                return response.getResult();
            }
            finally {
                returnChannel(channel);
            }
        }

        private Response readResponse(final Channel channel) throws InternalException {

            try {
                channel.attr(ResponseHandler.RESPONSE_BARRIER_ATTRIBUTE).get().acquire();
            }
            catch (final InterruptedException e) {
                throw new InternalException(e);
            }

            return channel.attr(ResponseHandler.RESPONSE_ATTRIBUTE).get();
        }

        private void writeRequest(final Channel channel, final Request request) throws JsonRpcException {

            try {
                channel.write(request).sync();
            }
            catch (final Exception e) {
                if (e instanceof JsonRpcException) { throw JsonRpcException.class.cast(e); }
                final Throwable cause = e.getCause();
                if (cause != null && cause instanceof JsonRpcException) { throw JsonRpcException.class.cast(cause); }
                throw new InternalException(e);
            }
        }

        private void returnChannel(final Channel channel) throws InternalException {

            try {
                channel_pool.returnObject(channel);
            }
            catch (final Exception e) {
                throw new InternalException(e);
            }
        }

        private Channel borrowChannel() throws InternalException, TransportException {

            try {
                return channel_pool.borrowObject();
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

        private Request createRequest(final Channel channel, final Method method, final Object[] params) {

            final Request request = channel.attr(ResponseHandler.REQUEST_ATTRIBUTE).get();
            request.setId(generateRequestId());
            request.setMethodName(getJsonRpcMethodName(method));
            request.setMethod(method);
            request.setParams(params);
            return request;
        }
    }
}
