package com.staticiser.jetson.lean;

import com.staticiser.jetson.Response;
import com.staticiser.jetson.ResponseEncoder;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.lean.codec.Codecs;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanResponseEncoder extends ResponseEncoder {

    private final Codecs codecs;

    public LeanResponseEncoder(Codecs codecs) {
        this.codecs = codecs;
    }

    @Override
    protected void encodeResponse(final ChannelHandlerContext context, final Response response, final ByteBuf out) throws RPCException {

        out.writeInt(response.getId());
        final boolean error = response.isError();
        out.writeBoolean(error);

        if (error) {
            codecs.encodeAs(response.getException(), out, Throwable.class);
        }
        else if (response.getResult() != null) {

            Type return_type = response.getResult().getClass(); //FIXME
            codecs.encodeAs(response.getResult(), out, return_type);
        }
    }
}
