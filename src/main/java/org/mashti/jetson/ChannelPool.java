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

import com.google.common.util.concurrent.MoreExecutors;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelPool extends GenericKeyedObjectPool<InetSocketAddress, Channel> {

    static final AttributeKey<Set<FutureResponse>> FUTURE_RESPONSES_ATTRIBUTE_KEY = AttributeKey.valueOf("future_responses");
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelPool.class);

    public ChannelPool(final Bootstrap bootstrap) {

        this(new PooledChannelFactory(bootstrap));
    }

    private ChannelPool(final PooledChannelFactory factory) {

        super(factory);
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
        if (responses != null && !responses.isEmpty()) {
            final RPCException exception = new TransportException("channel was closed");
            for (final FutureResponse future_response : responses) {
                if (!future_response.isDone()) {
                    future_response.setException(exception);
                }
            }
        }
    }

    static void notifyCaughtException(final Channel channel, Throwable cause) {

        final Set<FutureResponse> responses = getFutureResponsesByChannel(channel);
        if (responses != null && !responses.isEmpty()) {
            final RPCException exception = new RPCException(cause);
            for (final FutureResponse r : responses) {
                r.setException(exception);
            }
        }
    }

    static FutureResponse getFutureResponse(final Channel channel, final Integer id) {

        final Set<FutureResponse> responses = getFutureResponsesByChannel(channel);
        assert responses != null : id + " unknown channel: " + channel;
        if (responses != null) {
            for (final FutureResponse r : responses) {
                if (r.getId().equals(id)) { return r; }
            }
        }
        LOGGER.warn("received response with id {} from an unknown channel {}", id, channel);
        return null;
    }

    static Set<FutureResponse> getFutureResponsesByChannel(final Channel channel) {

        return channel.attr(FUTURE_RESPONSES_ATTRIBUTE_KEY).get();
    }

    void configure() {

        setTestOnReturn(true);
        setTestOnBorrow(true);
    }

    static class PooledChannelFactory extends BaseKeyedPooledObjectFactory<InetSocketAddress, Channel> {

        private final Bootstrap bootstrap;

        PooledChannelFactory(final Bootstrap bootstrap) {

            this.bootstrap = bootstrap;
        }

        @Override
        public Channel create(InetSocketAddress address) throws Exception {

            LOGGER.trace("making new channel for {}", address);
            final Channel channel = makeConnection(address);
            channel.attr(FUTURE_RESPONSES_ATTRIBUTE_KEY).set(new ConcurrentSkipListSet<FutureResponse>());
            return channel;
        }

        @Override
        public PooledObject<Channel> wrap(final Channel channel) {

            return new DefaultPooledObject<Channel>(channel);
        }

        @Override
        public boolean validateObject(final InetSocketAddress address, final PooledObject<Channel> pooled_channel) {

            final Channel channel = pooled_channel.getObject();
            return channel.isActive() && pooled_channel.getIdleTimeMillis() < 5000;
        }

        @Override
        public void destroyObject(final InetSocketAddress address, final PooledObject<Channel> pooled_channel) throws Exception {

            final Channel channel = pooled_channel.getObject();
            channel.close();
            channel.disconnect();
        }

        private Channel makeConnection(InetSocketAddress address) throws InterruptedException {

            final ChannelFuture connect_future = bootstrap.connect(address);
            connect_future.sync();
            return connect_future.channel();
        }
    }
}
