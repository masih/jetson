/**
 * This file is part of jetson.
 *
 * jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jetson.  If not, see <http://www.gnu.org/licenses/>.
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
    protected final ChannelPool channel_pool;

    protected ClientFactory(final Class<Service> service_interface, final ClientChannelInitializer handler) {

        this(service_interface, createDefaultBootstrap(handler));
    }

    protected ClientFactory(final Class<Service> service_interface, final Bootstrap bootstrap) {

        dispatch = ReflectionUtil.sort(service_interface.getMethods());
        class_loader = ClassLoader.getSystemClassLoader();
        interfaces = new Class<?>[] {service_interface};
        this.bootstrap = bootstrap;
        channel_pool = constructChannelPool(bootstrap);
    }

    protected ChannelPool constructChannelPool(final Bootstrap bootstrap) {

        return new ChannelPool(bootstrap);
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
        final NioEventLoopGroup client_event_loop = new NioEventLoopGroup(100, new NamedThreadFactory("client_event_loop_"));
        bootstrap.group(client_event_loop);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECTION_TIMEOUT_IN_MILLIS);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.handler(handler);
        return bootstrap;
    }
}
