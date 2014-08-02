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
import java.util.List;
import org.mashti.jetson.RequestDecoder;
import org.mashti.jetson.exception.MethodNotFoundException;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.codec.Codecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanRequestDecoder extends RequestDecoder {

    private final List<Method> dispatch;
    protected final Codecs codecs;

    public LeanRequestDecoder(final List<Method> dispatch, final Codecs codecs) {

        this.dispatch = dispatch;
        this.codecs = codecs;
    }

    private Method getMethodByIndex(final int index) throws MethodNotFoundException {

        try {
            return dispatch.get(index);
        }
        catch (final IndexOutOfBoundsException e) {
            throw new MethodNotFoundException("no method is found with the index: " + index, e);
        }
    }

    private Object[] readArguments(final Type[] argument_types, final ByteBuf in) throws RPCException {

        final int arguments_count = argument_types.length;
        final Object[] arguments;
        if (arguments_count > 0) {

            arguments = new Object[arguments_count];
            for (int i = 0; i < arguments_count; i++) {
                arguments[i] = codecs.decodeAs(in, argument_types[i]);
            }
        }
        else {
            arguments = null;
        }
        return arguments;
    }

    @Override
    protected Integer decodeId(final ChannelHandlerContext context, final ByteBuf in) throws RPCException {

        return in.readInt();
    }

    @Override
    protected Method decodeMethod(final ChannelHandlerContext context, final ByteBuf in) throws RPCException {

        final int method_index = in.readByte();
        return getMethodByIndex(method_index);
    }

    @Override
    protected Object[] decodeMethodArguments(final ChannelHandlerContext context, final ByteBuf in, final Method method) throws RPCException {

        final Type[] argument_types = method.getGenericParameterTypes();
        return readArguments(argument_types, in);
    }
}
