package org.mashti.jetson.lean.codec;

import org.junit.Assert;
import org.junit.Test;
import org.mashti.jetson.exception.UnknownTypeException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ObjectCodecTest extends CodecTest {

    public ObjectCodecTest() {

        super(new ObjectCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Object.class));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Integer.TYPE));
        Assert.assertTrue(codec.isSupported(Integer.class));
        Assert.assertTrue(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final Object[] longs = {54654654, -899999999999L, 0, null, "some text", '!'};
        for (final Object b : longs) {
            encode(b, Object.class);
            if (b == null) {
                Assert.assertTrue(decode(Object.class) == b);
            }
            else {
                Assert.assertTrue(decode(Object.class).equals(b));
            }
        }
    }

    @Test(expected = UnknownTypeException.class)
    public void testCodecFailure() throws Exception {

        encode(new Object(), Object.class);
    }
}
