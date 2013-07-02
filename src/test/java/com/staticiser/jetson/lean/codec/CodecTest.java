package com.staticiser.jetson.lean.codec;

import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Type;
import org.junit.Before;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class CodecTest {

    protected final Codec codec;
    protected final Codecs codecs;
    protected final ByteBuf buffer;

    protected CodecTest(final Codec codec) {

        this(codec, new Codecs());
    }

    protected CodecTest(final Codec codec, final Codecs codecs) {

        this.codec = codec;
        this.codecs = codecs;
        buffer = Unpooled.buffer();
    }

    @Before
    public void setUp() throws Exception {

        buffer.clear();
    }

    protected void encode(final Object value) throws RPCException {

        encode(value, value.getClass());
    }

    protected void encode(final Object value, final Type type) throws RPCException {

        codec.encode(value, buffer, codecs, type);
    }

    protected <T> T decode(Class<T> type) throws RPCException {

        return decode((Type) type);
    }

    protected <T> T decode(Type type) throws RPCException {

        return codec.decode(buffer, codecs, type);
    }
}
