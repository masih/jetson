package com.staticiser.jetson.lean;

import com.staticiser.jetson.Request;
import com.staticiser.jetson.RequestEncoder;
import com.staticiser.jetson.exception.MethodNotFoundException;
import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanRequestEncoder extends RequestEncoder {

    private final List<Method> dispatch;
    private final MarshallerRegistry marshallers;

    public LeanRequestEncoder(final List<Method> dispatch, MarshallerRegistry marshallers) {
        this.dispatch = dispatch;
        this.marshallers = marshallers;
    }

    @Override
    protected void encodeRequest(final ChannelHandlerContext context, final Request request, final ByteBuf out) throws RPCException {

        final Method method = request.getMethod();
        final Object[] arguments = request.getArguments();
        final Type[] argument_types = method.getGenericParameterTypes();
        out.writeInt(request.getId());
        writeMethod(method, out);
        writeArguments(arguments, argument_types, out);
    }

    private void writeArguments(final Object[] arguments, final Type[] argument_types, final ByteBuf out) throws RPCException {
        for (int i = 0; i < argument_types.length; i++) {

            final Type type = argument_types[i];
            final Object argument = arguments[i];
            final Marshaller marshaller = marshallers.get(type);
            if (marshaller != null) {
                marshaller.write(argument, out);
            }
            else {
                throw new RPCException("no marshaller found for type " + type);
            }
        }
    }

    private void writeMethod(final Method method, final ByteBuf out) throws MethodNotFoundException {
        final int index = dispatch.indexOf(method);
        if (index == -1) { throw new MethodNotFoundException();}
        out.writeByte(index);
    }
}
