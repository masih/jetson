package com.staticiser.jetson.lean.codec;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ArrayCodecTest extends CodecTest {

    private static final Class<?> NULL_ARRAY_CLASS = new String[]{}.getClass();

    public ArrayCodecTest() {

        super(new ArrayCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(new Object[]{}.getClass()));
        Assert.assertTrue(codec.isSupported(new int[]{}.getClass()));
        Assert.assertTrue(codec.isSupported(new byte[]{}.getClass()));
        Assert.assertTrue(codec.isSupported(Array.newInstance(Integer.TYPE, 5, 5).getClass()));
        Assert.assertFalse(codec.isSupported("".getClass()));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(new ArrayList().getClass().getGenericSuperclass()));
        Assert.assertFalse(codec.isSupported(new ArrayList().getClass()));
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

        for (Object[] value : arrays) {
            final Class<?> value_type = value != null ? value.getClass() : NULL_ARRAY_CLASS;
            encode(value, value_type);
            final Object[] decoded_value = (Object[]) decode(value_type);
            Assert.assertArrayEquals(value, decoded_value);
        }
    }
}
