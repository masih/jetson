package com.staticiser.jetson.lean.codec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SerializableCodecTest extends CodecTest {

    public SerializableCodecTest() {

        super(new SerializableCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Long.class));
        Assert.assertTrue(codec.isSupported(String.class));
        Assert.assertTrue(codec.isSupported(Throwable.class));
        Assert.assertTrue(codec.isSupported(ArrayList.class));
        Assert.assertTrue(codec.isSupported(HashMap.class));
        Assert.assertTrue(codec.isSupported(HashSet.class));
        Assert.assertTrue(codec.isSupported(Serializable.class));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final Serializable[] serializables = {54654654, -899999999999L, -88, Long.MAX_VALUE, Long.MIN_VALUE, 0L, "Some Text", (Serializable) Arrays.asList("an Element", 65535, Long.MAX_VALUE, Double.NaN, new ArrayList()), new HashMap<Object, Object>(), this.getClass()};
        for (final Serializable b : serializables) {
            encode(b);
        }
        for (final Serializable b : serializables) {
            Assert.assertTrue(b.equals(decode(Serializable.class)));
        }
    }
}
