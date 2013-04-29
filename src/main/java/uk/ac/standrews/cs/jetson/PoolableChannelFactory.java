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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.InetSocketAddress;
import java.util.concurrent.CyclicBarrier;

import org.apache.commons.pool.BasePoolableObjectFactory;

class PoolableChannelFactory extends BasePoolableObjectFactory<Channel> {

    private final Bootstrap bootstrap;
    private final InetSocketAddress address;

    PoolableChannelFactory(final Bootstrap bootstrap, final InetSocketAddress address) {

        this.bootstrap = bootstrap;
        this.address = address;
    }

    @Override
    public Channel makeObject() throws Exception {

        final ChannelFuture connect_future = bootstrap.connect(address);
        connect_future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        final Channel channel = connect_future.sync().channel();
        configureChannel(channel);
        return channel;
    }

    private void configureChannel(final Channel channel) {

        channel.attr(ClientHandler.RESPONSE_BARRIER_ATTRIBUTE).set(new CyclicBarrier(2));
        channel.attr(ClientHandler.REQUEST_ATTRIBUTE).set(new Request());
        channel.attr(ClientHandler.RESPONSE_ATTRIBUTE).set(new Response());
    }

    @Override
    public void destroyObject(final Channel channel) {

        channel.close();
    }

    @Override
    public void passivateObject(final Channel channel) throws Exception {

        channel.attr(ClientHandler.REQUEST_ATTRIBUTE).get().reset();
        channel.attr(ClientHandler.RESPONSE_BARRIER_ATTRIBUTE).get().reset();
        channel.attr(ClientHandler.RESPONSE_ATTRIBUTE).get().reset();
    }

    @Override
    public boolean validateObject(final Channel channel) {

        return channel.isOpen() && channel.isActive() && super.validateObject(channel);
    }
}