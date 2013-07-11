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
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public abstract class RequestDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDecoder.class);

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
                future_response.setException(e);
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

    protected abstract Integer decodeId(final ChannelHandlerContext context, final ByteBuf in) throws RPCException;

    protected abstract Method decodeMethod(final ChannelHandlerContext context, final ByteBuf in) throws RPCException;

    protected abstract Object[] decodeMethodArguments(final ChannelHandlerContext context, final ByteBuf in, Method method) throws RPCException;

    protected void afterDecode(final ChannelHandlerContext context, final ByteBuf in) {

    }
}
