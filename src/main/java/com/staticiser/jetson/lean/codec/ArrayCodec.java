package com.staticiser.jetson.lean.codec;

import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Array;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class ArrayCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && ((Class<?>) type).isArray();
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeInt(-1);
        }
        else {
            if (!value.getClass().isArray()) {
                System.out.println("SSSSS");
            }
            final int length = Array.getLength(value);
            out.writeInt(length);
            final Class<?> component_type = ((Class) type).getComponentType();
            for (int i = 0; i < length; i++) {
                final Object element = Array.get(value, i);
                codecs.encodeAs(element, out, component_type);
            }
        }
    }

    @Override
    public Object decode(final ByteBuf in, final Codecs codecs, final Type expected_type) throws RPCException {

        final int length = in.readInt();
        if (length < 0) { return null; }
        final Class<?> expected_class = (Class<?>) expected_type;
        final Class<?> component_type = expected_class.getComponentType();
        final Object result = Array.newInstance(component_type, length);

        for (int i = 0; i < length; i++) {
            Array.set(result, i, codecs.decodeAs(in, component_type));
        }
        return result;
    }
}
