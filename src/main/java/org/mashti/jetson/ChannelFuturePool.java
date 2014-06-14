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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelFuturePool {

    static final AttributeKey<Set<FutureResponse<?>>> FUTURE_RESPONSES_ATTRIBUTE_KEY = AttributeKey.valueOf("future_responses");
    private static final AttributeKey<Long> CREATION_TIME_ATTRIBUTE = AttributeKey.valueOf("creation_time");
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelFuturePool.class);
    private final Bootstrap bootstrap;
    private int max_age_millis = 2000;
    private final ConcurrentMap<InetSocketAddress, ChannelFuture> channel_future_pool = new ConcurrentHashMap<InetSocketAddress, ChannelFuture>();

    public ChannelFuturePool(final Bootstrap bootstrap) {

        this.bootstrap = bootstrap;
    }

    public void setMaxPooledObjectAgeInMillis(int max_age_millis) {

        this.max_age_millis = max_age_millis;
    }

    public int getMaxPooledObjectAgeInMillis() {

        return max_age_millis;
    }

    public Set<Map.Entry<InetSocketAddress, ChannelFuture>> getPooledEntries() {

        return channel_future_pool.entrySet();
    }

    public synchronized void clear() {

        final Iterator<Map.Entry<InetSocketAddress, ChannelFuture>> iterator = channel_future_pool.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<InetSocketAddress, ChannelFuture> next = iterator.next();
            iterator.remove();
            destroy(next.getValue());
        }

        channel_future_pool.clear();
    }

    static boolean addFutureResponse(final Channel channel, final FutureResponse response) {

        final Set<FutureResponse<?>> responses = getFutureResponsesByChannel(channel);
        final boolean added = responses.add(response);
        if (added) {
            response.thenRun(() -> responses.remove(response));
        }
        return added;
    }

    static void notifyChannelInactivation(final Channel channel) {

        final Set<FutureResponse<?>> responses = getFutureResponsesByChannel(channel);
        if (responses != null && !responses.isEmpty()) {
            final RPCException exception = new TransportException("channel was closed");
            responses.stream().filter(future_response -> !future_response.isDone()).forEach(future_response -> {
                future_response.completeExceptionally(exception);
            });
        }
    }

    static void notifyCaughtException(final Channel channel, Throwable cause) {

        final Set<FutureResponse<?>> responses = getFutureResponsesByChannel(channel);
        if (responses != null && !responses.isEmpty()) {
            final RPCException exception = new RPCException(cause);
            responses.stream().forEach(future_response -> {
                future_response.completeExceptionally(exception);
            });
        }
    }

    static FutureResponse getFutureResponse(final Channel channel, final Integer id) {

        final Set<FutureResponse<?>> responses = getFutureResponsesByChannel(channel);
        assert responses != null : id + " unknown channel: " + channel;
        if (responses != null) {
            for (final FutureResponse r : responses) {
                if (r.getId().equals(id)) { return r; }
            }
        }
        LOGGER.warn("received response with id {} from an unknown channel {}", id, channel);
        return null;
    }

    static Set<FutureResponse<?>> getFutureResponsesByChannel(final Channel channel) {

        return channel.attr(FUTURE_RESPONSES_ATTRIBUTE_KEY).get();
    }

    public ChannelFuture get(InetSocketAddress address) {

        final ChannelFuture channel_future = channel_future_pool.get(address);
        if (channel_future != null) {

            if (isValid(channel_future)) {
                return channel_future;
            }
            else {
                channel_future_pool.remove(address, channel_future);
            }
        }

        final ChannelFuture newly_created = create(address);
        final ChannelFuture existing = channel_future_pool.putIfAbsent(address, newly_created);
        if (existing == null) {
            return newly_created;
        }
        else {
            if (isValid(existing)) {
                destroy(newly_created);
                return existing;
            }
            else {
                channel_future_pool.remove(address, existing);
                return newly_created;
            }
        }
    }

    protected ChannelFuture create(final InetSocketAddress address) {

        LOGGER.trace("making new channel for {}", address);
        final ChannelFuture channel_future = bootstrap.connect(address);
        channel_future.channel().attr(FUTURE_RESPONSES_ATTRIBUTE_KEY).set(new ConcurrentSkipListSet<FutureResponse<?>>());
        channel_future.channel().attr(CREATION_TIME_ATTRIBUTE).set(System.currentTimeMillis());
        return channel_future;
    }

    protected boolean isValid(ChannelFuture channel_future) {

        final long since_creation_time = System.currentTimeMillis() - channel_future.channel().attr(CREATION_TIME_ATTRIBUTE).get();
        return !channel_future.isDone() || (!channel_future.isSuccess() && since_creation_time < max_age_millis) || (channel_future.isSuccess() && !channel_future.channel().closeFuture().isSuccess());
    }

    protected void destroy(ChannelFuture channel_future) {

        if (channel_future.isSuccess()) {
            final Channel channel = channel_future.channel();
            channel.close();
            channel.disconnect();
        }
        else {
            channel_future.cancel(true);
        }
    }
}