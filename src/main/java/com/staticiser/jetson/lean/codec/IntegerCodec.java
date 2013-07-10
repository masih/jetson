package com.staticiser.jetson.lean.codec;

import io.netty.buffer.ByteBuf;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class IntegerCodec extends PrimitiveTypeCodec {

    IntegerCodec() {

        super(Integer.class, Integer.TYPE);
    }

    @Override
    protected Object readValue(final ByteBuf in) {

        return in.readInt();
    }

    @Override
    protected void writeValue(final ByteBuf out, final Object value) {

        out.writeInt((Integer) value);
    }
}
