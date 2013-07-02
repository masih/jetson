package com.staticiser.jetson.lean.codec;

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
            encode(b);
        }
        Assert.assertEquals(ints.length * 32 / 8, buffer.readableBytes());
        for (final int b : ints) {
            Assert.assertTrue(decode(Integer.TYPE).equals(b));
        }
    }
}
