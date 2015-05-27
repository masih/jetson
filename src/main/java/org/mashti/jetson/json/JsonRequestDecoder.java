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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
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
import org.mashti.jetson.RequestDecoder;
import org.mashti.jetson.exception.InvalidRequestException;
import org.mashti.jetson.exception.MethodNotFoundException;
import org.mashti.jetson.exception.ParseException;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.TransportException;
import org.mashti.jetson.util.CloseableUtil;
import org.mashti.jetson.util.JsonParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
class JsonRequestDecoder extends RequestDecoder {

    private static final AttributeKey<JsonParser> PARSER_ATTRIBUTE_KEY = AttributeKey.valueOf("parser");
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
