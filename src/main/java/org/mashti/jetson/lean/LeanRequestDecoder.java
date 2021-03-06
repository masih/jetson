/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
    protected final Codecs codecs;

    public LeanRequestDecoder(final List<Method> dispatch, final Codecs codecs) {

        this.dispatch = dispatch;
        this.codecs = codecs;
    }

    private Method getMethodByIndex(final int index) throws MethodNotFoundException {

        try {
            return dispatch.get(index);
        }
        catch (final IndexOutOfBoundsException e) {
            throw new MethodNotFoundException("no method is found with the index: " + index, e);
        }
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
