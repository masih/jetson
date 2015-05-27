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
import org.mashti.jetson.util.ReflectionUtil;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
abstract class PrimitiveTypeCodec implements Codec {

    private final Class<?> primitive_class;
    private final Type primitive_type;

    PrimitiveTypeCodec(final Class<?> primitive_class, final Type primitive_type) {

        this.primitive_class = primitive_class;
        this.primitive_type = primitive_type;
    }

    @Override
    public boolean isSupported(final Type type) {

        return type == primitive_type || type == primitive_class;
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) {

        if (ReflectionUtil.isPrimitive(type)) {
            writeValue(out, value);
        }
        else {
            final boolean null_value = value == null;
            out.writeBoolean(null_value);
            if (!null_value) {
                writeValue(out, value);
            }
        }
    }

    @Override
    public Object decode(final ByteBuf in, final Codecs codecs, final Type type) {

        if (ReflectionUtil.isPrimitive(type)) {
            return readValue(in);
        }
        else {
            if (!in.readBoolean()) { return readValue(in); }
            return null;
        }
    }

    protected abstract Object readValue(final ByteBuf in);

    protected abstract void writeValue(ByteBuf out, Object value);
}
