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
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a JSON RPC server.
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

    protected <T> Server(final ServerBootstrap server_bootstrap, final Object service) {

        this.server_bootstrap = server_bootstrap;
        this.service = service;
        endpoint = DEFAULT_ENDPOINT_ADDRESS;
        server_channel_group = new DefaultChannelGroup();
    }

    public void setBindAddress(final InetSocketAddress endpoint) {

        this.endpoint = endpoint;
    }

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

        server_channel.attr(ServerChannelGroupHandler.CHANNEL_GROUP_ATTRIBUTE).set(server_channel_group);
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

    public boolean isExposed() {

        return exposed;
    }

    public InetSocketAddress getLocalSocketAddress() {

        return !isExposed() ? null : endpoint;
    }
}
