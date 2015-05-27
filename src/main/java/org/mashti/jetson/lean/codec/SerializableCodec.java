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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.util.CloseableUtil;
import org.mashti.jetson.util.ReflectionUtil;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SerializableCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        final Class<?> class_type = ReflectionUtil.getRawClass(type);
        return class_type != null && Serializable.class.isAssignableFrom(class_type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        ObjectOutputStream object_out = null;
        try {
            object_out = new ObjectOutputStream(new ByteBufOutputStream(out));
            object_out.writeObject(value);
            object_out.flush();
        }
        catch (final Exception e) {
            throw new RPCException(e);
        }
        finally {
            CloseableUtil.closeQuietly(object_out);
        }
    }

    @Override
    public Serializable decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        ObjectInputStream object_in = null;
        try {
            object_in = new ObjectInputStream(new ByteBufInputStream(in));
            return (Serializable) object_in.readObject();
        }
        catch (final Exception e) {
            throw new RPCException(e);
        }
        finally {
            CloseableUtil.closeQuietly(object_in);
        }
    }
}
