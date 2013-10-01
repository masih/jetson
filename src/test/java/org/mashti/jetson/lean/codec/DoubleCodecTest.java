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
public class DoubleCodecTest extends CodecTest {

    public DoubleCodecTest() {

        super(new DoubleCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Double.class));
        Assert.assertTrue(codec.isSupported(Double.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final double[] doubles = {15.9, 64565465465.54654654, -888949.000000001, Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN};
        for (final double b : doubles) {
            encode(b, Double.TYPE);
        }
        Assert.assertEquals(doubles.length * Double.SIZE / Byte.SIZE, buffer.readableBytes());
        for (final double b : doubles) {
            Assert.assertTrue(decode(Double.TYPE).equals(b));
        }
    }
}
