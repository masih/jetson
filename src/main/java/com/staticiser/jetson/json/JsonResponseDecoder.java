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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.staticiser.jetson.Request;
import com.staticiser.jetson.Response;
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

    protected Response decode(final ChannelHandlerContext context, final ByteBuf msg) throws RPCException {

        JsonParser parser = null;
        try {
            parser = json_factory.createParser(new ByteBufInputStream(msg));
            parser.nextToken();
            final Response response = new Response(); //FIXME cache
            readAndValidateVersion(parser);
            validateAndSetResponseId(parser, response);
            final Request request = getClient(context).getPendingRequestById(response.getId());
            final Type expected_return_type;
            //            if (request == null) {
            //              expected_return_type =Object.class;
            //            }
            //            else {
            expected_return_type = request.getMethod().getGenericReturnType();
            //            }
            setResponseResultOrError(parser, response, expected_return_type);
            parser.nextToken();
            return response;
        }
        catch (final JsonParseException e) {
            LOGGER.debug("failed to parse response", e);
            throw new InvalidResponseException(e);
        }
        catch (final JsonGenerationException e) {
            LOGGER.debug("failed to generate response", e);
            throw new InternalServerException(e);
        }
        catch (final JsonProcessingException e) {
            LOGGER.debug("failed to decode response", e);
            throw new InvalidResponseException(e);
        }
        catch (final IOException e) {
            LOGGER.debug("IO error occurred while decoding response", e);
            throw new TransportException("failed to process response", e);
        }
        catch (NullPointerException e) {
            throw new InternalServerException(e); //FIXME
        }
        finally {
            CloseableUtil.closeQuietly(parser);
        }
    }

    private void validateAndSetResponseId(final JsonParser parser, final Response response) throws IOException {

        final Integer id = JsonParserUtil.readFieldValueAs(parser, JsonRequestEncoder.ID_KEY, Integer.class);
        response.setId(id);
    }

    private void setResponseResultOrError(final JsonParser parser, final Response response, final Type expected_return_type) throws IOException {

        final String next_field_name = JsonParserUtil.expectFieldNames(parser, JsonResponseEncoder.RESULT_KEY, JsonResponseEncoder.ERROR_KEY);
        if (JsonResponseEncoder.ERROR_KEY.equals(next_field_name)) {
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
            LOGGER.debug("expected JSON RPC {}, but recieved ", JsonRequestEncoder.DEFAULT_VERSION, version);
            throw InvalidResponseException.fromMessage("version must be equal to ", JsonRequestEncoder.DEFAULT_VERSION);
        }
    }
}
