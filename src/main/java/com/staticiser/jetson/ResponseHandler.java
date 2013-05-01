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
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.concurrent.CyclicBarrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.staticiser.jetson.exception.JsonRpcError;
import com.staticiser.jetson.exception.JsonRpcException;
import com.staticiser.jetson.exception.TransportException;
import com.staticiser.jetson.exception.UnexpectedException;


@Sharable
class ResponseHandler extends ChannelInboundMessageHandlerAdapter<Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandler.class);
    static final AttributeKey<Response> RESPONSE_ATTRIBUTE = new AttributeKey<Response>("response");
    static final AttributeKey<Request> REQUEST_ATTRIBUTE = new AttributeKey<Request>("request");
    static final AttributeKey<CyclicBarrier> RESPONSE_BARRIER_ATTRIBUTE = new AttributeKey<CyclicBarrier>("response_latch");

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final Response response) throws Exception {

        ctx.channel().attr(RESPONSE_ATTRIBUTE).set(response);
        ctx.channel().attr(RESPONSE_BARRIER_ATTRIBUTE).get().await();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {

        LOGGER.debug("client disconencted {}", ctx.channel().remoteAddress());
        ctx.close();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {

        LOGGER.debug("caught on client handler", cause);
        final CyclicBarrier latch = ctx.channel().attr(RESPONSE_BARRIER_ATTRIBUTE).get();
        if (latch != null) {
            final Attribute<Request> id_attr = ctx.channel().attr(ResponseHandler.REQUEST_ATTRIBUTE);
            final Long request_id = id_attr == null ? null : id_attr.get().getId();
            final Response response = ctx.channel().attr(RESPONSE_ATTRIBUTE).get();
            final JsonRpcError error;
            if (cause instanceof JsonRpcException) {
                error = JsonRpcException.class.cast(cause);
            }
            else if (cause instanceof TimeoutException) {
                error = new TransportException(cause);
            }
            else {
                error = new UnexpectedException(cause);
            }
            response.setId(request_id);
            response.setError(error);
            latch.reset();
        }
        else {
            ctx.close();
        }
    }
}