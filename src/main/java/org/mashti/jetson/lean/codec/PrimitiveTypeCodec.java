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
import org.mashti.jetson.util.ReflectionUtil;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
abstract class PrimitiveTypeCodec implements Codec {

    private final Class<?> primitive_class;
    private final Type primitive_type;

    PrimitiveTypeCodec(final Class<?> primitive_class, final Type primitive_type) {

        this.primitive_class = primitive_class;
        this.primitive_type = primitive_type;
    }

    @Override
    public boolean isSupported(final Type type) {

        return type == primitive_type || type == primitive_class;
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) {

        if (ReflectionUtil.isPrimitive(type)) {
            writeValue(out, value);
        }
        else {
            final boolean null_value = value == null;
            out.writeBoolean(null_value);
            if (!null_value) {
                writeValue(out, value);
            }
        }
    }

    @Override
    public Object decode(final ByteBuf in, final Codecs codecs, final Type type) {

        if (ReflectionUtil.isPrimitive(type)) {
            return readValue(in);
        }
        else {
            if (!in.readBoolean()) { return readValue(in); }
            return null;
        }
    }

    protected abstract Object readValue(final ByteBuf in);

    protected abstract void writeValue(ByteBuf out, Object value);
}
