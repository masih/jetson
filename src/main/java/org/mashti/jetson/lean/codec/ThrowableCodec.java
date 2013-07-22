package org.mashti.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class ThrowableCodec extends SerializableCodec {

    public static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && Throwable.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        final Throwable throwable = (Throwable) value;
        skipStackTrace(throwable);
        super.encode(value, out, codecs, type);
    }

    @Override
    public Throwable decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        return (Throwable) super.decode(in, codecs, type);
    }

    private void skipStackTrace(final Throwable value) {

        if (value != null) {
            value.setStackTrace(EMPTY_STACK_TRACE);
        }
    }
}
