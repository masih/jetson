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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ConnectTimeoutException;
import java.net.InetSocketAddress;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ChannelPool extends GenericObjectPool<Channel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelPool.class);

    protected ChannelPool(final Bootstrap bootstrap, final InetSocketAddress address) {

        this(new PoolableChannelFactory(bootstrap, address));
    }

    protected ChannelPool(final PoolableChannelFactory poolable_channel_factory) {

        super(poolable_channel_factory);
        configure();

    }

    protected void configure() {

        //        setTestOnBorrow(true);
        setTestOnReturn(true);
    }

    protected static class PoolableChannelFactory extends BasePoolableObjectFactory<Channel> {

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

        protected void setConnectionTimeout(final long timeout, final TimeUnit unit) {

            connection_timeout_in_millis = TimeUnit.MILLISECONDS.convert(timeout, unit);
        }

        @Override
        public Channel makeObject() throws Exception {

            try {
                LOGGER.info("making new channel for {}", address);
                final ChannelFuture connect_future = bootstrap.connect(address);
                //            connect_future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                if (!connect_future.await(connection_timeout_in_millis)) { throw new ConnectTimeoutException(); }
                final Channel channel = connect_future.channel();
                return channel;
            }
            catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        @Override
        public void destroyObject(final Channel channel) {

            channel.close();
        }

        @Override
        public boolean validateObject(final Channel channel) {

            return channel.isOpen() && channel.isActive() && super.validateObject(channel);
        }
    }

    static final class ResettableSemaphore extends Semaphore {

        private static final long serialVersionUID = 2418286125921263676L;
        private static final Logger LOGGER = LoggerFactory.getLogger(ResettableSemaphore.class);
        private static final int INITIAL_PERMITS = 0;

        private ResettableSemaphore() {

            super(INITIAL_PERMITS, true);
        }

        void reset() {

            interruptQueuedThreads();
            reducePermits(INITIAL_PERMITS);
        }

        private void interruptQueuedThreads() {

            for (final Thread thread : getQueuedThreads()) {
                thread.interrupt();
                LOGGER.warn("interrupted queued thread {}", thread.getName());
            }
        }
    }
}
