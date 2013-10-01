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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ArrayCodecTest extends CodecTest {

    private static final Class<?> NULL_ARRAY_CLASS = String[].class;

    public ArrayCodecTest() {

        super(new ArrayCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Object[].class));
        Assert.assertTrue(codec.isSupported(int[].class));
        Assert.assertTrue(codec.isSupported(byte[].class));
        Assert.assertTrue(codec.isSupported(Array.newInstance(Integer.TYPE, 5, 5).getClass()));
        Assert.assertFalse(codec.isSupported("".getClass()));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(ArrayList.class.getGenericSuperclass()));
        Assert.assertFalse(codec.isSupported(ArrayList.class));
    }

    @Test
    public void testCodec() throws Exception {

        final List<Object[]> arrays = new ArrayList<Object[]>();
        arrays.add(new Integer[]{1, 2, 3, 4, 5, Integer.MAX_VALUE});
        arrays.add(new String[]{"AAA", null, null, null, ""});
        arrays.add(new String[]{"AAA", null, null, null, ""});
        arrays.add(new String[][]{{"aa", "bb", null}, {"ZZ", null, "DD"}});
        arrays.add(new Long[]{});
        arrays.add(null);

        for (final Object[] value : arrays) {
            final Class<?> value_type = value != null ? value.getClass() : NULL_ARRAY_CLASS;
            encode(value, value_type);
            final Object[] decoded_value = (Object[]) decode(value_type);
            Assert.assertArrayEquals(value, decoded_value);
        }
    }
}
