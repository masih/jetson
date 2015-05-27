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
