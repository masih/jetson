package org.mashti.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class ThrowableCodec extends SerializableCodec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && Throwable.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value != null) {
            ((Throwable) value).setStackTrace(new StackTraceElement[0]); //Skip stack trace
        }
        super.encode(value, out, codecs, type);
    }

    @Override
    public Throwable decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        return (Throwable) super.decode(in, codecs, type);
    }
}
