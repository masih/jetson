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

import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.exception.TransportException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.MessageList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
class ResponseHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandler.class);

    @Override
    public void channelInactive(final ChannelHandlerContext context) throws Exception {

        final Channel channel = context.channel();
        ChannelPool.setException(channel, new TransportException("connection closed"));
        LOGGER.debug("client disconencted {}", context.channel().remoteAddress());
        super.channelInactive(context);
    }

    @Override
    public void messageReceived(final ChannelHandlerContext context, final MessageList<Object> messages) throws Exception {

        messages.releaseAllAndRecycle();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {

        LOGGER.info("caught on client handler", cause);
        ChannelPool.setException(context.channel(), new RPCException(cause));
        context.close();
    }
}
