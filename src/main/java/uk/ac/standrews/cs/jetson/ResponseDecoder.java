package uk.ac.standrews.cs.jetson;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.io.IOException;
import java.lang.reflect.Type;

import uk.ac.standrews.cs.jetson.exception.InternalException;
import uk.ac.standrews.cs.jetson.exception.InvalidResponseException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcException;
import uk.ac.standrews.cs.jetson.exception.TransportException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

@Sharable
class ResponseDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final JsonFactory json_factory;

    ResponseDecoder(final JsonFactory json_factory) {

        this.json_factory = json_factory;
    }

    @Override
    protected Response decode(final ChannelHandlerContext ctx, final ByteBuf msg) throws Exception {

        final Response response = ctx.channel().attr(ClientHandler.RESPONSE_ATTRIBUTE).get();
        final Request request = ctx.channel().attr(ClientHandler.REQUEST_ATTRIBUTE).get();
        final long request_id = request.getId();
        final Type expected_return_type = request.getMethod().getGenericReturnType();

        JsonParser parser = null;
        try {
            parser = json_factory.createParser(new ByteBufInputStream(msg));
            parser.nextToken();
            setResponseVersion(parser, response);
            setResponseResultOrError(parser, response, expected_return_type);
            vaildateAndSetResponseId(parser, response, request_id);
            parser.nextToken();
            return response;
        }
        catch (final JsonProcessingException e) {
            throw new InvalidResponseException(e);
        }
        catch (final JsonRpcException e) {
            throw e;
        }
        catch (final IOException e) {
            throw new TransportException(e);
        }
        finally {
            if (parser != null) {
                try {
                    parser.close();
                }
                catch (final IOException e) {
                    throw new InternalException(e);
                }
            }
        }
    }

    private void vaildateAndSetResponseId(final JsonParser parser, final Response response, final Long expected_id) throws IOException {

        final Long id = readValue(parser, Message.ID_KEY, Long.class);
        if (id == null || !id.equals(expected_id)) { throw new InvalidResponseException("response id must not be null, and must be equal to " + expected_id); }
        response.setId(id);
    }

    private void setResponseResultOrError(final JsonParser parser, final Response response, final Type expected_return_type) throws IOException {

        if (parser.nextToken() == JsonToken.FIELD_NAME) {
            final String key = parser.getCurrentName();
            if (Response.ERROR_KEY.equals(key)) {
                parser.nextToken();
                final JsonRpcException error = parser.readValueAs(JsonRpcException.class);
                if (error == null) { throw new InvalidResponseException("error in response must not be null "); }
                response.setError(error);
            }
            else if (Response.RESULT_KEY.equals(key)) {
                final Object result;
                parser.nextToken();
                if (expected_return_type.equals(Void.TYPE)) {
                    if (parser.getCurrentToken() != JsonToken.VALUE_NULL && !parser.getText().equals("")) { throw new InvalidResponseException("expected void method return type but found value"); }
                    result = null;
                }
                else {
                    //TODO pool object writers
                    final ObjectMapper mapper = (ObjectMapper) parser.getCodec();
                    final JavaType type = mapper.getTypeFactory().constructType(expected_return_type);
                    result = mapper.readValue(parser, type);
                }
                response.setResult(result);
            }
            else {
                throw new InvalidResponseException("expected result or error key, found " + key);
            }
        }
        else {
            throw new InvalidResponseException("expected key, found " + parser.getCurrentToken());
        }
    }

    private void setResponseVersion(final JsonParser parser, final Response response) throws IOException {

        final String version = readValue(parser, Message.VERSION_KEY, String.class);
        if (version == null || !version.equals(Message.DEFAULT_VERSION)) { throw new InvalidResponseException("version must be equal to " + Message.DEFAULT_VERSION); }
        response.setVersion(version);
    }

    private <Value> Value readValue(final JsonParser parser, final String expected_key, final Class<Value> value_type) throws IOException {

        if (parser.nextToken() == JsonToken.FIELD_NAME && expected_key.equals(parser.getCurrentName())) {
            parser.nextToken();
            return parser.readValueAs(value_type);
        }
        throw new InvalidResponseException("expected key " + expected_key + ", found " + parser.getCurrentToken());
    }
}
