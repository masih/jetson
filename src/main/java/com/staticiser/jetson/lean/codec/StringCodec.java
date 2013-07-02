package com.staticiser.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class StringCodec implements Codec {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private final Charset charset;

    StringCodec() {

        this(DEFAULT_CHARSET);
    }

    StringCodec(final Charset charset) {

        this.charset = charset;
    }

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && String.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) {

        if (value == null) {
            out.writeInt(-1);
        }
        else {
            final byte[] bytes = ((String) value).getBytes(charset);
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        }
    }

    @Override
    public String decode(final ByteBuf in, final Codecs codecs, final Type type) {

        final int size = in.readInt();
        if (size < 0) { return null; }
        final byte[] bytes = new byte[size];
        in.readBytes(bytes);
        return new String(bytes, charset);
    }
}
