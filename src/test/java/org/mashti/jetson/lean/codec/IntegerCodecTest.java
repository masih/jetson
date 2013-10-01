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
public class IntegerCodecTest extends CodecTest {

    public IntegerCodecTest() {

        super(new IntegerCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Integer.class));
        Assert.assertTrue(codec.isSupported(Integer.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final int[] ints = {54654654, -89999, -88, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        for (final int b : ints) {
            encode(b, Integer.TYPE);
        }
        Assert.assertEquals(ints.length * Integer.SIZE / 8, buffer.readableBytes());
        for (final int b : ints) {
            Assert.assertTrue(decode(Integer.TYPE).equals(b));
        }
    }
}
