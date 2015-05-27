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
