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
package org.mashti.jetson.lean.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class ThrowableCodec extends SerializableCodec {

    public static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && Throwable.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        final Throwable throwable = (Throwable) value;
        skipStackTrace(throwable);
        super.encode(value, out, codecs, type);
    }

    @Override
    public Throwable decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        return (Throwable) super.decode(in, codecs, type);
    }

    private void skipStackTrace(final Throwable value) {

        if (value != null) {
            value.setStackTrace(EMPTY_STACK_TRACE);
        }
    }
}
