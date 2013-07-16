package org.mashti.jetson.lean.codec;

import io.netty.buffer.ByteBuf;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class BooleanCodec extends PrimitiveTypeCodec {

    BooleanCodec() {

        super(Boolean.class, Boolean.TYPE);
    }

    @Override
    protected Object readValue(final ByteBuf in) {

        return in.readBoolean();
    }

    @Override
    protected void writeValue(final ByteBuf out, final Object value) {

        out.writeBoolean((Boolean) value);
    }
}
