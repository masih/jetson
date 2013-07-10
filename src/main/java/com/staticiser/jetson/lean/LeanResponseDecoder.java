package com.staticiser.jetson.lean;

import com.staticiser.jetson.FutureResponse;
import com.staticiser.jetson.ResponseDecoder;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.lean.codec.Codecs;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanResponseDecoder extends ResponseDecoder {

    private final Codecs codecs;

    public LeanResponseDecoder(final Codecs codecs) {

        this.codecs = codecs;
    }

    @Override
    protected FutureResponse decode(final ChannelHandlerContext context, final ByteBuf in) {

        final int id = in.readInt();
        final FutureResponse response = getFutureResponseById(context, id);
        final boolean error = in.readBoolean();
        try {
            if (error) {
                final Throwable throwable = codecs.decodeAs(in, Throwable.class);
                response.setException(throwable);
            }
            else {
                final Method method = response.getMethod();
                final Type return_type = method.getGenericReturnType();
                final Object result = codecs.decodeAs(in, return_type);
                response.set(result);
            }
        }
        catch (RPCException e) {
            response.setException(e);
        }
        return response;
    }
}
