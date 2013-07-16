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
