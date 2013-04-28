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
