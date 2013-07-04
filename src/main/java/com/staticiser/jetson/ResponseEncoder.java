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

import com.staticiser.jetson.exception.InternalServerException;
import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.lang.reflect.Method;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

@Sharable
public abstract class ResponseEncoder extends MessageToByteEncoder<FutureResponse> {

    @Override
    protected void encode(final ChannelHandlerContext context, final FutureResponse future_response, final ByteBuf out) throws RPCException {

        final Integer id = future_response.getId();
        try {
            encodeResult(context, id, future_response.get(), future_response.getMethod(), out);
        }
        catch (InterruptedException e) {
            final Throwable exception = new InternalServerException(e);
            encodeException(context, id, exception, out);
        }
        catch (ExecutionException e) {
            encodeException(context, id, e.getCause(), out);
        }
        catch (CancellationException e) {
            final Throwable exception = new InternalServerException(e);
            encodeException(context, id, exception, out);
        }
        catch (RPCException e) {
            encodeException(context, id, e, out);
        }
    }

    protected abstract void encodeResult(final ChannelHandlerContext context, final Integer id, final Object result, final Method method, final ByteBuf out) throws RPCException;

    protected abstract void encodeException(final ChannelHandlerContext context, final Integer id, final Throwable exception, final ByteBuf out) throws RPCException;

}
