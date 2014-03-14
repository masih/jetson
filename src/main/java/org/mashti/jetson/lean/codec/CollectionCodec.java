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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class CollectionCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && isCollection(type) || isParameterizedCollection(type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeInt(-1);
        }
        else {

            final Collection collection = (Collection) value;
            final Type component_type = getComponentType(type);
            final int size = collection.size();
            out.writeInt(size);
            for (final Object element : collection) {
                codecs.encodeAs(element, out, component_type);
            }
        }
    }

    @Override
    public Collection decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final int length = in.readInt();
        if (length < 0) { return null; }
        final Collection result = constructCollectionOfType(type);
        final Type component_type = getComponentType(type);
        for (int i = 0; i < length; i++) {
            result.add(codecs.decodeAs(in, component_type));
        }
        return result;

    }

    boolean isParameterizedCollection(final Type type) {

        return type != null && type instanceof ParameterizedType && Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType());
    }

    boolean isCollection(final Type type) {

        return type instanceof Class<?> && Collection.class.isAssignableFrom((Class<?>) type);

    }

    protected Type getComponentType(final Type type) {

        return type instanceof ParameterizedType ? ((ParameterizedType) type).getActualTypeArguments()[0] : Object.class;
    }

    protected abstract Collection constructCollectionOfType(final Type type);
}
