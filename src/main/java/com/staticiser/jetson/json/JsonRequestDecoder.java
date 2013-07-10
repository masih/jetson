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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.staticiser.jetson.RequestDecoder;
import com.staticiser.jetson.exception.InvalidRequestException;
import com.staticiser.jetson.exception.MethodNotFoundException;
import com.staticiser.jetson.exception.ParseException;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.exception.TransportException;
import com.staticiser.jetson.util.CloseableUtil;
import com.staticiser.jetson.util.JsonParserUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
class JsonRequestDecoder extends RequestDecoder {

    private static final AttributeKey<JsonParser> PARSER_ATTRIBUTE_KEY = new AttributeKey<JsonParser>("parser");
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonRequestDecoder.class);
    private final Map<String, Method> dispatch;
    private final JsonFactory json_factory;

    JsonRequestDecoder(final JsonFactory json_factory, final Map<String, Method> dispatch) {

        this.json_factory = json_factory;
        this.dispatch = dispatch;
    }

    @Override
    protected void beforeDecode(final ChannelHandlerContext context, final ByteBuf buffer) throws TransportException {

        super.beforeDecode(context, buffer);
        final Channel channel = context.channel();
        final ByteBufInputStream in = new ByteBufInputStream(buffer);
        try {
            final JsonParser parser = json_factory.createParser(in);
            parser.nextToken();
            channel.attr(PARSER_ATTRIBUTE_KEY).set(parser);
        }
        catch (IOException e) {
            throw new TransportException(e);
        }
    }

    @Override
    protected Integer decodeId(final ChannelHandlerContext context, final ByteBuf buffer) throws RPCException {

        final JsonParser parser = context.channel().attr(PARSER_ATTRIBUTE_KEY).get();
        try {
            return readId(parser);
        }
        catch (final JsonParseException e) {
            LOGGER.debug("failed to parse request", e);
            throw new InvalidRequestException(e);
        }
        catch (final JsonGenerationException e) {
            LOGGER.debug("failed to generate request", e);
            throw new ParseException(e);
        }
        catch (final IOException e) {
            LOGGER.debug("IO error occurred while decoding response", e);
            throw new TransportException(e);
        }
    }

    @Override
    protected Method decodeMethod(final ChannelHandlerContext context, final ByteBuf buffer) throws RPCException {

        final JsonParser parser = context.channel().attr(PARSER_ATTRIBUTE_KEY).get();
        try {
            readAndValidateVersion(parser);
            final String method_name = readAndValidateMethodName(parser);
            return findServiceMethodByName(method_name);
        }
        catch (final JsonParseException e) {
            LOGGER.debug("failed to parse request", e);
            throw new InvalidRequestException(e);
        }
        catch (final JsonGenerationException e) {
            LOGGER.debug("failed to generate request", e);
            throw new ParseException(e);
        }
        catch (final IOException e) {
            LOGGER.debug("IO error occurred while decoding response", e);
            throw new TransportException(e);
        }
    }

    @Override
    protected Object[] decodeMethodArguments(final ChannelHandlerContext context, final ByteBuf buffer, final Method method) throws RPCException {

        final JsonParser parser = context.channel().attr(PARSER_ATTRIBUTE_KEY).get();
        try {
            final Object[] arguments = readArguments(parser, method);

            parser.nextToken();
            return arguments;
        }
        catch (final JsonParseException e) {
            LOGGER.debug("failed to parse request", e);
            throw new InvalidRequestException(e);
        }
        catch (final JsonGenerationException e) {
            LOGGER.debug("failed to generate request", e);
            throw new ParseException(e);
        }
        catch (final IOException e) {
            LOGGER.debug("IO error occurred while decoding response", e);
            throw new TransportException(e);
        }
    }

    @Override
    protected void afterDecode(final ChannelHandlerContext context, final ByteBuf in) {

        super.afterDecode(context, in);
        final JsonParser parser = context.channel().attr(PARSER_ATTRIBUTE_KEY).get();
        CloseableUtil.closeQuietly(parser);
    }

    private Integer readId(final JsonParser parser) throws IOException {

        final Integer id = JsonParserUtil.readFieldValueAs(parser, JsonRequestEncoder.ID_KEY, Integer.class);
        if (id == null) { throw new InvalidRequestException("request id of null is not supported"); }
        return id;
    }

    private String readAndValidateMethodName(final JsonParser parser) throws IOException {

        final String method_name = JsonParserUtil.readFieldValueAs(parser, JsonRequestEncoder.METHOD_NAME_KEY, String.class);
        if (method_name == null) { throw new InvalidRequestException("method name cannot be null"); }
        return method_name;
    }

    private String readAndValidateVersion(final JsonParser parser) throws IOException {

        final String version = JsonParserUtil.readFieldValueAs(parser, JsonRequestEncoder.VERSION_KEY, String.class);
        if (version == null || !version.equals(JsonRequestEncoder.DEFAULT_VERSION)) { throw new InvalidRequestException("version must be equal to " + JsonRequestEncoder.DEFAULT_VERSION); }
        return version;
    }

    private Object[] readArguments(final JsonParser parser, final Method method) throws IOException {

        JsonParserUtil.expectFieldName(parser, JsonRequestEncoder.PARAMETERS_KEY);
        final Type[] param_types = method.getGenericParameterTypes();
        return JsonParserUtil.readArrayValuesAs(parser, param_types);
    }

    private Method findServiceMethodByName(final String method_name) throws MethodNotFoundException {

        if (!dispatch.containsKey(method_name)) { throw new MethodNotFoundException(); }
        return dispatch.get(method_name);
    }
}
