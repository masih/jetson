package com.staticiser.jetson.lean.codec;

import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class BooleanCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type == Boolean.class || type == Boolean.TYPE;
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {
        out.writeBoolean((Boolean) value);
    }

    @Override
    public Boolean decode(final ByteBuf in, final Codecs codecs, final Type type) {
        return in.readBoolean();
    }
}
