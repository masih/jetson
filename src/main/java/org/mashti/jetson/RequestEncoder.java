/**
 * This file is part of jetson.
 *
 * jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jetson.  If not, see <http://www.gnu.org/licenses/>.
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
public abstract class RequestEncoder extends MessageToByteEncoder<FutureResponse<?>> {
               
    @Override
    protected void encode(final ChannelHandlerContext context, final FutureResponse<?> future_response, final ByteBuf out) {

        int current_index = out.writerIndex();
        try {
            addPendingFutureResponse(context, future_response);
            final Integer id = future_response.getId();
            final Method method = future_response.getMethod();
            final Object[] arguments = future_response.getArguments();
            encodeRequest(context, id, method, arguments, out);
            future_response.notifyWrittenByteCount(out.writerIndex());
        }             
        catch (final RPCException e) {
            future_response.notifyWrittenByteCount(out.writerIndex() - current_index);
            future_response.completeExceptionally(e);
        }
    }

    protected static void addPendingFutureResponse(final ChannelHandlerContext context, final FutureResponse<?> future_response) {

        final Channel channel = context.channel();
        ChannelFuturePool.addFutureResponse(channel, future_response);
    }

    protected abstract void encodeRequest(ChannelHandlerContext context, Integer id, Method method, Object[] arguments, ByteBuf out) throws RPCException;
}
