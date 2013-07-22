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
