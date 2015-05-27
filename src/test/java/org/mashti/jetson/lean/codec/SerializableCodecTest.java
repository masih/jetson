/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mashti.jetson.lean.codec;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Test;
import org.mashti.jetson.exception.RPCException;

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

    @Test(expected = RPCException.class)
    public void testSerializableWithNonSerializableFields() throws Exception {

        final SerializableWithNonSerializableFields obj = new SerializableWithNonSerializableFields();
        encode(obj);
        Assert.assertTrue(obj.equals(decode(Serializable.class)));
    }

    @Test(expected = RPCException.class)
    public void testCorruptSerializable() throws Exception {

        final CorruptSerializable obj = new CorruptSerializable();
        encode(obj);
        Assert.assertTrue(obj.equals(decode(Serializable.class)));
    }

    private static class SerializableWithNonSerializableFields implements Serializable {

        private final Object non_serializable_field;

        private SerializableWithNonSerializableFields() {

            non_serializable_field = new Object();
        }
    }

    private static class CorruptSerializable implements Serializable {

        private void writeObject(java.io.ObjectOutputStream out) throws IOException {

        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

            throw new IOException("intentional error");
        }

        private void readObjectNoData() throws ObjectStreamException {

        }

    }

}
