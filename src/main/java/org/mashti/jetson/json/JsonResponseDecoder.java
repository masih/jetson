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
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.ResponseDecoder;
import org.mashti.jetson.exception.InternalServerException;
import org.mashti.jetson.exception.InvalidResponseException;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.ServerRuntimeException;
import org.mashti.jetson.exception.TransportException;
import org.mashti.jetson.util.CloseableUtil;
import org.mashti.jetson.util.JsonParserUtil;
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
        FutureResponse future_response = null;
        try {
            parser = json_factory.createParser(new ByteBufInputStream(in));
            parser.nextToken();
            final Integer id = validateAndReadResponseId(parser);
            future_response = getFutureResponseById(context, id);
            readAndValidateVersion(parser);

            final Type expected_return_type = future_response.getMethod().getGenericReturnType();
            setResponseResultOrError(parser, future_response, ((ParameterizedType)expected_return_type).getActualTypeArguments()[0]);
            parser.nextToken();
        }
        catch (final JsonParseException e) {
            LOGGER.debug("failed to parse response", e);

            checkFutureResponse(future_response);
            future_response.completeExceptionally(new InvalidResponseException(e));
        }
        catch (final JsonGenerationException e) {
            LOGGER.debug("failed to generate response", e);

            checkFutureResponse(future_response);
            future_response.completeExceptionally(new InternalServerException(e));
        }
        catch (final IOException e) {
            LOGGER.debug("IO error occurred while decoding response", e);

            checkFutureResponse(future_response);
            future_response.completeExceptionally(new TransportException("failed to process response", e));
        }
        catch (final RuntimeException e) {
            LOGGER.debug("runtime error while decoding response", e);

            checkFutureResponse(future_response);
            future_response.completeExceptionally(new ServerRuntimeException(e));
        }
        finally {
            CloseableUtil.closeQuietly(parser);
        }

        return future_response;
    }

    private void checkFutureResponse(final FutureResponse future_response) throws RPCException {

        if (future_response == null) { throw new RPCException("failed to process enough response to determine pending future"); }
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
        response.complete(result);
    }

    private void setResponseError(final JsonParser parser, final FutureResponse response) throws IOException {

        final JsonRpcError error = JsonParserUtil.readValueAs(parser, JsonRpcError.class);
        if (error == null) { throw new InvalidResponseException("error in response must not be null"); }

        final Throwable throwable = JsonRpcExceptions.fromJsonRpcError(error);
        response.completeExceptionally(throwable);
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
