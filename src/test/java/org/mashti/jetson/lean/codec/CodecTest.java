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
import io.netty.buffer.Unpooled;
import java.lang.reflect.Type;
import org.junit.Before;
import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class CodecTest {

    final Codec codec;
    final Codecs codecs;
    final ByteBuf buffer;

    CodecTest(final Codec codec) {

        this(codec, new Codecs());
    }

    private CodecTest(final Codec codec, final Codecs codecs) {

        this.codec = codec;
        this.codecs = codecs;
        buffer = Unpooled.buffer();
    }

    @Before
    public void setUp() throws Exception {

        buffer.clear();
    }

    void encode(final Object value) throws RPCException {

        encode(value, value.getClass());
    }

    void encode(final Object value, final Type type) throws RPCException {

        codec.encode(value, buffer, codecs, type);
    }

    <T> T decode(final Class<T> type) throws RPCException {

        return decode((Type) type);
    }

    <T> T decode(final Type type) throws RPCException {

        return codec.decode(buffer, codecs, type);
    }
}
