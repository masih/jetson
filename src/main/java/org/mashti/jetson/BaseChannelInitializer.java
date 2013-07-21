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

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BaseChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final int DEFAULT_LENGTH_FIELD_LENGTH = 2;
    private static final int DEFAULT_MAX_FRAME_LENGTH = 0xFFFF;
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseChannelInitializer.class);
    private static final long DEFAULT_READ_TIMEOUT_IN_SECONDS = 30;
    private static final long DEFAULT_WRITE_TIMEOUT_IN_SECONDS = DEFAULT_READ_TIMEOUT_IN_SECONDS;
    private static final LengthFieldPrepender DEFAULT_FRAME_ENCODER = new LengthFieldPrepender(DEFAULT_LENGTH_FIELD_LENGTH);
    private static final LoggingHandler LOGGING = new LoggingHandler(LogLevel.INFO);
    private volatile long read_timeout;
    private volatile TimeUnit read_timeout_unit;
    private volatile long write_timeout;
    private volatile TimeUnit write_timeout_unit;

    BaseChannelInitializer() {

        setReadTimeout(DEFAULT_READ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        setWriteTimeout(DEFAULT_WRITE_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void initChannel(final SocketChannel channel) throws Exception {

        if (LOGGER.isDebugEnabled()) {
            channel.pipeline().addLast(LOGGING);
        }
        channel.pipeline().addLast("write_timeout", createWriteTimeoutHandler());
        channel.pipeline().addLast("read_timeout", createReadTimeoutHandler());
        channel.pipeline().addLast("frame_decoder", getFrameDecoder());
        channel.pipeline().addLast("frame_encoder", getFrameEncoder());
    }

    void setReadTimeout(final long timeout, final TimeUnit unit) {

        read_timeout = timeout;
        read_timeout_unit = unit;
    }

    void setWriteTimeout(final long timeout, final TimeUnit unit) {

        write_timeout = timeout;
        write_timeout_unit = unit;
    }

    ChannelOutboundHandler getFrameEncoder() {

        return DEFAULT_FRAME_ENCODER;
    }

    ChannelInboundHandler getFrameDecoder() {

        return new LengthFieldBasedFrameDecoder(DEFAULT_MAX_FRAME_LENGTH, 0, DEFAULT_LENGTH_FIELD_LENGTH, 0, DEFAULT_LENGTH_FIELD_LENGTH);
    }

    private WriteTimeoutHandler createWriteTimeoutHandler() {

        return new WriteTimeoutHandler(write_timeout, write_timeout_unit);
    }

    private ReadTimeoutHandler createReadTimeoutHandler() {

        return new ReadTimeoutHandler(read_timeout, read_timeout_unit);
    }
}
