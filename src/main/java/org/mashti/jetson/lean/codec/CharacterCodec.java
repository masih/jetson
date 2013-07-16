package org.mashti.jetson.lean.codec;

import io.netty.buffer.ByteBuf;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class CharacterCodec extends PrimitiveTypeCodec {

    CharacterCodec() {

        super(Character.class, Character.TYPE);
    }

    @Override
    protected Object readValue(final ByteBuf in) {

        return in.readChar();
    }

    @Override
    protected void writeValue(final ByteBuf out, final Object value) {

        out.writeChar((Character) value);
    }
}
