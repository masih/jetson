package com.staticiser.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class DoubleCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {
        return type == Double.class || type == Double.TYPE;
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) {
        out.writeDouble((Double) value);
    }

    @Override
    public Double decode(final ByteBuf in, final Codecs codecs, final Type type) {
        return in.readDouble();
    }
}
