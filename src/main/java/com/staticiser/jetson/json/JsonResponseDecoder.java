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
import com.staticiser.jetson.FutureResponse;
import com.staticiser.jetson.ResponseDecoder;
import com.staticiser.jetson.exception.InternalServerException;
import com.staticiser.jetson.exception.InvalidResponseException;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.exception.TransportException;
import com.staticiser.jetson.util.CloseableUtil;
import com.staticiser.jetson.util.JsonParserUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.lang.reflect.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class JsonResponseDecoder extends ResponseDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonResponseDecoder.class);
    private final JsonFactory json_factory;

    JsonResponseDecoder(final JsonFactory json_factory) {

        this.json_factory = json_factory;
    }

    protected FutureResponse decode(final ChannelHandlerContext context, final ByteBuf in) throws RPCException {

        JsonParser parser = null;
        try {
            parser = json_factory.createParser(new ByteBufInputStream(in));
            parser.nextToken();
            final Integer id = validateAndReadResponseId(parser);
            final FutureResponse future_response = getClient(context).getFutureResponseById(id);
            // FIXME cover for unknown IDs
            readAndValidateVersion(parser);
            final Type expected_return_type = future_response.getMethod().getGenericReturnType();
            setResponseResultOrError(parser, future_response, expected_return_type);
            parser.nextToken();
            return future_response;
        }
        catch (final JsonParseException e) {
            LOGGER.debug("failed to parse response", e);
            throw new InvalidResponseException(e);
        }
        catch (final JsonGenerationException e) {
            LOGGER.debug("failed to generate response", e);
            throw new InternalServerException(e);
        }
        catch (final IOException e) {
            LOGGER.debug("IO error occurred while decoding response", e);
            throw new TransportException("failed to process response", e);
        }
        finally {
            CloseableUtil.closeQuietly(parser);
        }
    }

    private Integer validateAndReadResponseId(final JsonParser parser) throws IOException {

        return JsonParserUtil.readFieldValueAs(parser, JsonRequestEncoder.ID_KEY, Integer.class);
    }

    private void setResponseResultOrError(final JsonParser parser, final FutureResponse response, final Type expected_return_type) throws IOException {

        final String next_field_name = JsonParserUtil.expectFieldNames(parser, JsonResponseEncoder.RESULT_KEY, JsonResponseEncoder.ERROR_KEY);
        if (JsonResponseEncoder.ERROR_KEY.equals(next_field_name)) {
            setResponseError(parser, response);
        }
        else {
            setResponseResult(parser, response, expected_return_type);
        }
    }

    private void setResponseResult(final JsonParser parser, final FutureResponse response, final Type expected_return_type) throws IOException {

        final Object result = JsonParserUtil.readValueAs(parser, expected_return_type);
        response.setResult(result);
    }

    private void setResponseError(final JsonParser parser, final FutureResponse response) throws IOException {

        final JsonRpcError error = JsonParserUtil.readValueAs(parser, JsonRpcError.class);
        if (error == null) { throw new InvalidResponseException("error in response must not be null"); }

        final Throwable throwable = JsonRpcExceptions.fromJsonRpcError(error);
        response.setException(throwable);
    }

    private void readAndValidateVersion(final JsonParser parser) throws IOException {

        final String version = JsonParserUtil.readFieldValueAs(parser, JsonRequestEncoder.VERSION_KEY, String.class);
        validateVersion(version);
    }

    private void validateVersion(final String version) throws InvalidResponseException {

        if (version == null || !version.equals(JsonRequestEncoder.DEFAULT_VERSION)) {
            LOGGER.debug("expected JSON RPC {}, but relieved ", JsonRequestEncoder.DEFAULT_VERSION, version);
            throw InvalidResponseException.fromMessage("version must be equal to ", JsonRequestEncoder.DEFAULT_VERSION);
        }
    }
}
