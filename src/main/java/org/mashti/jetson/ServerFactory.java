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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.mashti.jetson.util.NamedThreadFactory;

/**
 * A factory for creating Servers.
 *
 * @param <Service> the type of the service
 */
public class ServerFactory<Service> {

    protected final ServerBootstrap server_bootstrap;

    /** Instantiates a new server factory. */
    public ServerFactory(final ServerChannelInitializer handler) {

        this(createDefaultServerBootstrap(handler));
    }

    /** Instantiates a new server factory. */
    public ServerFactory(final ServerBootstrap server_bootstrap) {

        this.server_bootstrap = server_bootstrap;
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

    /**
     * Shuts down the {@link ServerBootstrap server bootstrap} and the {@link EventLoop}s used by any server that is created using this factory.
     * After this method is called any server that is created using this factory will become unresponsive.
     *
     * @see EventLoop#shutdownGracefully()
     */
    public void shutdown() {

        server_bootstrap.group().shutdownGracefully();
        server_bootstrap.childGroup().shutdownGracefully();
    }

    protected static ServerBootstrap createDefaultServerBootstrap(final ServerChannelInitializer handler) {

        final ServerBootstrap server_bootstrap = new ServerBootstrap();
        final NioEventLoopGroup parent_event_loop = new NioEventLoopGroup(0, new NamedThreadFactory("server_parent_event_loop_"));
        final NioEventLoopGroup child_event_loop = new NioEventLoopGroup(10, new NamedThreadFactory("server_child_event_loop_"));
        server_bootstrap.group(parent_event_loop, child_event_loop);
        server_bootstrap.channel(NioServerSocketChannel.class);
        server_bootstrap.option(ChannelOption.TCP_NODELAY, true);
        server_bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        server_bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        server_bootstrap.childHandler(handler);
        return server_bootstrap;
    }
}
