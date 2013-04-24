package uk.ac.standrews.cs.jetson;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseError;
import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseResult;
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
public class JsonRpcResponseDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger LOGGER = Logger.getLogger(JsonRpcResponseDecoder.class.getName());

    static final String REQUEST_ID_ATTR_NAME = "request_id";
    static final AttributeKey<Long> REQUEST_ID_ATTRIBUTE = new AttributeKey<Long>(REQUEST_ID_ATTR_NAME);
    static final String RETURN_TYPE_ATTR_NAME = "return_type";
    static final AttributeKey<Type> RETURN_TYPE_ATTRIBUTE = new AttributeKey<Type>(RETURN_TYPE_ATTR_NAME);

    private final JsonFactory json_factory;

    public JsonRpcResponseDecoder(final JsonFactory json_factory) {

        this.json_factory = json_factory;
    }

    @Override
    protected JsonRpcResponse decode(final ChannelHandlerContext ctx, final ByteBuf msg) throws Exception {

        final long request_id = ctx.channel().attr(REQUEST_ID_ATTRIBUTE).get();
        final Type expected_return_type = ctx.channel().attr(RETURN_TYPE_ATTRIBUTE).get();
        JsonParser parser = null;
        try {
            parser = json_factory.createParser(new ByteBufInputStream(msg));

            parser.nextToken();
            final String version = readAndValidateVersion(parser);
            final JsonRpcResponse response = readAndValidateResultOrError(parser, expected_return_type);
            final Long id = readAndValidateId(parser, request_id);
            response.setId(id);
            response.setVersion(version);
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

    private Long readAndValidateId(final JsonParser parser, final Long expected_id) throws IOException {

        final Long id = readValue(parser, JsonRpcMessage.ID_KEY, Long.class);
        if (id == null || !id.equals(expected_id)) { throw new InvalidResponseException("response id must not be null, and must be equal to " + expected_id); }
        return id;
    }

    private JsonRpcResponse readAndValidateResultOrError(final JsonParser parser, final Type expected_return_type) throws IOException {

        if (parser.nextToken() == JsonToken.FIELD_NAME) {
            final String key = parser.getCurrentName();
            if (JsonRpcResponse.ERROR_KEY.equals(key)) {
                parser.nextToken();
                final JsonRpcResponseError response_error = new JsonRpcResponseError();
                final JsonRpcException error = parser.readValueAs(JsonRpcException.class);
                if (error == null) { throw new InvalidResponseException("error in response must not be null "); }
                response_error.setError(error);
                return response_error;
            }
            else if (JsonRpcResponse.RESULT_KEY.equals(key)) {
                parser.nextToken();
                final JsonRpcResponseResult response_result = new JsonRpcResponseResult();
                if (expected_return_type.equals(Void.TYPE)) {
                    if (parser.getCurrentToken() != JsonToken.VALUE_NULL && !parser.getText().equals("")) { throw new InvalidResponseException("expected void method return type but found value"); }
                    response_result.setResult(null);
                }
                else {
                    final ObjectMapper mapper = (ObjectMapper) parser.getCodec();
                    final JavaType type = mapper.getTypeFactory().constructType(expected_return_type);
                    response_result.setResult(mapper.readValue(parser, type));
                }
                return response_result;
            }
            else {
                throw new InvalidResponseException("expected result or error key, found " + key);
            }
        }
        throw new InvalidResponseException("expected key, found " + parser.getCurrentToken());
    }

    private String readAndValidateVersion(final JsonParser parser) throws IOException {

        final String version = readValue(parser, JsonRpcMessage.VERSION_KEY, String.class);
        if (version == null || !version.equals(JsonRpcMessage.DEFAULT_VERSION)) { throw new InvalidResponseException("version must be equal to " + JsonRpcMessage.DEFAULT_VERSION); }
        return version;
    }

    private <Value> Value readValue(final JsonParser parser, final String expected_key, final Class<Value> value_type) throws IOException {

        if (parser.nextToken() == JsonToken.FIELD_NAME && expected_key.equals(parser.getCurrentName())) {
            parser.nextToken();
            return parser.readValueAs(value_type);
        }
        throw new InvalidResponseException("expected key " + expected_key + ", found " + parser.getCurrentToken());
    }
}
