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
package org.mashti.jetson.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.lang.reflect.Method;
import org.mashti.jetson.ResponseEncoder;
import org.mashti.jetson.exception.InternalServerException;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.TransportException;
import org.mashti.jetson.util.CloseableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
class JsonResponseEncoder extends ResponseEncoder {

    static final String RESULT_KEY = "result";
    static final String ERROR_KEY = "error";
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonResponseEncoder.class);
    private final JsonFactory json_factory;
    private final JsonEncoding encoding;

    JsonResponseEncoder(final JsonFactory json_factory) {

        this(json_factory, JsonEncoding.UTF8);
    }

    private JsonResponseEncoder(final JsonFactory json_factory, final JsonEncoding encoding) {

        this.json_factory = json_factory;
        this.encoding = encoding;
    }

    private JsonGenerator createJsonGenerator(final ByteBuf buffer) throws IOException {

        final ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        return json_factory.createGenerator(out, encoding);
    }

    @Override
    protected void encodeResult(final ChannelHandlerContext context, final Integer id, final Object result, final Method method, final ByteBuf out) throws RPCException {

        encodeResultOrException(id, result, null, out, false);
    }

    @Override
    protected void encodeException(final ChannelHandlerContext context, final Integer id, final Throwable exception, final ByteBuf out) throws RPCException {

        encodeResultOrException(id, null, exception, out, true);
    }

    void encodeResultOrException(final Integer id, final Object result, final Throwable exception, final ByteBuf out, final boolean error) throws RPCException {

        JsonGenerator generator = null;
        try {
            generator = createJsonGenerator(out);
            generator.writeStartObject();
            generator.writeObjectField(JsonRequestEncoder.ID_KEY, id);
            generator.writeObjectField(JsonRequestEncoder.VERSION_KEY, JsonRequestEncoder.DEFAULT_VERSION);
            if (error) {
                final JsonRpcError json_rpc_error = JsonRpcExceptions.toJsonRpcError(exception);
                generator.writeObjectField(ERROR_KEY, json_rpc_error);
            }
            else {
                generator.writeObjectField(RESULT_KEY, result);
            }
            generator.writeEndObject();
            generator.flush();
            generator.close();
        }
        catch (final JsonProcessingException e) {
            LOGGER.debug("failed to encode response", e);
            throw new InternalServerException(e);
        }
        catch (final IOException e) {
            LOGGER.debug("IO error occurred while encoding response", e);
            throw new TransportException(e);
        }
        finally {
            CloseableUtil.closeQuietly(generator);
        }
    }
}
