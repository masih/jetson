package com.staticiser.jetson.lean;

import com.staticiser.jetson.RequestEncoder;
import com.staticiser.jetson.exception.MethodNotFoundException;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.lean.codec.Codecs;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanRequestEncoder extends RequestEncoder {

    private final List<Method> dispatch;
    private final Codecs codecs;

    public LeanRequestEncoder(final List<Method> dispatch, Codecs codecs) {

        this.dispatch = dispatch;
        this.codecs = codecs;
    }

    @Override
    protected void encodeRequest(final ChannelHandlerContext context, final Integer id, final Method method, final Object[] arguments, final ByteBuf out) throws RPCException {

        final Type[] argument_types = method.getGenericParameterTypes();
        out.writeInt(id);
        writeMethod(method, out);
        writeArguments(arguments, argument_types, out);
    }

    private void writeArguments(final Object[] arguments, final Type[] argument_types, final ByteBuf out) throws RPCException {

        for (int i = 0; i < argument_types.length; i++) {

            final Type type = argument_types[i];
            final Object argument = arguments[i];
            codecs.encodeAs(argument, out, type);
        }
    }

    private void writeMethod(final Method method, final ByteBuf out) throws MethodNotFoundException {

        final int index = dispatch.indexOf(method);
        if (index == -1) { throw new MethodNotFoundException(); }
        out.writeByte(index);
    }
}
