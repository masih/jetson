package com.staticiser.jetson.lean.codec;

import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class ByteCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type == Byte.class || type == Byte.TYPE;
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        out.writeByte((Byte) value);
    }

    @Override
    public Byte decode(final ByteBuf in, final Codecs codecs, final Type type) {

        return in.readByte();
    }
}
