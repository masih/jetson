package com.staticiser.jetson.lean.codec;

import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class BooleanCodecTest extends CodecTest {

    public BooleanCodecTest() {

        super(new BooleanCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Boolean.class));
        Assert.assertTrue(codec.isSupported(Boolean.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodecPrimitive() throws Exception {

        final boolean[] booleans = {true, false, false, true};
        for (final boolean b : booleans) {
            encode(b, Boolean.TYPE);
        }
        Assert.assertEquals(booleans.length, buffer.readableBytes());
        for (final boolean b : booleans) {
            Assert.assertTrue(decode(Boolean.TYPE) == b);
        }
    }

    @Test
    public void testCodec() throws Exception {

        final Boolean[] booleans = {true, false, null, true};
        for (final Boolean b : booleans) {
            encode(b, Boolean.class);
        }
        // The number of written bytes is 2 for each non-null boolean and 1 for each null boolean
        Assert.assertEquals(booleans.length * 2 - 1, buffer.readableBytes());
        for (final Boolean b : booleans) {
            Assert.assertEquals(b, decode(Boolean.class));
        }
    }
}
