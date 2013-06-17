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
package com.staticiser.jetson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.staticiser.jetson.exception.InternalException;
import com.staticiser.jetson.exception.InvalidJsonException;
import com.staticiser.jetson.exception.InvalidResponseException;
import com.staticiser.jetson.exception.JsonRpcException;
import com.staticiser.jetson.exception.TransportException;
import com.staticiser.jetson.exception.UnexpectedException;
import com.staticiser.jetson.util.CloseableUtil;
import com.staticiser.jetson.util.JsonParserUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.io.IOException;
import java.lang.reflect.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
class ResponseDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseDecoder.class);
    private final JsonFactory json_factory;

    ResponseDecoder(final JsonFactory json_factory) {

        this.json_factory = json_factory;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf msg, final MessageList<Object> out) throws Exception {
        out.add(decode(ctx, msg));
    }

    protected Response decode(final ChannelHandlerContext ctx, final ByteBuf msg) throws JsonRpcException {

        final Response response = ctx.channel().attr(ResponseHandler.RESPONSE_ATTRIBUTE).get();
        final Request request = ctx.channel().attr(ResponseHandler.REQUEST_ATTRIBUTE).get();
        if (request == null) { throw new UnexpectedException("unexpected response to a request that does not exist"); }
        final long expected_response_id = request.getId();
        final Type expected_return_type = request.getMethod().getGenericReturnType();

        JsonParser parser = null;
        try {
            parser = json_factory.createParser(new ByteBufInputStream(msg));
            parser.nextToken();
            setResponseVersion(parser, response);
            setResponseResultOrError(parser, response, expected_return_type);
            vaildateAndSetResponseId(parser, response, expected_response_id);
            parser.nextToken();
            return response;
        }
        catch (final JsonParseException e) {
            LOGGER.debug("failed to parse response", e);
            throw new InvalidJsonException(e);
        }
        catch (final JsonGenerationException e) {
            LOGGER.debug("failed to generate response", e);
            throw new InternalException(e);
        }
        catch (final JsonProcessingException e) {
            LOGGER.debug("failed to decode response", e);
            throw new InvalidResponseException(e);
        }
        catch (final IOException e) {
            LOGGER.debug("IO error occured while decoding response", e);
            throw new TransportException("failed to process response", e);
        }
        finally {
            CloseableUtil.closeQuietly(parser);
        }
    }

    private void vaildateAndSetResponseId(final JsonParser parser, final Response response, final Long expected_id) throws IOException {

        final Long id = JsonParserUtil.readFieldValueAs(parser, Message.ID_KEY, Long.class);
        validateResponseId(expected_id, id);
        response.setId(id);
    }

    private void validateResponseId(final Long expected_id, final Long id) throws InvalidResponseException {

        if (id == null || !id.equals(expected_id)) { throw InvalidResponseException.fromMessage("response id must not be null, and must be equal to ", expected_id); }
    }

    private void setResponseResultOrError(final JsonParser parser, final Response response, final Type expected_return_type) throws IOException {

        final String next_field_name = JsonParserUtil.expectFieldNames(parser, Response.RESULT_KEY, Response.ERROR_KEY);
        if (Response.ERROR_KEY.equals(next_field_name)) {
            setResponseError(parser, response);
        }
        else {
            setResponseResult(parser, response, expected_return_type);
        }
    }

    private void setResponseResult(final JsonParser parser, final Response response, final Type expected_return_type) throws IOException {

        final Object result = JsonParserUtil.readValueAs(parser, expected_return_type);
        response.setResult(result);
    }

    private void setResponseError(final JsonParser parser, final Response response) throws IOException {

        final JsonRpcException error = JsonParserUtil.readValueAs(parser, JsonRpcException.class);
        if (error == null) { throw new InvalidResponseException("error in response must not be null"); }
        response.setError(error);
    }

    private void setResponseVersion(final JsonParser parser, final Response response) throws IOException {

        final String version = JsonParserUtil.readFieldValueAs(parser, Message.VERSION_KEY, String.class);
        vaidateVersion(version);
        response.setVersion(version);
    }

    private void vaidateVersion(final String version) throws InvalidResponseException {

        if (version == null || !version.equals(Message.DEFAULT_VERSION)) {
            LOGGER.debug("expected JSON RPC {}, but recieved ", Message.DEFAULT_VERSION, version);
            throw InvalidResponseException.fromMessage("version must be equal to ", Message.DEFAULT_VERSION);
        }
    }
}
