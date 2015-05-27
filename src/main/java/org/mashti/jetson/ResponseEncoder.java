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
import io.netty.handler.codec.MessageToByteEncoder;
import java.lang.reflect.Method;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import org.mashti.jetson.exception.InternalServerException;
import org.mashti.jetson.exception.RPCException;

@Sharable
public abstract class ResponseEncoder extends MessageToByteEncoder<FutureResponse<?>> {

    //TODO http://normanmaurer.me/presentations/2014-facebook-eng-netty/slides.html#32.0
    @Override
    protected void encode(final ChannelHandlerContext context, final FutureResponse<?> future_response, final ByteBuf out) throws RPCException {

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

    protected abstract void encodeResult(ChannelHandlerContext context, Integer id, Object result, Method method, ByteBuf out) throws RPCException;

    protected abstract void encodeException(ChannelHandlerContext context, Integer id, Throwable exception, ByteBuf out) throws RPCException;

}
