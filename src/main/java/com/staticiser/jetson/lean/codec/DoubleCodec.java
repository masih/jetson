package com.staticiser.jetson.lean.codec;

import io.netty.buffer.ByteBuf;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class DoubleCodec extends PrimitiveTypeCodec {

    protected DoubleCodec() {

        super(Double.class, Double.TYPE);
    }

    @Override
    protected Object readValue(final ByteBuf in) {

        return in.readDouble();
    }

    @Override
    protected void writeValue(final ByteBuf out, final Object value) {

        out.writeDouble((Double) value);
    }
}
