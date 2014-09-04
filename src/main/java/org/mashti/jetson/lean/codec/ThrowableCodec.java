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
import java.lang.reflect.Type;
import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class ThrowableCodec extends SerializableCodec {

    public static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && Throwable.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        final Throwable throwable = (Throwable) value;
        skipStackTrace(throwable);
        super.encode(value, out, codecs, type);
    }

    @Override
    public Throwable decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        return (Throwable) super.decode(in, codecs, type);
    }

    private void skipStackTrace(final Throwable value) {

        if (value != null) {
            value.setStackTrace(EMPTY_STACK_TRACE);
        }
    }
}
