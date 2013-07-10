package com.staticiser.jetson.lean.codec;

import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.exception.UnknownTypeException;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ObjectCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && Object.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeBoolean(true);
        }
        else {
            out.writeBoolean(false);
            final Class<?> runtime_type = value.getClass();
            if (runtime_type.equals(Object.class)) { throw new UnknownTypeException("type Object at runtime is not supported"); }
            codecs.encodeAs(runtime_type.getName(), out, String.class);
            codecs.encodeAs(value, out, value.getClass());
        }
    }

    @Override
    public Object decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final boolean is_null = in.readBoolean();
        if (is_null) { return null; }

        final String value_type_as_string = codecs.decodeAs(in, String.class);
        final Class<?> value_type;
        try {
            value_type = Class.forName(value_type_as_string);
        }
        catch (ClassNotFoundException e) {
            throw new RPCException(e);
        }
        return codecs.decodeAs(in, value_type);
    }
}
