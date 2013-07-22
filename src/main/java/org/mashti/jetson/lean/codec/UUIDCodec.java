package org.mashti.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.util.UUID;
import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class UUIDCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && UUID.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeBoolean(true);
        }
        else {
            UUID uuid = (UUID) value;
            out.writeBoolean(false);
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        }
    }

    @Override
    public UUID decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final boolean is_null = in.readBoolean();
        if (is_null) { return null; }
        final long most_significant_bits = in.readLong();
        final long least_significant_bits = in.readLong();

        return new UUID(most_significant_bits, least_significant_bits);
    }
}
