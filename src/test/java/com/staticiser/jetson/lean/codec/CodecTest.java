package com.staticiser.jetson.lean.codec;

import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Type;
import org.junit.Before;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class CodecTest {

    final Codec codec;
    final Codecs codecs;
    final ByteBuf buffer;

    CodecTest(final Codec codec) {

        this(codec, new Codecs());
    }

    private CodecTest(final Codec codec, final Codecs codecs) {

        this.codec = codec;
        this.codecs = codecs;
        buffer = Unpooled.buffer();
    }

    @Before
    public void setUp() throws Exception {

        buffer.clear();
    }

    void encode(final Object value) throws RPCException {

        encode(value, value.getClass());
    }

    void encode(final Object value, final Type type) throws RPCException {

        codec.encode(value, buffer, codecs, type);
    }

    <T> T decode(final Class<T> type) throws RPCException {

        return decode((Type) type);
    }

    <T> T decode(final Type type) throws RPCException {

        return codec.decode(buffer, codecs, type);
    }
}
