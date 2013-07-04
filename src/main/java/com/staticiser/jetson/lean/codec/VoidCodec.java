package com.staticiser.jetson.lean.codec;

import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class VoidCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return Void.TYPE.equals(type) || Void.class.equals(type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        // write nothing
    }

    @Override
    public <Value> Value decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        // read nothing; and return null
        return null;
    }
}
