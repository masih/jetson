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
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ImmediateEventExecutor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import org.mashti.jetson.exception.IllegalAccessException;
import org.mashti.jetson.exception.IllegalArgumentException;
import org.mashti.jetson.exception.InternalServerException;
import org.mashti.jetson.exception.ServerRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a RPC server.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Server {

    static final AttributeKey<Server> SERVER_ATTRIBUTE = AttributeKey.valueOf("server");
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final InetSocketAddress DEFAULT_ENDPOINT_ADDRESS = new InetSocketAddress(0);
    private final ServerBootstrap server_bootstrap;
    private final ChannelGroup server_channel_group;
    private final Object service;
    private volatile Channel server_channel;
    private volatile InetSocketAddress endpoint;
    private volatile boolean exposed;
    private volatile WrittenByteCountListener written_byte_count_listener;

    protected Server(final ServerBootstrap server_bootstrap, final Object service) {

        this.server_bootstrap = server_bootstrap;
        this.service = service;
        endpoint = DEFAULT_ENDPOINT_ADDRESS;
        server_channel_group = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    }

    public void setWrittenByteCountListener(WrittenByteCountListener listener) {

        written_byte_count_listener = listener;
    }

    /**
     * Sets the socket address on which this server will be listening for incoming connections.
     * A port number of {@code zero} will let the system pick up an ephemeral port when this server is {@link #expose() exposed}.
     *
     * @param endpoint the new bind address
     */
    public void setBindAddress(final InetSocketAddress endpoint) {

        this.endpoint = endpoint;
    }

    /**
     * Exposes this server to the incoming connections.
     *
     * @return whether the exposure of this sever was changed
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized boolean expose() throws IOException {

        final boolean exposure_changed;
        if (!isExposed()) {
            attemptBind();
            updateLocalSocketAddress();
            configureServerChannel();
            exposed = true;
            LOGGER.debug("exposed server on {}", endpoint);
            exposure_changed = true;
        }
        else {
            LOGGER.debug("expose was called when the server is already exposed; local address: {}", endpoint);
            exposure_changed = false;
        }
        return exposure_changed;
    }

    /**
     * Disconnects all the connected clients and stops listening for the incoming connections.
     * This method has no effect if this server is not exposed.
     *
     * @return whether the exposure of this sever was changed
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized boolean unexpose() throws IOException {

        final boolean exposure_changed;
        if (isExposed()) {
            try {
                unbindServerChannel();
                disconnectActiveClients();
                exposed = false;
                LOGGER.debug("unexposed server on {}", endpoint);
                exposure_changed = true;
            }
            catch (final Exception e) {
                LOGGER.debug("error while un-exposing server", e);
                throw new IOException(e);
            }
        }
        else {
            LOGGER.debug("unexpose was called when the server is already unexposed; local address: {}", endpoint);
            exposure_changed = false;
        }
        return exposure_changed;
    }

    /**
     * Checks if this server is listening for incoming connections.
     *
     * @return {@code true} if this server is listening for incoming connections; {@code false} otherwise
     */
    public boolean isExposed() {

        return exposed;
    }

    /**
     * Gets the address to which this server listens for incoming connections, or {@code null} if this server is not exposed.
     *
     * @return the address to which this server listens for incoming connections, or {@code null} if this server is not exposed
     * @see #isExposed()
     */
    public InetSocketAddress getLocalSocketAddress() {

        return endpoint;
    }

    static Server getServerFromContext(final ChannelHandlerContext context) {

        return context.channel()
                .parent()
                .attr(SERVER_ATTRIBUTE)
                .get();
    }

    protected void handle(final ChannelHandlerContext context, final FutureResponse<Object> future_response) {

        assert isExposed();

        future_response.setWrittenByteCountListener(written_byte_count_listener);
        final Method method = future_response.getMethod();
        final Object[] arguments = future_response.getArguments();
        if (!future_response.isDone()) {

            try {
                handleRequest(method, arguments).whenComplete((result, error) -> {
                    if (error == null) {
                        future_response.complete(result);
                    }
                    else {
                        future_response.completeExceptionally(error);
                    }
                    context.writeAndFlush(future_response);
                });
            }
            catch (final Throwable e) {
                future_response.completeExceptionally(e);
                context.writeAndFlush(future_response);
            }
        }
    }

    protected void notifyChannelActivation(final Channel channel) {

        server_channel_group.add(channel);
    }

    protected void notifyChannelInactivation(final Channel channel) {

        server_channel_group.remove(channel);
    }

    private CompletableFuture<?> handleRequest(final Method method, final Object[] arguments) throws Throwable {

        try {

            return (CompletableFuture<?>) method.invoke(service, arguments);
        }
        catch (final java.lang.IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
        catch (final RuntimeException e) {
            throw new ServerRuntimeException(e);
        }
        catch (final InvocationTargetException e) {
            throw e.getCause();
        }
        catch (final java.lang.IllegalAccessException e) {
            throw new IllegalAccessException(e);
        }
        catch (final ExceptionInInitializerError e) {
            throw new InternalServerException(e);
        }
    }

    private void configureServerChannel() {

        server_channel.attr(SERVER_ATTRIBUTE)
                .set(this);
    }

    private void updateLocalSocketAddress() {

        endpoint = (InetSocketAddress) server_channel.localAddress();
    }

    private void attemptBind() throws IOException {

        try {
            server_channel = server_bootstrap.bind(endpoint)
                    .sync()
                    .channel();
        }
        catch (final Exception e) {
            LOGGER.debug("error while waiting for channel exposure", e);
            throw new IOException(e);
        }
    }

    private void unbindServerChannel() throws InterruptedException {

        server_channel.disconnect()
                .sync();
        server_channel.closeFuture()
                .sync();
    }

    private void disconnectActiveClients() throws InterruptedException {

        server_channel_group.disconnect()
                .sync();
        server_channel_group.close()
                .sync();
        server_channel_group.clear();
    }
}
