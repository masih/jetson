package com.staticiser.jetson.lean.codec;

import io.netty.buffer.ByteBuf;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class FloatCodec extends PrimitiveTypeCodec {

    protected FloatCodec() {

        super(Float.class, Float.TYPE);
    }

    @Override
    protected Object readValue(final ByteBuf in) {

        return in.readFloat();
    }

    @Override
    protected void writeValue(final ByteBuf out, final Object value) {

        out.writeFloat((Float) value);
    }
}
