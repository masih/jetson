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
package uk.ac.standrews.cs.jetson;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import uk.ac.standrews.cs.jetson.util.NamingThreadFactory;
import uk.ac.standrews.cs.jetson.util.ReflectionUtil;

import com.fasterxml.jackson.core.JsonFactory;

public class ServerFactory<Service> {

    private final JsonFactory json_factory;
    private final ServerBootstrap server_bootstrap;
    private final Map<String, Method> dispatch;

    private static final NioEventLoopGroup GLOBAL_SERVER_THREADS_GROUP = new NioEventLoopGroup(8, new NamingThreadFactory("server"));
    private static final NioEventLoopGroup GLOBAL_SERVER_WORKER_THREADS_GROUP = new NioEventLoopGroup(200, new NamingThreadFactory("server_worker"));
    private static final ThreadPoolExecutor REQUEST_EXECUTOR = new ThreadPoolExecutor(0, 1000, 5, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(true));

    public ServerFactory(final Class<Service> service_type, final JsonFactory json_factory) {

        dispatch = ReflectionUtil.mapNamesToMethods(service_type);
        this.json_factory = json_factory;
        server_bootstrap = createServerBootstrap();
    }

    protected ServerBootstrap createServerBootstrap() {

        final ServerBootstrap server_bootstrap = new ServerBootstrap();
        server_bootstrap.group(GLOBAL_SERVER_THREADS_GROUP, GLOBAL_SERVER_WORKER_THREADS_GROUP);
        server_bootstrap.channel(NioServerSocketChannel.class);
        server_bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        server_bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        server_bootstrap.childHandler(new ServerChannelInitializer(json_factory, dispatch, REQUEST_EXECUTOR));
        return server_bootstrap;
    }

    public Server createJsonRpcServer(final Service service) {

        return new Server(server_bootstrap, service);
    }
}
