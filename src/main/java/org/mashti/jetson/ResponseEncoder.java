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
import io.netty.handler.codec.MessageToByteEncoder;
import java.lang.reflect.Method;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import org.mashti.jetson.exception.InternalServerException;
import org.mashti.jetson.exception.RPCException;

@Sharable
public abstract class ResponseEncoder extends MessageToByteEncoder<FutureResponse> {

    @Override
    protected void encode(final ChannelHandlerContext context, final FutureResponse future_response, final ByteBuf out) throws RPCException {

        final int current_index = out.writerIndex();
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
        finally {
            future_response.notifyWrittenByteCount(out.writerIndex() - current_index);
        }
    }

    protected abstract void encodeResult(final ChannelHandlerContext context, final Integer id, final Object result, final Method method, final ByteBuf out) throws RPCException;

    protected abstract void encodeException(final ChannelHandlerContext context, final Integer id, final Throwable exception, final ByteBuf out) throws RPCException;

}
