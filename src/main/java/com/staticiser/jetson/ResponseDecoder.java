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
public abstract class ResponseDecoder extends MessageToMessageDecoder<ByteBuf> {

    protected Client getClient(ChannelHandlerContext context) {
        return ResponseHandler.getClientFromContext(context);

    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf msg, final MessageList<Object> out) {
        Response response;
        try {
            response = decode(ctx, msg);
        }
        catch (RPCException e) {
            response = new Response(); //TODO cache
            response.setException(e);
        }
        out.add(response);
    }

    protected abstract Response decode(final ChannelHandlerContext context, final ByteBuf msg) throws RPCException;

}
