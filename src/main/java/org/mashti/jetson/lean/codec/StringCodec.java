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
import java.nio.charset.Charset;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class StringCodec implements Codec {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private final Charset charset;

    StringCodec() {

        this(DEFAULT_CHARSET);
    }

    private StringCodec(final Charset charset) {

        this.charset = charset;
    }

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && String.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) {

        if (value == null) {
            out.writeInt(-1);
        }
        else {
            final byte[] bytes = ((String) value).getBytes(charset);
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        }
    }

    @Override
    public String decode(final ByteBuf in, final Codecs codecs, final Type type) {

        final int size = in.readInt();
        if (size < 0) { return null; }
        final byte[] bytes = new byte[size];
        in.readBytes(bytes);
        return new String(bytes, charset);
    }
}
