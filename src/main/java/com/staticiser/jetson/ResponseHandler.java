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

        LOGGER.debug("client disconencted {}", context.channel().remoteAddress());
        final Client client = getClientFromContext(context);
        client.notifyChannelInactivation(context.channel());
        super.channelInactive(context);
    }

    @Override
    public void messageReceived(final ChannelHandlerContext context, final MessageList<Object> messages) throws Exception {

        final MessageList<Response> responses = messages.cast();
        final Client client = getClientFromContext(context);
        for (final Response response : responses) {
            client.handle(context, response);
        }
        messages.releaseAllAndRecycle();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        final Client client = getClientFromContext(context);
        client.notifyChannelInactivation(context.channel());
        LOGGER.info("caught on client handler", cause);
        context.close();
    }

    static Client getClientFromContext(final ChannelHandlerContext context) {
        return context.channel().attr(Client.CLIENT_ATTRIBUTE_KEY).get();
    }
}
