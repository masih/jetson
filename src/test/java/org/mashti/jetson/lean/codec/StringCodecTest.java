package org.mashti.jetson.lean.codec;

import java.io.Serializable;
import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class StringCodecTest extends CodecTest {

    public StringCodecTest() {

        super(new StringCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(String.class));
        Assert.assertFalse(codec.isSupported(Serializable.class));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final String[] strings = {"", null, " ", "Some text"};
        for (final String b : strings) {
            encode(b, String.class);
            Assert.assertEquals(b, decode(String.class));
        }
    }
}
