package com.staticiser.jetson.lean.codec;

import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class CharacterCodecTest extends CodecTest {

    public CharacterCodecTest() {

        super(new CharacterCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Character.class));
        Assert.assertTrue(codec.isSupported(Character.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final char[] chars = {' ', '@', 'a', '!', '\u02DA', '\u02FA'};
        for (final char b : chars) {
            encode(b);
        }
        // Assume UTF-8 encoding: 2 bytes for each character
        Assert.assertEquals(chars.length * 2, buffer.readableBytes());
        for (final char b : chars) {
            Assert.assertTrue(b == decode(Character.TYPE));
        }
    }
}
