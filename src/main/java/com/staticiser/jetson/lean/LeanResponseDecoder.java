package com.staticiser.jetson.lean;

import com.staticiser.jetson.Request;
import com.staticiser.jetson.Response;
import com.staticiser.jetson.ResponseDecoder;
import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanResponseDecoder extends ResponseDecoder {

    private final MarshallerRegistry marshallers;

    public LeanResponseDecoder(MarshallerRegistry marshallers) {
        this.marshallers = marshallers;
    }

    @Override
    protected Response decode(final ChannelHandlerContext context, final ByteBuf in) throws RPCException {

        final Response response = new Response();
        final int id = in.readInt();
        response.setId(id);
        final boolean error = in.readBoolean();
        if (error) {
            final Throwable throwable = (Throwable) marshallers.get(Throwable.class).read(in);
            response.setException(throwable);
        }
        else {
            final Request request = getClient(context).getPendingRequestById(id);
            final Method method = request.getMethod();
            if (!method.getReturnType().equals(Void.TYPE)) {
                final Type return_type = method.getGenericReturnType();
                final Object result = marshallers.get(return_type).read(in);
                response.setResult(result);
            }
        }

        return response;
    }
}
