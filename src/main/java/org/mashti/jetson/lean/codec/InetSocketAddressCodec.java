package org.mashti.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class InetSocketAddressCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && InetSocketAddress.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeBoolean(true);
        }
        else {
            out.writeBoolean(false);
            final InetSocketAddress socket_address = (InetSocketAddress) value;
            final InetAddress address = socket_address.getAddress();
            codecs.encodeAs(address, out, InetAddress.class);

            final int port = socket_address.getPort();
            encodePort(out, port);
        }
    }

    @Override
    public InetSocketAddress decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final boolean is_null = in.readBoolean();
        if (is_null) { return null; }
        final InetAddress address = codecs.decodeAs(in, InetAddress.class);
        final int port = decodePort(in);

        return new InetSocketAddress(address, port);
    }

    private int decodePort(final ByteBuf in) {

        return in.readUnsignedShort();
    }

    private void encodePort(final ByteBuf out, final int port) {

        out.writeShort(port);
    }

}
