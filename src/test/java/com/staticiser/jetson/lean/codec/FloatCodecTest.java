package com.staticiser.jetson.lean.codec;

import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class FloatCodecTest extends CodecTest {

    public FloatCodecTest() {

        super(new FloatCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Float.class));
        Assert.assertTrue(codec.isSupported(Float.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final float[] floats = {15.9f, 64565465465.54654654f, -888949.000000001f, Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN};
        for (final float b : floats) {
            encode(b);
        }
        Assert.assertEquals(floats.length * 32 / 8, buffer.readableBytes());
        for (final float b : floats) {
            Assert.assertTrue(decode(Float.TYPE).equals(b));
        }
    }
}
