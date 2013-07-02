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

import com.staticiser.jetson.exception.IllegalAccessException;
import com.staticiser.jetson.exception.IllegalArgumentException;
import com.staticiser.jetson.exception.InternalServerException;
import com.staticiser.jetson.exception.ServerRuntimeException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ImmediateEventExecutor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a JSON RPC server.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Server {

    static final AttributeKey<Server> SERVER_ATTRIBUTE = new AttributeKey<Server>("server");
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final InetSocketAddress DEFAULT_ENDPOINT_ADDRESS = new InetSocketAddress(0);
    private final ServerBootstrap server_bootstrap;
    private final ChannelGroup server_channel_group;
    private final Object service;
    private final ExecutorService executor;
    private volatile Channel server_channel;
    private volatile InetSocketAddress endpoint;
    private volatile boolean exposed;

    protected Server(final ServerBootstrap server_bootstrap, final Object service, ExecutorService executor) {

        this.server_bootstrap = server_bootstrap;
        this.service = service;
        this.executor = executor;
        endpoint = DEFAULT_ENDPOINT_ADDRESS;
        server_channel_group = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
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
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void expose() throws IOException {

        if (!isExposed()) {
            attemptBind();
            updateLocalSocketAddress();
            configureServerChannel();
            exposed = true;
            LOGGER.debug("exposed server on {}", endpoint);
        }
    }

    public void handle(final ChannelHandlerContext context, final Request request) throws Exception {

        final Callable<ChannelFuture> task = new Callable<ChannelFuture>() {

            @Override
            public ChannelFuture call() throws Exception {

                final Response response = new Response(); //FIXME cache
                response.setRequest(request);
                try {
                    response.setResult(handleRequest(request));
                }
                catch (final Throwable e) {
                    response.setException(e);
                }
                return context.write(response);
            }
        };
        final Future<ChannelFuture> processing_future = executor.submit(task); // TODO add to a map; upon channel inactivation cancel the processing if not done
    }

    public void notifyChannelActivation(final Channel channel) {

        server_channel_group.add(channel);
    }

    public void notifyChannelInactivation(final Channel channel) {

        server_channel_group.remove(channel);
    }

    /**
     * Disconnects all the connected clients and stops listening for the incoming connections.
     * This method has no effect if this server is not exposed.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void unexpose() throws IOException {

        if (isExposed()) {
            try {
                disconnectActiveClients();
                unbindServerChannel();
                exposed = false;
                LOGGER.debug("unexposed server on {}", endpoint);
            }
            catch (final Exception e) {
                LOGGER.error("error while unexposing server", e);
                throw new IOException(e);
            }
        }
        else {
            LOGGER.warn("unexpose was called when the server is already unexposed; local address: {}", endpoint);
        }
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

        return !isExposed() ? null : endpoint;
    }

    private Object handleRequest(final Request request) throws Throwable {

        final Method method = request.getMethod();
        final Object[] arguments = request.getArguments();

        try {
            return method.invoke(service, arguments);
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

        server_channel.attr(SERVER_ATTRIBUTE).set(this);
    }

    private void updateLocalSocketAddress() {

        endpoint = (InetSocketAddress) server_channel.localAddress();
    }

    private void attemptBind() throws IOException {

        try {
            server_channel = server_bootstrap.bind(endpoint).sync().channel();
        }
        catch (final Exception e) {
            LOGGER.error("error while waiting for channel exposure", e);
            throw new IOException(e);
        }
    }

    private void unbindServerChannel() throws InterruptedException {

        server_channel.disconnect().sync();
        server_channel.closeFuture().sync();
    }

    private void disconnectActiveClients() throws InterruptedException {

        server_channel_group.disconnect().sync();
        server_channel_group.close().sync();
        server_channel_group.clear();
    }
}
