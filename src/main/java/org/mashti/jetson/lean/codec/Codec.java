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

/**
 * Encodes/Decodes an object to/from {@link ByteBuf byte buffer}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface Codec {

    /**
     * Checks if this codec is capable of encoding/decoding the given {@code type}.
     *
     * @param type the type to check
     * @return whether the value is accepted by this codec
     */
    boolean isSupported(Type type);

    /**
     * Encodes the given {@code value} into the given byte buffer.
     *
     * @param value the value to encode
     * @param out the buffer to write the encoded value into
     * @param codecs the available codecs
     * @throws RPCException Signals that an error has occurred during encoding
     */
    void encode(Object value, ByteBuf out, Codecs codecs, Type type) throws RPCException;

    /**
     * Decodes a value from the given byte buffer
     *
     * @param in the byte buffer to decode a value from
     * @param codecs the available codecs
     * @return the decoded value
     * @throws RPCException Signals that an error has occurred during decoding
     */
    <Value> Value decode(ByteBuf in, Codecs codecs, Type type) throws RPCException;
}
