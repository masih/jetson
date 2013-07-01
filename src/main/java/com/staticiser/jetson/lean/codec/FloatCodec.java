package com.staticiser.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class FloatCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {
        return type == Float.class || type == Float.TYPE;
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) {
        out.writeFloat((Float) value);
    }

    @Override
    public Float decode(final ByteBuf in, final Codecs codecs, final Type type) {
        return in.readFloat();
    }
}
