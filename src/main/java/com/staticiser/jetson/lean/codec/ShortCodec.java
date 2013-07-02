package com.staticiser.jetson.lean.codec;

import io.netty.buffer.ByteBuf;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class ShortCodec extends PrimitiveTypeCodec {

    protected ShortCodec() {

        super(Short.class, Short.TYPE);
    }

    @Override
    protected Object readValue(final ByteBuf in) {

        return in.readShort();
    }

    @Override
    protected void writeValue(final ByteBuf out, final Object value) {

        out.writeShort((Short) value);
    }
}
