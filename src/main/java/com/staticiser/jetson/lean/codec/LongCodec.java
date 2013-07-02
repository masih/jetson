package com.staticiser.jetson.lean.codec;

import io.netty.buffer.ByteBuf;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class LongCodec extends PrimitiveTypeCodec {

    protected LongCodec() {

        super(Long.class, Long.TYPE);
    }

    @Override
    protected Object readValue(final ByteBuf in) {

        return in.readLong();
    }

    @Override
    protected void writeValue(final ByteBuf out, final Object value) {

        out.writeLong((Long) value);
    }
}
