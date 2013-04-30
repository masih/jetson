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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BaseChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseChannelInitializer.class);
    private static final long DEFAULT_READ_TIMEOUT_IN_SECONDS = 150;
    private static final long DEFAULT_WRITE_TIMEOUT_IN_SECONDS = 150;
    private final long read_timeout;
    private final long write_timeout;
    private final TimeUnit timeout_unit;

    public BaseChannelInitializer() {

        this(DEFAULT_READ_TIMEOUT_IN_SECONDS, DEFAULT_WRITE_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    }

    public BaseChannelInitializer(final long read_timeout, final long write_timeout, final TimeUnit timeout_unit) {

        this.read_timeout = read_timeout;
        this.write_timeout = write_timeout;
        this.timeout_unit = timeout_unit;
    }

    private WriteTimeoutHandler createWriteTimeoutHandler() {

        return new WriteTimeoutHandler(write_timeout, timeout_unit);
    }

    private ReadTimeoutHandler createReadTimeoutHandler() {

        return new ReadTimeoutHandler(read_timeout, timeout_unit);
    }

    @Override
    public void initChannel(final SocketChannel channel) throws Exception {

        channel.pipeline().addLast("write_timeout", createWriteTimeoutHandler());
        channel.pipeline().addLast("read_timeout", createReadTimeoutHandler());
        channel.pipeline().addLast(FrameDecoder.NAME, createFrameDecoder());
    }

    protected FrameDecoder createFrameDecoder() {

        return new FrameDecoder();
    }
}
