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

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.lang.reflect.Method;
import org.mashti.jetson.exception.RPCException;

@Sharable
public abstract class RequestEncoder extends MessageToByteEncoder<FutureResponse> {

    @Override
    protected void encode(final ChannelHandlerContext context, final FutureResponse future_response, final ByteBuf out) {

        try {
            addPendingFutureResponse(context, future_response);
            final Integer id = future_response.getId();
            final Method method = future_response.getMethod();
            final Object[] arguments = future_response.getArguments();
            encodeRequest(context, id, method, arguments, out);

        }
        catch (final Exception e) {
            e.printStackTrace();
            future_response.setException(e);
        }
    }

    private void addPendingFutureResponse(final ChannelHandlerContext context, final FutureResponse future_response) {

        final Channel channel = context.channel();
        ChannelPool.addFutureResponse(channel, future_response);
    }

    protected abstract void encodeRequest(final ChannelHandlerContext context, final Integer id, final Method method, Object[] arguments, final ByteBuf out) throws RPCException;
}
