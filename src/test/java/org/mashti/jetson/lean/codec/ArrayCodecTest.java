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
