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
package uk.ac.standrews.cs.jetson;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundMessageHandlerAdapter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.standrews.cs.jetson.exception.InternalException;
import uk.ac.standrews.cs.jetson.exception.TransportException;
import uk.ac.standrews.cs.jetson.util.CloseableUtil;
import uk.ac.standrews.cs.jetson.util.JsonGeneratorUtil;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

@Sharable
class RequestEncoder extends ChannelOutboundMessageHandlerAdapter<Request> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestEncoder.class);
    private final JsonFactory json_factory;
    private final JsonEncoding encoding;

    RequestEncoder(final JsonFactory json_factory) {

        this(json_factory, JsonEncoding.UTF8);
    }

    RequestEncoder(final JsonFactory json_factory, final JsonEncoding encoding) {

        this.json_factory = json_factory;
        this.encoding = encoding;
    }

    @Override
    public void flush(final ChannelHandlerContext context, final Request request) throws Exception {

        JsonGenerator generator = null;
        try {
            generator = createJsonGenerator(context);
            generator.writeStartObject();
            generator.writeObjectField(Message.VERSION_KEY, request.getVersion());
            generator.writeObjectField(Request.METHOD_NAME_KEY, request.getMethodName());
            writeRequestParameters(request, generator);
            generator.writeObjectField(Message.ID_KEY, request.getId());
            generator.writeEndObject();
            writeFrameDelimiter(generator);
            generator.flush();
            generator.close();
        }
        catch (final JsonProcessingException e) {
            LOGGER.debug("failed to encode request", e);
            throw new InternalException(e);
        }
        catch (final IOException e) {
            LOGGER.debug("IO error occured while encoding request", e);
            throw new TransportException(e);
        }
        finally {
            CloseableUtil.closeQuietly(generator);
        }
    }

    private JsonGenerator createJsonGenerator(final ChannelHandlerContext context) throws IOException {

        final ByteBufOutputStream out = new ByteBufOutputStream(context.nextOutboundByteBuffer());
        return json_factory.createGenerator(out, encoding);
    }

    static void writeFrameDelimiter(final JsonGenerator generator) throws IOException {

        generator.writeRaw(FrameDecoder.FRAME_DELIMITER_AS_STRING);
    }

    private static void writeRequestParameters(final Request request, final JsonGenerator generator) throws IOException {

        final Method target_method = request.getMethod();
        final Type[] param_types = target_method.getGenericParameterTypes();
        JsonGeneratorUtil.writeValuesAs(generator, Request.PARAMETERS_KEY, param_types, request.getParameters());
    }
}
