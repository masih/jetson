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
import com.staticiser.jetson.RequestEncoder;
import com.staticiser.jetson.exception.InternalServerException;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.exception.TransportException;
import com.staticiser.jetson.util.CloseableUtil;
import com.staticiser.jetson.util.JsonGeneratorUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
class JsonRequestEncoder extends RequestEncoder {

    static final String ID_KEY = "id";
    static final String VERSION_KEY = "jsonrpc";
    static final String DEFAULT_VERSION = "2.0";
    static final String PARAMETERS_KEY = "params";
    static final String METHOD_NAME_KEY = "method";
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonRequestEncoder.class);
    private final JsonFactory json_factory;
    private final JsonEncoding encoding;
    private final Map<Method, String> dispatch;

    JsonRequestEncoder(final JsonFactory json_factory, final Map<Method, String> dispatch) {

        this(json_factory, JsonEncoding.UTF8, dispatch);
    }

    JsonRequestEncoder(final JsonFactory json_factory, final JsonEncoding encoding, final Map<Method, String> dispatch) {

        this.json_factory = json_factory;
        this.encoding = encoding;
        this.dispatch = dispatch;
    }

    @Override
    protected void encodeRequest(final ChannelHandlerContext context, final Integer id, final Method method, final Object[] arguments, final ByteBuf out) throws RPCException {

        JsonGenerator generator = null;
        try {
            generator = createJsonGenerator(out);
            generator.writeStartObject();
            generator.writeObjectField(ID_KEY, id);
            generator.writeObjectField(VERSION_KEY, DEFAULT_VERSION);
            generator.writeObjectField(METHOD_NAME_KEY, dispatch.get(method));
            writeRequestParameters(method, arguments, generator);
            generator.writeEndObject();
            generator.flush();
            generator.close();
        }
        catch (final JsonProcessingException e) {

            LOGGER.debug("failed to encode request", e);
            throw new InternalServerException(e);
        }
        catch (final IOException e) {
            LOGGER.debug("IO error occurred while encoding request", e);
            throw new TransportException(e);
        }
        finally {
            CloseableUtil.closeQuietly(generator);
        }
    }

    private static void writeRequestParameters(final Method method, Object[] arguments, final JsonGenerator generator) throws IOException {

        final Type[] param_types = method.getGenericParameterTypes();
        JsonGeneratorUtil.writeValuesAs(generator, PARAMETERS_KEY, param_types, arguments);
    }

    private synchronized JsonGenerator createJsonGenerator(final ByteBuf buffer) throws IOException {

        final ByteBufOutputStream out = new ByteBufOutputStream(buffer);

        return json_factory.createGenerator(out, encoding);
    }

}
