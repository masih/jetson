/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
