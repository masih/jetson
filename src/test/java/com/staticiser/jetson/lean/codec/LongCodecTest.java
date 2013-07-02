package com.staticiser.jetson.lean.codec;

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

        final long[] longs = {54654654, -899999999999L, -88, Long.MAX_VALUE, Long.MIN_VALUE, 0L};
        for (final long b : longs) {
            encode(b);
        }
        Assert.assertEquals(longs.length * 64 / 8, buffer.readableBytes());
        for (final long b : longs) {
            Assert.assertTrue(decode(Long.TYPE).equals(b));
        }
    }
}
