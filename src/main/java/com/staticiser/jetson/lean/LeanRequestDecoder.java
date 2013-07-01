package com.staticiser.jetson.lean;

import com.staticiser.jetson.Request;
import com.staticiser.jetson.RequestDecoder;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.lean.codec.Codecs;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanRequestDecoder extends RequestDecoder {

    private final List<Method> dispatch;
    private final Codecs marshallers;

    public LeanRequestDecoder(List<Method> dispatch, Codecs marshallers) {

        this.dispatch = dispatch;
        this.marshallers = marshallers;
    }

    @Override
    protected Request decode(final ChannelHandlerContext context, final ByteBuf in) throws RPCException {

        final Request request = new Request(); //TODO cache
        final int id = in.readInt();
        request.setId(id);

        final int method_index = in.readByte();
        final Method method = dispatch.get(method_index);
        request.setMethod(method);

        final Type[] argument_types = method.getGenericParameterTypes();
        final Object[] arguments = readArguments(argument_types, in);
        request.setArguments(arguments);

        return request;
    }

    private Object[] readArguments(final Type[] argument_types, final ByteBuf in) throws RPCException {

        final int arguments_count = argument_types.length;
        final Object[] arguments;
        if (arguments_count > 0) {

            arguments = new Object[arguments_count];
            for (int i = 0; i < arguments_count; i++) {
                arguments[i] = marshallers.decodeAs(in, argument_types[i]);
            }
        }
        else {
            arguments = null;
        }
        return arguments;
    }
}
