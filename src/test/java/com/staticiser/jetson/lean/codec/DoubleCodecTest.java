package com.staticiser.jetson.lean.codec;

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
