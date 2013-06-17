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

import com.fasterxml.jackson.core.JsonFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ImmediateEventExecutor;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a JSON RPC server.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final InetSocketAddress DEFAULT_ENDPOINT_ADDRESS = new InetSocketAddress(0);
    static final AttributeKey<Object> SERVICE_ATTRIBUTE = new AttributeKey<Object>("service");

    private final ServerBootstrap server_bootstrap;
    private final ChannelGroup server_channel_group;
    private volatile Channel server_channel;
    private volatile InetSocketAddress endpoint;
    private volatile boolean exposed;
    private final Object service;

    /**
     * Instantiates a new server for the given {@code service_interface}.
     *
     * @param <T> the type of the service
     * @param service_interface the type of the service
     * @param service the service implementation
     * @param json_factory the JSON factory
     */
    public <T> Server(final Class<T> service_interface, final T service, final JsonFactory json_factory) {

        this(ServerFactory.createDefaultServerBootstrap(service_interface, json_factory), service);
    }

    protected Server(final ServerBootstrap server_bootstrap, final Object service) {

        this.server_bootstrap = server_bootstrap;
        this.service = service;
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

    private void configureServerChannel() {

        server_channel.attr(RequestHandler.CHANNEL_GROUP_ATTRIBUTE).set(server_channel_group);
        server_channel.attr(SERVICE_ATTRIBUTE).set(service);
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

    private void unbindServerChannel() throws InterruptedException {

        server_channel.disconnect().sync();
        server_channel.closeFuture().sync();
    }

    private void disconnectActiveClients() throws InterruptedException {

        server_channel_group.disconnect().sync();
        server_channel_group.close().sync();
        server_channel_group.clear();
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
}
