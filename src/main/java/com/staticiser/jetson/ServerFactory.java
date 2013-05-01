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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonFactory;
import com.staticiser.jetson.util.NamingThreadFactory;
import com.staticiser.jetson.util.ReflectionUtil;

/**
 * A factory for creating Servers.
 *
 * @param <Service> the type of the service
 */
public class ServerFactory<Service> {

    private final ServerBootstrap server_bootstrap;
    private final ThreadPoolExecutor request_executor;

    /**
     * Instantiates a new server factory.
     *
     * @param service_type the type of the service
     * @param json_factory the JSON factory
     */
    public ServerFactory(final Class<Service> service_type, final JsonFactory json_factory) {

        request_executor = new ThreadPoolExecutor(0, 200, 5, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(true));
        server_bootstrap = createServerBootstrap(service_type, json_factory);
    }

    /**
     * Creates a new Server object.
     *
     * @param service the service implementation
     * @return the server
     */
    public Server createServer(final Service service) {

        return new Server(server_bootstrap, service);
    }

    protected ServerBootstrap createServerBootstrap(final Class<?> service_type, final JsonFactory json_factory) {

        return createDefaultServerBootstrap(service_type, json_factory, request_executor);
    }

    static ServerBootstrap createDefaultServerBootstrap(final Class<?> service_type, final JsonFactory json_factory) {

        return createDefaultServerBootstrap(service_type, json_factory, Executors.newCachedThreadPool());
    }

    static ServerBootstrap createDefaultServerBootstrap(final Class<?> service_type, final JsonFactory json_factory, final ExecutorService request_executor) {

        System.out.println("sss");
        final Map<String, Method> dispatch = ReflectionUtil.mapNamesToMethods(service_type);
        final NioEventLoopGroup parent_event_loop = new NioEventLoopGroup(8, new NamingThreadFactory("server_parent_event_loop_"));
        final NioEventLoopGroup child_event_loop = new NioEventLoopGroup(50, new NamingThreadFactory("server_child_event_loop_"));
        final ServerBootstrap server_bootstrap = new ServerBootstrap();
        server_bootstrap.group(parent_event_loop, child_event_loop);
        server_bootstrap.channel(NioServerSocketChannel.class);
        server_bootstrap.option(ChannelOption.TCP_NODELAY, true);
        server_bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        final ServerChannelInitializer server_channel_initialiser = new ServerChannelInitializer(json_factory, dispatch, request_executor);
        server_bootstrap.childHandler(server_channel_initialiser);
        return server_bootstrap;
    }

    /**
     * Shuts down the {@link ServerBootstrap server bootstrap} and the {@link EventLoop}s used by any server that is created using this factory.
     * After this method is called any server that is created using this factory will become unresponsive.
     * 
     * @see ServerBootstrap#shutdown()
     */
    public void shutdown() {

        request_executor.shutdownNow();
        server_bootstrap.shutdown();

    }
}
