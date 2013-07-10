package com.staticiser.jetson.lean.codec;

import io.netty.buffer.ByteBuf;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class ByteCodec extends PrimitiveTypeCodec {

    ByteCodec() {

        super(Byte.class, Byte.TYPE);
    }

    @Override
    protected Object readValue(final ByteBuf in) {

        return in.readByte();
    }

    @Override
    protected void writeValue(final ByteBuf out, final Object value) {

        out.writeByte((Byte) value);
    }
}
