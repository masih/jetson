package org.mashti.jetson.lean.codec;

import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class VoidCodecTest extends CodecTest {

    public VoidCodecTest() {

        super(new VoidCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Void.class));
        Assert.assertTrue(codec.isSupported(Void.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final Object[] objects = {true, new Object(), Void.class, null};
        for (final Object o : objects) {
            encode(o, Void.TYPE);
        }
        Assert.assertEquals(0, buffer.readableBytes());
        for (final Object o : objects) {
            Assert.assertNull(codec.decode(buffer, codecs, Void.TYPE));
        }
    }
}
