package com.staticiser.jetson.lean;

import com.staticiser.jetson.Response;
import com.staticiser.jetson.ResponseEncoder;
import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanResponseEncoder extends ResponseEncoder {

    private final MarshallerRegistry marshallers;

    public LeanResponseEncoder(MarshallerRegistry marshallers) {
        this.marshallers = marshallers;
    }

    @Override
    protected void encodeResponse(final ChannelHandlerContext context, final Response response, final ByteBuf out) throws RPCException {

        out.writeInt(response.getId());
        final boolean error = response.isError();
        out.writeBoolean(error);

        if (error) {
            marshallers.get(Throwable.class).write(response.getException(), out);
        }
        else if (response.getResult() != null) {

            Type return_type = response.getResult().getClass(); //FIXME
            marshallers.get(return_type).write(response.getResult(), out);
        }
    }
}
