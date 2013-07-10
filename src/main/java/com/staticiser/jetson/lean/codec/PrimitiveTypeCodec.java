package com.staticiser.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
abstract class PrimitiveTypeCodec implements Codec {

    private final Class<?> primitive_class;
    private final Type primitive_type;

    PrimitiveTypeCodec(final Class<?> primitive_class, final Type primitive_type) {

        this.primitive_class = primitive_class;
        this.primitive_type = primitive_type;
    }

    @Override
    public boolean isSupported(final Type type) {

        return type == primitive_type || type == primitive_class;
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) {

        if (CodecUtils.isPrimitive(type)) {
            writeValue(out, value);
        }
        else {
            final boolean null_value = value == null;
            out.writeBoolean(null_value);
            if (!null_value) {
                writeValue(out, value);
            }
        }
    }

    @Override
    public Object decode(final ByteBuf in, final Codecs codecs, final Type type) {

        if (CodecUtils.isPrimitive(type)) {
            return readValue(in);
        }
        else {
            if (!in.readBoolean()) { return readValue(in); }
            return null;
        }
    }

    protected abstract Object readValue(final ByteBuf in);

    protected abstract void writeValue(ByteBuf out, Object value);
}
