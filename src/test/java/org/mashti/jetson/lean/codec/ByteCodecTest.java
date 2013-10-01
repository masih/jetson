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

import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ByteCodecTest extends CodecTest {

    //FIXME separate primitives from Objects

    public ByteCodecTest() {

        super(new ByteCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Byte.class));
        Assert.assertTrue(codec.isSupported(Byte.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final byte[] bytes = {0, -52, 127, -127};
        for (final byte b : bytes) {
            encode(b, Byte.TYPE);
        }
        Assert.assertEquals(bytes.length, buffer.readableBytes());
        for (final byte b : bytes) {
            Assert.assertTrue(b == decode(Byte.TYPE));
        }
    }
}
