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
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelUtils {

    static final AttributeKey<Set<FutureResponse>> FUTURE_RESPONSES_ATTRIBUTE_KEY = AttributeKey.valueOf("future_responses");
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelUtils.class);

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

    static Channel create(InetSocketAddress address, Bootstrap bootstrap) throws Exception {

        LOGGER.trace("making new channel for {}", address);
        final Channel channel = makeConnection(address, bootstrap);
        channel.attr(FUTURE_RESPONSES_ATTRIBUTE_KEY).set(new ConcurrentSkipListSet<FutureResponse>());
        return channel;
    }

    private static Channel makeConnection(InetSocketAddress address, Bootstrap bootstrap) throws InterruptedException {

        final ChannelFuture connect_future = bootstrap.connect(address);
        connect_future.sync();
        return connect_future.channel();
    }

}
