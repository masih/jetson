package org.mashti.jetson.lean;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import org.mashti.jetson.RequestDecoder;
import org.mashti.jetson.exception.MethodNotFoundException;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.codec.Codecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanRequestDecoder extends RequestDecoder {

    private final List<Method> dispatch;
    private final Codecs codecs;
    private final int dispatch_size;

    public LeanRequestDecoder(final List<Method> dispatch, final Codecs codecs) {

        this.dispatch = dispatch;
        this.codecs = codecs;
        dispatch_size = dispatch.size();
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

    @Override
    protected Integer decodeId(final ChannelHandlerContext context, final ByteBuf in) throws RPCException {

        return in.readInt();
    }

    @Override
    protected Method decodeMethod(final ChannelHandlerContext context, final ByteBuf in) throws RPCException {

        final int method_index = in.readByte();
        return getMethodByIndex(method_index);
    }

    @Override
    protected Object[] decodeMethodArguments(final ChannelHandlerContext context, final ByteBuf in, final Method method) throws RPCException {

        final Type[] argument_types = method.getGenericParameterTypes();
        return readArguments(argument_types, in);
    }
}
