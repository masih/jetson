package com.staticiser.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class CharacterCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {
        return type == Character.class || type == Character.TYPE;
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) {
        out.writeChar((Character) value);
    }

    @Override
    public Character decode(final ByteBuf in, final Codecs codecs, final Type type) {
        return in.readChar();
    }
}
