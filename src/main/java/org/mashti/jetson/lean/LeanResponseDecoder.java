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
package org.mashti.jetson.lean;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.ResponseDecoder;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.codec.Codecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanResponseDecoder extends ResponseDecoder {

    private final Codecs codecs;

    public LeanResponseDecoder(final Codecs codecs) {

        this.codecs = codecs;
    }

    @Override
    protected FutureResponse decode(final ChannelHandlerContext context, final ByteBuf in) {

        final int id = in.readInt();
        final FutureResponse response = getFutureResponseById(context, id);
        final boolean error = in.readBoolean();
        try {
            if (error) {
                final Throwable throwable = codecs.decodeAs(in, Throwable.class);
                response.setException(throwable);
            }
            else {
                final Method method = response.getMethod();
                final Type return_type = method.getGenericReturnType();
                final Object result = codecs.decodeAs(in, return_type);
                response.set(result);
            }
        }
        catch (RPCException e) {
            response.setException(e);
        }
        return response;
    }
}
