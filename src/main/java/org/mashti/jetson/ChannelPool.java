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
package org.mashti.jetson;

import com.google.common.util.concurrent.MoreExecutors;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ConnectTimeoutException;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelPool extends GenericObjectPool<Channel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelPool.class);
    private static final AttributeKey<Set<FutureResponse>> FUTURE_RESPONSES_ATTRIBUTE_KEY = new AttributeKey<Set<FutureResponse>>("future_responses");

    ChannelPool(final Bootstrap bootstrap, final InetSocketAddress address) {

        this(new PoolableChannelFactory(bootstrap, address));
    }

    private ChannelPool(final PoolableChannelFactory poolable_channel_factory) {

        super(poolable_channel_factory);
        configure();
    }

    static boolean addFutureResponse(final Channel channel, final FutureResponse response) {

        final Set<FutureResponse> responses = getFutureResponsesByChannel(channel);
        final boolean added = responses.add(response);
        if (added) {
            response.addListener(new Runnable() {

                @Override
                public void run() {

                    responses.remove(response);
                }
            }, MoreExecutors.sameThreadExecutor());
        }
        return added;
    }

    static void notifyChannelInactivation(final Channel channel) {

        final Set<FutureResponse> responses = getFutureResponsesByChannel(channel);
        if (responses != null && responses.size() > 0) {
            final RPCException exception = new TransportException("channel was closed");
            for (final FutureResponse r : responses) {
                r.setException(exception);
            }
        }
    }

    static FutureResponse getFutureResponse(final Channel channel, final Integer id) {

        final Set<FutureResponse> responses = getFutureResponsesByChannel(channel);
        for (final FutureResponse r : responses) {
            if (r.getId().equals(id)) { return r; }
        }
        return null;
    }

    private static Set<FutureResponse> getFutureResponsesByChannel(final Channel channel) {

        return channel.attr(FUTURE_RESPONSES_ATTRIBUTE_KEY).get();
    }

    void configure() {

        setTestOnBorrow(true);
        setTestOnReturn(true);
        setMinEvictableIdleTimeMillis(500);
    }

    static class PoolableChannelFactory extends BasePoolableObjectFactory<Channel> {

        private static final long DEFAULT_CONNECTION_TIMEOUT_IN_MILLIS = 5 * 1000;
        private final Bootstrap bootstrap;
        private final InetSocketAddress address;
        private volatile long connection_timeout_in_millis;

        PoolableChannelFactory(final Bootstrap bootstrap, final InetSocketAddress address) {

            this(bootstrap, address, DEFAULT_CONNECTION_TIMEOUT_IN_MILLIS);
        }

        PoolableChannelFactory(final Bootstrap bootstrap, final InetSocketAddress address, final long connection_timeout_in_millis) {

            this.bootstrap = bootstrap;
            this.address = address;
            this.connection_timeout_in_millis = connection_timeout_in_millis;
        }

        @Override
        public Channel makeObject() throws Exception {

            LOGGER.debug("making new channel for {}", address);
            final ChannelFuture connect_future = bootstrap.connect(address);
            connect_future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            if (!connect_future.await(connection_timeout_in_millis)) { throw new ConnectTimeoutException(); }
            final Channel channel = connect_future.channel();
            channel.attr(FUTURE_RESPONSES_ATTRIBUTE_KEY).set(new ConcurrentSkipListSet<FutureResponse>());
            return channel;
        }

        @Override
        public void destroyObject(final Channel channel) {

            channel.close();
            channel.disconnect();
        }

        @Override
        public boolean validateObject(final Channel channel) {

            return channel.isOpen() && channel.isActive() && super.validateObject(channel);
        }

        protected void setConnectionTimeout(final long timeout, final TimeUnit unit) {

            connection_timeout_in_millis = TimeUnit.MILLISECONDS.convert(timeout, unit);
        }
    }
}
