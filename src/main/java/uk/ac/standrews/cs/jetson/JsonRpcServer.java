package uk.ac.standrews.cs.jetson;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.standrews.cs.jetson.util.ReflectionUtil;

import com.fasterxml.jackson.core.JsonFactory;

public class JsonRpcServer {

    private static final Logger LOGGER = Logger.getLogger(JsonRpcServer.class.getName());
    private static final EventLoopGroup GLOBAL_SERVER_THREADS_GROUP = new NioEventLoopGroup(100);

    private final Map<String, Method> dispatch;
    private volatile InetSocketAddress endpoint;
    private final ServerBootstrap bootstrap;
    private ChannelFuture server_channel_future;

    public <T> JsonRpcServer(final Class<T> service_interface, final T service, final JsonFactory json_factory) {

        dispatch = ReflectionUtil.mapNamesToMethods(service_interface);
        bootstrap = new ServerBootstrap();
        bootstrap.group(GLOBAL_SERVER_THREADS_GROUP).channel(NioServerSocketChannel.class).childHandler(new JsonRpcServerPipelineFactory(service, json_factory, dispatch));
        endpoint = new InetSocketAddress(0);
    }

    public void setBindAddress(final InetSocketAddress endpoint) {

        this.endpoint = endpoint;
    }

    public synchronized void expose() throws IOException {

        if (!isExposed()) {
            try {
                server_channel_future = bootstrap.bind(endpoint).sync();
            }
            catch (final InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    public synchronized void unexpose() throws IOException {

        if (isExposed()) {
            try {
                server_channel_future.channel().close().sync();
            }
            catch (final InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    public boolean isExposed() {

        return server_channel_future != null && server_channel_future.channel().isOpen();
    }

    public InetSocketAddress getLocalSocketAddress() {

        synchronized (server_channel_future) {
            return (InetSocketAddress) (!isExposed() ? null : server_channel_future.channel().localAddress());
        }
    }

    public void shutdown() {

        try {
            unexpose();
        }
        catch (final IOException e) {
            LOGGER.log(Level.WARNING, "error while unexposing the server", e);
        }
    }
}
