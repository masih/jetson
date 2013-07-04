package com.staticiser.jetson.lean;

import com.staticiser.jetson.FutureResponse;
import com.staticiser.jetson.ResponseEncoder;
import com.staticiser.jetson.exception.InternalServerException;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.lean.codec.Codecs;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanResponseEncoder extends ResponseEncoder {

    private final Codecs codecs;

    public LeanResponseEncoder(Codecs codecs) {

        this.codecs = codecs;
    }

    @Override
    protected void encodeResponse(final ChannelHandlerContext context, final FutureResponse response, final ByteBuf out) throws RPCException {

        out.writeInt(response.getId());

        try {
            final Object result = response.get();
            out.writeBoolean(false);
            if (!response.getMethod().getReturnType().equals(Void.TYPE)) { //FIXME null encoding
                final Type return_type = response.getMethod().getGenericReturnType();
                codecs.encodeAs(result, out, return_type);
            }
        }
        catch (InterruptedException e) {
            out.writeBoolean(true);
            codecs.encodeAs(new InternalServerException(e), out, Throwable.class);

        }
        catch (ExecutionException e) {
            out.writeBoolean(true);
            codecs.encodeAs(e.getCause(), out, Throwable.class);
        }
    }
}
