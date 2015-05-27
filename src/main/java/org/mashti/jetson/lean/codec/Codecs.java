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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.UnknownTypeException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Codecs {

    public static final InetAddressCodec INET_ADDRESS_CODEC = new InetAddressCodec();
    public static final InetSocketAddressCodec INET_SOCKET_ADDRESS_CODEC = new InetSocketAddressCodec();
    private static final ArrayCodec ARRAY_CODEC = new ArrayCodec();
    private static final VoidCodec VOID_CODEC = new VoidCodec();
    private static final StringCodec STRING_CODEC = new StringCodec();
    private static final BooleanCodec BOOLEAN_CODEC = new BooleanCodec();
    private static final ThrowableCodec THROWABLE_CODEC = new ThrowableCodec();
    private static final CharacterCodec CHARACTER_CODEC = new CharacterCodec();
    private static final IntegerCodec INTEGER_CODEC = new IntegerCodec();
    private static final ByteCodec BYTE_CODEC = new ByteCodec();
    private static final ShortCodec SHORT_CODEC = new ShortCodec();
    private static final LongCodec LONG_CODEC = new LongCodec();
    private static final FloatCodec FLOAT_CODEC = new FloatCodec();
    private static final DoubleCodec DOUBLE_CODEC = new DoubleCodec();
    private static final SerializableCodec SERIALIZABLE_CODEC = new SerializableCodec();
    private static final ObjectCodec OBJECT_CODEC = new ObjectCodec();
    private static final UUIDCodec UUID_CODEC = new UUIDCodec();
    private final ConcurrentHashMap<Type, Codec> cached_codec_mapping = new ConcurrentHashMap<Type, Codec>();
    private final List<Codec> codecs;

    public Codecs() {

        codecs = new ArrayList<Codec>();
        registerDefaultCodecs();
    }

    public synchronized boolean register(final Codec codec) {

        return !codecs.contains(codec) && codecs.add(codec);
    }

    public synchronized boolean register(final int index, final Codec codec) {

        if (!codecs.contains(codec)) {
            codecs.add(index, codec);
            return true;
        }
        return false;
    }

    public boolean isSupported(final Type type) {

        for (final Codec codec : codecs) {
            if (codec.isSupported(type)) { return true; }
        }
        return false;
    }

    public void encodeAs(final Object value, final ByteBuf out, final Type type) throws RPCException {

        get(type).encode(value, out, this, type);
    }

    public <Value> Value decodeAs(final ByteBuf in, final Type type) throws RPCException {

        return get(type).decode(in, this, type);
    }

    Codec get(final Type type) throws UnknownTypeException {

        if (isCodecMappingCached(type)) {
            return getCachedCodecMapping(type);
        }

        final Iterator<Codec> codecs_iterator = codecs.iterator();
        while (codecs_iterator.hasNext()) {
            Codec codec = codecs_iterator.next();
            if (codec != null && codec.isSupported(type)) {
                cacheCodecMapping(type, codec);
                return codec;
            }
        }

        throw new UnknownTypeException(type);
    }

    private Codec getCachedCodecMapping(final Type type) {

        return cached_codec_mapping.get(type);
    }

    private boolean isCodecMappingCached(final Type type) {

        return cached_codec_mapping.containsKey(type);
    }

    private void cacheCodecMapping(final Type type, final Codec codec) {

        cached_codec_mapping.putIfAbsent(type, codec);
    }

    void registerDefaultCodecs() {

        register(VOID_CODEC);
        register(BYTE_CODEC);
        register(SHORT_CODEC);
        register(INTEGER_CODEC);
        register(LONG_CODEC);
        register(FLOAT_CODEC);
        register(DOUBLE_CODEC);
        register(CHARACTER_CODEC);
        register(STRING_CODEC);
        register(BOOLEAN_CODEC);
        register(UUID_CODEC);
        register(INET_ADDRESS_CODEC);
        register(INET_SOCKET_ADDRESS_CODEC);
        register(THROWABLE_CODEC);
        register(ARRAY_CODEC);
        register(SERIALIZABLE_CODEC);
        register(OBJECT_CODEC);
    }
}
