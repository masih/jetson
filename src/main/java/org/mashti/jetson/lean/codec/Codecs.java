package org.mashti.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.UnknownTypeException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Codecs {

    public static final InetAddressCodec INET_ADDRESS_CODEC = new InetAddressCodec();
    public static final InetSocketAddressCodec INET_SOCKET_ADDRESS_CODEC = new InetSocketAddressCodec();
    private static final ArrayCodec ARRAY_CODEC = new ArrayCodec();
    private static final VoidCodec VOID_CODEC = new VoidCodec();
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
    private static final ObjectCodec OBJECT_CODEC = new ObjectCodec();
    private final List<Codec> codecs;

    public Codecs() {

        codecs = new ArrayList<Codec>();
        registerDefaultCodecs();
    }

    public synchronized boolean register(final Codec codec) {

        return !codecs.contains(codec) && codecs.add(codec);
    }

    public synchronized boolean register(final int index, final Codec codec) {

        if (!codecs.contains(codec)) {
            codecs.add(index, codec);
            return true;
        }
        return false;
    }

    public boolean isSupported(final Type type) {

        for (final Codec codec : codecs) {
            if (codec.isSupported(type)) { return true; }
        }
        return false;
    }

    public synchronized void encodeAs(final Object value, final ByteBuf out, final Type type) throws RPCException {

        get(type).encode(value, out, this, type);
    }

    public synchronized <Value> Value decodeAs(final ByteBuf in, final Type type) throws RPCException {

        return get(type).decode(in, this, type);
    }

    synchronized Codec get(final Type type) throws UnknownTypeException {

        for (final Codec codec : codecs) {
            if (codec != null && codec.isSupported(type)) { return codec; }
        }

        throw new UnknownTypeException(type);
    }

    void registerDefaultCodecs() {

        register(VOID_CODEC);
        register(BYTE_CODEC);
        register(SHORT_CODEC);
        register(INTEGER_CODEC);
        register(LONG_CODEC);
        register(FLOAT_CODEC);
        register(DOUBLE_CODEC);
        register(CHARACTER_CODEC);
        register(STRING_CODEC);
        register(BOOLEAN_CODEC);
        register(INET_ADDRESS_CODEC);
        register(INET_SOCKET_ADDRESS_CODEC);
        register(THROWABLE_CODEC);
        register(ARRAY_CODEC);
        register(SERIALIZABLE_CODEC);
        register(OBJECT_CODEC);
    }
}