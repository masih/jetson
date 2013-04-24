package uk.ac.standrews.cs.jetson;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

import uk.ac.standrews.cs.jetson.exception.InternalException;
import uk.ac.standrews.cs.jetson.exception.InvalidJsonException;
import uk.ac.standrews.cs.jetson.exception.InvalidRequestException;
import uk.ac.standrews.cs.jetson.exception.InvalidResponseException;
import uk.ac.standrews.cs.jetson.exception.MethodNotFoundException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;

@Sharable
public class JsonRpcRequestDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger LOGGER = Logger.getLogger(JsonRpcRequestDecoder.class.getName());

    private final Map<String, Method> dispatch;

    private final JsonFactory json_factory;

    public JsonRpcRequestDecoder(final JsonFactory json_factory, final Map<String, Method> dispatch) {

        this.json_factory = json_factory;
        this.dispatch = dispatch;
    }

    @Override
    protected JsonRpcRequest decode(final ChannelHandlerContext ctx, final ByteBuf msg) throws Exception {

        JsonParser parser = null;
        try {
            parser = json_factory.createParser(new ByteBufInputStream(msg));
            final JsonRpcRequest request = new JsonRpcRequest();
            parser.nextToken();
            final String version = readAndValidateVersion(parser);
            final String method_name = readAndValidateMethodName(parser);
            final Object[] params = readRequestParameters(parser, method_name);
            final Long id = readAndValidateId(parser);
            request.setVersion(version);
            request.setMethod(findServiceMethodByName(method_name));
            request.setMethodName(method_name);
            request.setId(id);
            request.setParams(params);
            parser.nextToken();
            ctx.channel().attr(JsonRpcResponseDecoder.REQUEST_ID_ATTRIBUTE).set(request.getId());
            return request;
        }
        catch (final JsonParseException e) {
            throw new InvalidJsonException(e);
        }
        catch (final JsonGenerationException e) {
            throw new InternalException(e);
        }
        catch (final JsonProcessingException e) {
            throw new InvalidRequestException(e);
        }
        catch (final IOException e) {
            throw new InternalException(e);
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

    private Long readAndValidateId(final JsonParser parser) throws JsonParseException, IOException {

        final Long id = readValue(parser, JsonRpcMessage.ID_KEY, Long.class);
        if (id == null) { throw new InvalidResponseException("request id of null is not supported"); }
        return id;
    }

    private String readAndValidateMethodName(final JsonParser parser) throws JsonParseException, IOException {

        final String method_name = readValue(parser, JsonRpcRequest.METHOD_NAME_KEY, String.class);
        if (method_name == null) { throw new InvalidRequestException("method name cannot be null"); }
        return method_name;
    }

    private String readAndValidateVersion(final JsonParser parser) throws JsonParseException, IOException {

        final String version = readValue(parser, JsonRpcMessage.VERSION_KEY, String.class);
        if (version == null || !version.equals(JsonRpcMessage.DEFAULT_VERSION)) { throw new InvalidRequestException("version must be equal to " + JsonRpcMessage.DEFAULT_VERSION); }
        return version;
    }

    private <Value> Value readValue(final JsonParser parser, final String expected_key, final Class<Value> value_type) throws JsonParseException, IOException {

        if (parser.nextToken() == JsonToken.FIELD_NAME && expected_key.equals(parser.getCurrentName())) {
            parser.nextToken();
            return parser.readValueAs(value_type);
        }
        throw new InvalidRequestException("expected key " + expected_key);
    }

    private Object[] readRequestParameters(final JsonParser parser, final String method_name) throws IOException, JsonProcessingException, JsonParseException {

        if (parser.nextToken() != JsonToken.FIELD_NAME || !JsonRpcRequest.PARAMETERS_KEY.equals(parser.getCurrentName())) { throw new InvalidRequestException("params must not be omitted"); }
        final Object[] params;
        if (method_name == null) {
            LOGGER.warning("unspecified method name, or params is passed before method name in JSON request; deserializing parameters without type information.");
            params = readRequestParametersWithoutTypeInformation(parser);
        }
        else {
            final Class<?>[] param_types = findParameterTypesByMethodName(method_name);
            if (param_types == null) {
                LOGGER.warning("no parameter types was found for method " + method_name + "; deserializing parameters without type information.");
                return readRequestParametersWithoutTypeInformation(parser);
            }
            else {
                params = readRequestParametersWithTypes(parser, param_types);
            }
        }
        return params;
    }

    private Object[] readRequestParametersWithTypes(final JsonParser parser, final Class<?>[] types) throws IOException, JsonParseException, JsonProcessingException {

        final Object[] params = new Object[types.length];
        int index = 0;
        if (parser.nextToken() != JsonToken.START_ARRAY) { throw new InvalidRequestException("expected start array"); }
        while (parser.nextToken() != JsonToken.END_ARRAY && parser.getCurrentToken() != null) {
            params[index] = parser.readValueAs(types[index]);
            index++;
        }
        return params;
    }

    private Object[] readRequestParametersWithoutTypeInformation(final JsonParser parser) throws IOException, JsonProcessingException {

        parser.nextToken();
        return parser.readValueAs(Object[].class);
    }

    private Method findServiceMethodByName(final String method_name) throws MethodNotFoundException {

        if (!dispatch.containsKey(method_name)) { throw new MethodNotFoundException(); }
        return dispatch.get(method_name);
    }

    private Class<?>[] findParameterTypesByMethodName(final String method_name) {

        final Method method = dispatch.get(method_name);
        return method != null ? method.getParameterTypes() : null;
    }
}
