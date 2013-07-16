package org.mashti.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class InetAddressCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && InetAddress.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeBoolean(true);
        }
        else {
            out.writeBoolean(false);
            final InetAddress address = (InetAddress) value;
            final byte[] address_bytes = address.getAddress();
            codecs.encodeAs(address_bytes, out, byte[].class);
        }
    }

    @Override
    public InetAddress decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final boolean is_null = in.readBoolean();
        if (is_null) { return null; }

        final byte[] address_bytes = codecs.decodeAs(in, byte[].class);

        try {
            return InetAddress.getByAddress(address_bytes);
        }
        catch (UnknownHostException e) {
            throw new RPCException("failed to decode address bytes", e);
        }
    }
}
