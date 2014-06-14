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

        final FutureResponse future_response;
        future_response = decode(context, bytes);
        out.add(future_response);
    }

    FutureResponse decode(final ChannelHandlerContext context, final ByteBuf in) {

        FutureResponse future_response = null;
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
