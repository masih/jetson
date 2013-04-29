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

import com.fasterxml.jackson.core.JsonFactory;

public class JsonRpcServerFactory {

    private final JsonFactory json_factory;
    private final Class service_interface;
    private final Object service;
    private final ServerBootstrap server_bootstrap;

    public <T> JsonRpcServerFactory(final Class<T> service_interface, final T service, final JsonFactory json_factory) {

        this.service_interface = service_interface;
        this.service = service;
        this.json_factory = json_factory;
        server_bootstrap = createServerBootstrap();
    }

    ServerBootstrap createServerBootstrap() {

        //        final NioEventLoopGroup GLOBAL_SERVER_THREADS_GROUP = new NioEventLoopGroup();
        //        final NioEventLoopGroup GLOBAL_SERVER_WORKER_THREADS_GROUP = new NioEventLoopGroup(200);
        //        final ServerBootstrap bootstrap = new ServerBootstrap();
        //        bootstrap.group(GLOBAL_SERVER_THREADS_GROUP, GLOBAL_SERVER_WORKER_THREADS_GROUP).channel(NioServerSocketChannel.class).childHandler(new JsonRpcServerInitializer(channel_group, service, json_factory, dispatch));
        //        return bootstrap;
        return null;
    }

    JsonRpcServer createJsonRpcServer() {

        return new JsonRpcServer(service_interface, service, json_factory);
    }
}
