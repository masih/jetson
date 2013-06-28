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

import com.staticiser.jetson.util.NamingThreadFactory;
import com.staticiser.jetson.util.ReflectionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating JSON RPC clients. The created clients are cached for future reuse. This class is thread-safe.
 *
 * @param <Service> the type of the remote service
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ClientFactory<Service> {

    private static final int DEFAULT_CONNECTION_TIMEOUT_IN_MILLIS = 5000;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);
    private static final int THREAD_POOL_SIZE = 8;
    private static final Executor EXECUTOR = Executors.newCachedThreadPool(new NamingThreadFactory("client_response_handler_"));
    private final Bootstrap bootstrap;
    private final ClassLoader class_loader;
    private final Class<?>[] interfaces;
    private final Map<InetSocketAddress, Service> cached_proxy_map = new HashMap<InetSocketAddress, Service>();
    private final Method[] dispatch;

    protected ClientFactory(final Class<Service> service_interface, final ChannelHandler handler) {

        dispatch = ReflectionUtil.sort(service_interface.getMethods());
        this.class_loader = ClassLoader.getSystemClassLoader();
        this.interfaces = new Class<?>[] {service_interface};
        bootstrap = new Bootstrap();
        configure(handler);
    }

    /**
     * Gets a proxy to the remote service.
     *
     * @param address the address
     * @return the service
     */
    public synchronized Service get(final InetSocketAddress address) {

        if (cached_proxy_map.containsKey(address)) { return cached_proxy_map.get(address); }
        final Client handler = createInvocationHandler(address);
        final Service proxy = createProxy(handler);
        cached_proxy_map.put(address, proxy);
        return proxy;
    }

    /** Shuts down all the {@link EventLoopGroup threads} that are used by any client constructed using this factory. */
    public void shutdown() {
        LOGGER.debug("shutting down client factory for service {}", interfaces[0]);
        bootstrap.group().shutdownGracefully();
    }

    protected void configure(final ChannelHandler handler) {

        bootstrap.group(new NioEventLoopGroup(THREAD_POOL_SIZE, new NamingThreadFactory("client_event_loop_")));
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECTION_TIMEOUT_IN_MILLIS);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.handler(handler);
    }

    protected Client createInvocationHandler(final InetSocketAddress address) {

        return new Client(address, dispatch, bootstrap, EXECUTOR);
    }

    @SuppressWarnings("unchecked")
    protected Service createProxy(final Client handler) {

        return (Service) Proxy.newProxyInstance(class_loader, interfaces, handler);
    }

}
