/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mashti.jetson;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import org.mashti.jetson.util.NamedThreadFactory;
import org.mashti.jetson.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating RPC clients. The created clients are cached for future reuse. This class is thread-safe.
 *
 * @param <Service> the type of the remote service
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ClientFactory<Service> {

    private static final int DEFAULT_CONNECTION_TIMEOUT_IN_MILLIS = 5000;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);
    protected final Method[] dispatch;
    private final Bootstrap bootstrap;
    private final ClassLoader class_loader;
    private final Class<?>[] interfaces;
    private final ConcurrentHashMap<InetSocketAddress, Service> cached_proxy_map = new ConcurrentHashMap<InetSocketAddress, Service>();
    protected final ChannelFuturePool channel_pool;

    public ClientFactory(final Class<Service> service_interface, final ClientChannelInitializer handler) {

        this(service_interface, createDefaultBootstrap(handler));
    }

    public ClientFactory(final Class<Service> service_interface, final Bootstrap bootstrap) {

        this(service_interface, ReflectionUtil.checkAndSort(service_interface.getMethods()), bootstrap);

    }

    public ClientFactory(final Class<Service> service_interface, Method[] dispatch, final Bootstrap bootstrap) {

        this.dispatch = dispatch;
        class_loader = ClassLoader.getSystemClassLoader();
        interfaces = new Class<?>[] {service_interface};
        this.bootstrap = bootstrap;
        channel_pool = constructChannelPool(bootstrap);
    }

    protected ChannelFuturePool constructChannelPool(final Bootstrap bootstrap) {

        return new ChannelFuturePool(bootstrap);
    }

    /**
     * Gets a proxy to the remote service.
     *
     * @param address the address
     * @return the service
     */
    public Service get(final InetSocketAddress address) {

        if (cached_proxy_map.containsKey(address)) { return cached_proxy_map.get(address); }
        final Client handler = createClient(address);
        final Service new_proxy = createProxy(handler);
        final Service existing_proxy = cached_proxy_map.putIfAbsent(address, new_proxy);
        return existing_proxy != null ? existing_proxy : new_proxy;
    }

    /** Shuts down all the {@link EventLoopGroup threads} that are used by any client constructed using this factory. */
    public void shutdown() {

        LOGGER.debug("shutting down client factory for service {}", interfaces[0]);
        bootstrap.group().shutdownGracefully();
    }

    protected Client createClient(final InetSocketAddress address) {

        return new Client(address, dispatch, channel_pool);
    }

    @SuppressWarnings("unchecked")
    Service createProxy(final Client handler) {

        return (Service) Proxy.newProxyInstance(class_loader, interfaces, handler);
    }

    private static Bootstrap createDefaultBootstrap(final ChannelHandler handler) {

        final Bootstrap bootstrap = new Bootstrap();
        final NioEventLoopGroup client_event_loop = new NioEventLoopGroup(0, new NamedThreadFactory("client_event_loop_"));
        bootstrap.group(client_event_loop);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECTION_TIMEOUT_IN_MILLIS);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.handler(handler);
        return bootstrap;
    }

    @Override
    public String toString() {

        return getClass().getSimpleName() + ':' + interfaces[0].getSimpleName();
    }
}
