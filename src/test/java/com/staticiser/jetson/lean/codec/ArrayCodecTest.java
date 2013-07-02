package com.staticiser.jetson.lean.codec;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ArrayCodecTest extends CodecTest {

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
        Assert.assertFalse(codec.isSupported(new ArrayList().getClass()));
    }

    @Test
    public void testCodecIntegers() throws Exception {

        final int[] value = {1, 2, 3, 4, 5};
        final int value_length = value.length;
        encode(value);
        Assert.assertEquals(4 * (value_length + 1), buffer.readableBytes());
        assertEncodedArrayLength(value_length);
        final int[] decoded_value = decode(value.getClass());
        Assert.assertTrue(Arrays.equals(value, decoded_value));
    }

    @Test
    public void testCodecStrings() throws Exception {

        final String[] value = {"AAA", null, null, null, ""};
        final int value_length = value.length;
        encode(value);
        assertEncodedArrayLength(value_length);
        final String[] decoded_value = decode(value.getClass());
        Assert.assertTrue(Arrays.equals(value, decoded_value));
    }

    private void assertEncodedArrayLength(final int length) {

        Assert.assertEquals(length, buffer.getInt(0));
    }
}
