/**
 * This file is part of jetson.
 *
 * jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jetson.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mashti.jetson.lean.codec;

import java.io.IOException;
import java.io.Serializable;
import org.junit.Assert;
import org.junit.Test;
import org.mashti.jetson.exception.RPCException;

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

    private static void assertThrowableEquality(final Throwable throwable, final Throwable decoded_throwable) {

        if (throwable != null) {
            Assert.assertEquals(throwable.getClass(), decoded_throwable.getClass());
            Assert.assertEquals(throwable.getMessage(), decoded_throwable.getMessage());
        }
        else {
            Assert.assertSame(throwable, decoded_throwable);
        }
    }
}
