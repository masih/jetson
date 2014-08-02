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
import org.mashti.jetson.RequestEncoder;
import org.mashti.jetson.exception.MethodNotFoundException;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.codec.Codecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanRequestEncoder extends RequestEncoder {

    private final List<Method> dispatch;
    protected final Codecs codecs;

    public LeanRequestEncoder(final List<Method> dispatch, final Codecs codecs) {

        this.dispatch = dispatch;
        this.codecs = codecs;
    }

    @Override
    protected void encodeRequest(final ChannelHandlerContext context, final Integer id, final Method method, final Object[] arguments, final ByteBuf out) throws RPCException {

        final Type[] argument_types = method.getGenericParameterTypes();
        out.writeInt(id);
        writeMethod(method, out);
        writeArguments(arguments, argument_types, out);
    }

    protected void writeArguments(final Object[] arguments, final Type[] argument_types, final ByteBuf out) throws RPCException {

        for (int i = 0; i < argument_types.length; i++) {

            final Type type = argument_types[i];
            final Object argument = arguments[i];
            codecs.encodeAs(argument, out, type);
        }
    }

    protected void writeMethod(final Method method, final ByteBuf out) throws MethodNotFoundException {

        final int index = dispatch.indexOf(method);
        if (index == -1) { throw new MethodNotFoundException(); }
        out.writeByte(index);
    }
}
