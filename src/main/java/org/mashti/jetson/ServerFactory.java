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
        final NioEventLoopGroup child_event_loop = new NioEventLoopGroup(0, new NamedThreadFactory("server_child_event_loop_"));
        server_bootstrap.group(parent_event_loop, child_event_loop);
        server_bootstrap.channel(NioServerSocketChannel.class);
        server_bootstrap.option(ChannelOption.TCP_NODELAY, true);
        server_bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        server_bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        server_bootstrap.childHandler(handler);
        return server_bootstrap;
    }
    
    @Override
    public String toString() {

        return getClass().getSimpleName();
    }
}
