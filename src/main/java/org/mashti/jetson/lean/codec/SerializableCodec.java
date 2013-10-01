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
