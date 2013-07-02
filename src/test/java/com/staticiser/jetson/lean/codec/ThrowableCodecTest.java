package com.staticiser.jetson.lean.codec;

import com.staticiser.jetson.exception.RPCException;
import java.io.IOException;
import java.io.Serializable;
import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ThrowableCodecTest extends CodecTest {

    public ThrowableCodecTest() {

        super(new ThrowableCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(IOException.class));
        Assert.assertTrue(codec.isSupported(Exception.class));
        Assert.assertTrue(codec.isSupported(Throwable.class));
        Assert.assertFalse(codec.isSupported(Serializable.class));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final Throwable[] throwables = {new IOException(), new RPCException("Some Message"), new RuntimeException("some message", new IOException()), new Exception(new RPCException()), null};
        for (final Throwable throwable : throwables) {
            encode(throwable, Throwable.class);
            final Throwable decoded_throwable = decode(Throwable.class);
            if (throwable != null) {
                final Throwable throwable_cause = throwable.getCause();
                final Throwable decoded_throwable_cause = decoded_throwable.getCause();
                assertThrowableEquality(throwable, decoded_throwable);
                assertThrowableEquality(throwable_cause, decoded_throwable_cause);
            }
        }
    }

    private void assertThrowableEquality(final Throwable throwable, final Throwable decoded_throwable) {

        if (throwable != null && decoded_throwable != null) {
            Assert.assertEquals(throwable.getClass(), decoded_throwable.getClass());
            Assert.assertEquals(throwable.getMessage(), decoded_throwable.getMessage());
        }
        else {
            Assert.assertTrue(throwable == decoded_throwable);
        }
    }
}
