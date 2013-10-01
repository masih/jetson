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
package org.mashti.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class ArrayCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && ((Class<?>) type).isArray();
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeInt(-1);
        }
        else {
            final int length = Array.getLength(value);
            out.writeInt(length);
            final Class<?> component_type = ((Class) type).getComponentType();
            for (int i = 0; i < length; i++) {
                final Object element = Array.get(value, i);
                codecs.encodeAs(element, out, component_type);
            }
        }
    }

    @Override
    public Object decode(final ByteBuf in, final Codecs codecs, final Type expected_type) throws RPCException {

        final int length = in.readInt();
        if (length < 0) { return null; }
        final Class<?> expected_class = (Class<?>) expected_type;
        final Class<?> component_type = expected_class.getComponentType();
        final Object result = Array.newInstance(component_type, length);

        for (int i = 0; i < length; i++) {
            Array.set(result, i, codecs.decodeAs(in, component_type));
        }
        return result;
    }
}
