package com.staticiser.jetson.lean.codec;

import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.exception.UnknownTypeException;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Codecs {

    private static final StringCodec STRING_CODEC = new StringCodec();
    private static final BooleanCodec BOOLEAN_CODEC = new BooleanCodec();
    private static final ThrowableCodec THROWABLE_CODEC = new ThrowableCodec();
    private static final CharacterCodec CHARACTER_CODEC = new CharacterCodec();
    private static final IntegerCodec INTEGER_CODEC = new IntegerCodec();
    private static final ByteCodec BYTE_CODEC = new ByteCodec();
    private static final ShortCodec SHORT_CODEC = new ShortCodec();
    private static final LongCodec LONG_CODEC = new LongCodec();
    private static final FloatCodec FLOAT_CODEC = new FloatCodec();
    private static final DoubleCodec DOUBLE_CODEC = new DoubleCodec();
    private static final SerializableCodec SERIALIZABLE_CODEC = new SerializableCodec();
    private final List<Codec> codecs;

    public Codecs() {
        codecs = new ArrayList<Codec>();
        registerDefaultCodecs();
    }

    public synchronized boolean register(Codec codec) {

        return !codecs.contains(codec) && codecs.add(codec);
    }

    public boolean isSupported(final Type type) {
        for (Codec codec : codecs) {
            if (codec.isSupported(type)) { return true; }
        }
        return false;
    }

    public synchronized void encodeAs(final Object value, final ByteBuf out, Type type) throws RPCException {
        get(type).encode(value, out, this, type);
    }

    public synchronized <Value> Value decodeAs(final ByteBuf in, Type type) throws RPCException {
        return get(type).decode(in, this, type);
    }

    protected synchronized Codec get(Type type) throws UnknownTypeException {

        for (Codec codec : codecs) {
            if (codec.isSupported(type)) { return codec; }
        }

        throw new UnknownTypeException(type);
    }

    protected void registerDefaultCodecs() {
        register(BYTE_CODEC);
        register(SHORT_CODEC);
        register(INTEGER_CODEC);
        register(LONG_CODEC);
        register(FLOAT_CODEC);
        register(DOUBLE_CODEC);
        register(CHARACTER_CODEC);
        register(STRING_CODEC);
        register(BOOLEAN_CODEC);
        register(THROWABLE_CODEC);
        register(SERIALIZABLE_CODEC);
    }
}
