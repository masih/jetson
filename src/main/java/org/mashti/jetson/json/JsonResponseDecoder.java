/**
 * This file is part of jetson.
 *
 * jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jetson.  If not, see <http://www.gnu.org/licenses/>.
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
            setResponseResultOrError(parser, future_response, expected_return_type);
            parser.nextToken();
        }
        catch (final JsonParseException e) {
            LOGGER.debug("failed to parse response", e);

            checkFutureResponse(future_response);
            future_response.setException(new InvalidResponseException(e));
        }
        catch (final JsonGenerationException e) {
            LOGGER.debug("failed to generate response", e);

            checkFutureResponse(future_response);
            future_response.setException(new InternalServerException(e));
        }
        catch (final IOException e) {
            LOGGER.debug("IO error occurred while decoding response", e);

            checkFutureResponse(future_response);
            future_response.setException(new TransportException("failed to process response", e));
        }
        catch (final RuntimeException e) {
            LOGGER.debug("runtime error while decoding response", e);

            checkFutureResponse(future_response);
            future_response.setException(new ServerRuntimeException(e));
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
        response.set(result);
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
