package com.staticiser.jetson.lean;

import com.staticiser.jetson.FutureResponse;
import com.staticiser.jetson.RequestDecoder;
import com.staticiser.jetson.exception.MethodNotFoundException;
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
    private final Codecs codecs;
    private final int dispatch_size;

    public LeanRequestDecoder(List<Method> dispatch, Codecs codecs) {

        this.dispatch = dispatch;
        this.codecs = codecs;
        dispatch_size = dispatch.size();
    }

    @Override
    protected void decodeAndSetIdMethodArguments(final ChannelHandlerContext context, final ByteBuf in, final FutureResponse future_response) throws RPCException {

        final int id = in.readInt();
        future_response.setId(id);

        final int method_index = in.readByte();
        final Method method = getMethodByIndex(method_index);
        future_response.setMethod(method);

        final Type[] argument_types = method.getGenericParameterTypes();
        final Object[] arguments = readArguments(argument_types, in);
        future_response.setArguments(arguments);

    }

    private Method getMethodByIndex(final int index) throws MethodNotFoundException {

        final Method method = isInRange(index) ? dispatch.get(index) : null;
        if (method == null) { throw new MethodNotFoundException("no method is found with the index: " + index); }
        return method;
    }

    private boolean isInRange(final int index) {

        return index > -1 && index < dispatch_size;
    }

    private Object[] readArguments(final Type[] argument_types, final ByteBuf in) throws RPCException {

        final int arguments_count = argument_types.length;
        final Object[] arguments;
        if (arguments_count > 0) {

            arguments = new Object[arguments_count];
            for (int i = 0; i < arguments_count; i++) {
                arguments[i] = codecs.decodeAs(in, argument_types[i]);
            }
        }
        else {
            arguments = null;
        }
        return arguments;
    }
}
