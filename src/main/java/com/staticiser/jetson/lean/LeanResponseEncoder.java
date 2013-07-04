package com.staticiser.jetson.lean;

import com.staticiser.jetson.ResponseEncoder;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.lean.codec.Codecs;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanResponseEncoder extends ResponseEncoder {

    private final Codecs codecs;

    public LeanResponseEncoder(Codecs codecs) {

        this.codecs = codecs;
    }

    @Override
    protected void encodeResult(final ChannelHandlerContext context, final Integer id, final Object result, final Method method, final ByteBuf out) throws RPCException {

        out.writeInt(id);
        out.writeBoolean(false);
        final Type return_type = method.getGenericReturnType();
        codecs.encodeAs(result, out, return_type);
    }

    @Override
    protected void encodeException(final ChannelHandlerContext context, final Integer id, final Throwable exception, final ByteBuf out) throws RPCException {

        out.writeInt(id);
        out.writeBoolean(true);
        codecs.encodeAs(exception, out, Throwable.class);
    }
}
