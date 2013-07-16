package org.mashti.jetson.lean.codec;

import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ByteCodecTest extends CodecTest {

    //FIXME separate primitives from Objects

    public ByteCodecTest() {

        super(new ByteCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Byte.class));
        Assert.assertTrue(codec.isSupported(Byte.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final byte[] bytes = {0, -52, 127, -127};
        for (final byte b : bytes) {
            encode(b, Byte.TYPE);
        }
        Assert.assertEquals(bytes.length, buffer.readableBytes());
        for (final byte b : bytes) {
            Assert.assertTrue(b == decode(Byte.TYPE));
        }
    }
}
