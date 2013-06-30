/*
 * Copyright 2013 Masih Hajiarabderkani
 *
 * This file is part of Jetson.
 *
 * Jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Jetson.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.staticiser.jetson.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.staticiser.jetson.Response;
import com.staticiser.jetson.ResponseEncoder;
import com.staticiser.jetson.exception.InternalServerException;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.exception.TransportException;
import com.staticiser.jetson.util.CloseableUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
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

    JsonResponseEncoder(final JsonFactory json_factory, final JsonEncoding encoding) {

        this.json_factory = json_factory;
        this.encoding = encoding;
    }

    @Override
    protected void encodeResponse(final ChannelHandlerContext context, final Response response, final ByteBuf out) throws RPCException {

        // TODO use tokenbuffer

        JsonGenerator generator = null;
        try {
            generator = createJsonGenerator(out);
            generator.writeStartObject();
            generator.writeObjectField(JsonRequestEncoder.VERSION_KEY, JsonRequestEncoder.DEFAULT_VERSION);
            generator.writeObjectField(JsonRequestEncoder.ID_KEY, response.getId());
            writeResultOrError(response, generator);
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

    private JsonGenerator createJsonGenerator(final ByteBuf buffer) throws IOException {

        final ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        return json_factory.createGenerator(out, encoding);
    }

    private static void writeResultOrError(final Response response, final JsonGenerator generator) throws IOException {

        if (!response.isError()) {
            generator.writeObjectField(RESULT_KEY, response.getResult());
        }
        else {

            final JsonRpcError error = JsonRpcExceptions.toJsonRpcError(response.getException());
            generator.writeObjectField(ERROR_KEY, error);
        }
    }
}