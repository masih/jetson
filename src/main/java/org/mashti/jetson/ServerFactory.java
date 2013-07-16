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
package org.mashti.jetson;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.mashti.jetson.util.NamingThreadFactory;

/**
 * A factory for creating Servers.
 *
 * @param <Service> the type of the service
 */
public class ServerFactory<Service> {

    private final ListeningExecutorService request_executor;
    private final ServerBootstrap server_bootstrap;

    /**
     * Instantiates a new server factory.
     *
     */
    protected ServerFactory(final ServerChannelInitializer handler) {

        this(Executors.newCachedThreadPool(new NamingThreadFactory("server_factory_", true)), handler);
        //        this(service_type, Executors.newFixedThreadPool(500, new NamingThreadFactory(service_type.getSimpleName() + "_server_factory_")), handler);
    }

    /**
     * Instantiates a new server factory.
     *
     * @param request_executor the executor that is used to process requests by all the servers that are instantiated using this factory
     */
    private ServerFactory(final ExecutorService request_executor, final ServerChannelInitializer handler) {

        this.request_executor = MoreExecutors.listeningDecorator(request_executor);
        server_bootstrap = createServerBootstrap(handler);
    }

    /**
     * Creates a new Server object.
     *
     * @param service the service implementation
     * @return the server
     */
    public Server createServer(final Service service) {

        return new Server(server_bootstrap, service, request_executor);
    }

    /**
     * Shuts down the {@link ServerBootstrap server bootstrap} and the {@link EventLoop}s used by any server that is created using this factory.
     * After this method is called any server that is created using this factory will become unresponsive.
     *
     * @see EventLoop#shutdownGracefully()
     */
    public void shutdown() {

        request_executor.shutdownNow();
        server_bootstrap.group().shutdownGracefully();
    }

    ServerBootstrap createServerBootstrap(final ServerChannelInitializer handler) {

        return createDefaultServerBootstrap(handler);
    }

    private static ServerBootstrap createDefaultServerBootstrap(final ServerChannelInitializer handler) {

        final NioEventLoopGroup parent_event_loop = new NioEventLoopGroup(0, new NamingThreadFactory("server_parent_event_loop_"));
        final NioEventLoopGroup child_event_loop = new NioEventLoopGroup(0, new NamingThreadFactory("server_child_event_loop_"));
        final ServerBootstrap server_bootstrap = new ServerBootstrap();
        server_bootstrap.group(parent_event_loop, child_event_loop);
        server_bootstrap.channel(NioServerSocketChannel.class);
        server_bootstrap.option(ChannelOption.TCP_NODELAY, true);
        server_bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        server_bootstrap.childHandler(handler);
        return server_bootstrap;
    }
}
