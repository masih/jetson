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
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.lang.reflect.Method;
import java.util.List;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public abstract class RequestDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDecoder.class);

    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf bytes, final List<Object> out) {

        final FutureResponse<?> future_response;
        future_response = decode(context, bytes);
        out.add(future_response);
    }

    protected FutureResponse<?> decode(final ChannelHandlerContext context, final ByteBuf in) {

        FutureResponse<?> future_response = null;
        final Integer id;
        final Method method;
        final Object[] arguments;
        try {
            beforeDecode(context, in);
            id = decodeId(context, in);
            future_response = new FutureResponse(id);

            method = decodeMethod(context, in);
            future_response.setMethod(method);

            arguments = decodeMethodArguments(context, in, method);
            future_response.setArguments(arguments);
        }
        catch (RPCException e) {
            LOGGER.warn("error decoding request", e);

            if (future_response != null) {
                future_response.completeExceptionally(e);
            }
            else {
                LOGGER.warn("cannot handle bad request", e);
            }
        }
        finally {
            afterDecode(context, in);
        }
        return future_response;
    }

    protected void beforeDecode(final ChannelHandlerContext context, final ByteBuf in) throws TransportException {

    }

    protected abstract Integer decodeId(ChannelHandlerContext context, ByteBuf in) throws RPCException;

    protected abstract Method decodeMethod(ChannelHandlerContext context, ByteBuf in) throws RPCException;

    protected abstract Object[] decodeMethodArguments(ChannelHandlerContext context, ByteBuf in, Method method) throws RPCException;

    protected void afterDecode(final ChannelHandlerContext context, final ByteBuf in) {

    }
}
