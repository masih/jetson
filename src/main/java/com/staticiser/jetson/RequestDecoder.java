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
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.MessageToMessageDecoder;

@Sharable
public abstract class RequestDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf bytes, final MessageList<Object> out) {

        final FutureResponse future_response;
        future_response = decode(context, bytes);
        if (future_response.isDone()) {
            context.write(future_response);
        }
        else {
            out.add(future_response);
        }
    }

    protected FutureResponse decode(final ChannelHandlerContext context, final ByteBuf in) {

        final FutureResponse future_response = new FutureResponse(context.channel());
        try {
            decodeAndSetIdMethodArguments(context, in, future_response);
        }
        catch (RPCException e) {
            future_response.setException(e);
        }
        return future_response;
    }

    protected abstract void decodeAndSetIdMethodArguments(final ChannelHandlerContext context, final ByteBuf in, final FutureResponse future_response) throws RPCException;

    protected Server getServer(ChannelHandlerContext context) {

        return RequestHandler.getServerFromContext(context);
    }

}
