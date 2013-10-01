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
public class LongCodecTest extends CodecTest {

    public LongCodecTest() {

        super(new LongCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Long.class));
        Assert.assertTrue(codec.isSupported(Long.TYPE));
        Assert.assertFalse(codec.isSupported(Integer.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final long[] longs = {54654654, -899999999999L, -88, Long.MAX_VALUE, Long.MIN_VALUE, 0};
        for (final long b : longs) {
            encode(b, Long.TYPE);
        }
        Assert.assertEquals(longs.length * Long.SIZE / 8, buffer.readableBytes());
        for (final long b : longs) {
            Assert.assertTrue(decode(Long.TYPE).equals(b));
        }
    }
}
