package com.staticiser.jetson.lean.codec;

import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.util.CloseableUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SerializableCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && Serializable.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        ObjectOutputStream object_out = null;
        try {
            object_out = new ObjectOutputStream(new ByteBufOutputStream(out));
            object_out.writeObject(value);
            object_out.flush();
        }
        catch (final IOException e) {
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
        catch (final IOException e) {
            throw new RPCException(e);
        }
        catch (final ClassNotFoundException e) {
            throw new RPCException(e);
        }
        finally {
            CloseableUtil.closeQuietly(object_in);
        }
    }
}