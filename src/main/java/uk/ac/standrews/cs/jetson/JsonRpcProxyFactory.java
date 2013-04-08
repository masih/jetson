/*
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
package uk.ac.standrews.cs.jetson;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javax.net.SocketFactory;

import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseError;
import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseResult;
import uk.ac.standrews.cs.jetson.exception.InvalidResponseException;
import uk.ac.standrews.cs.jetson.exception.InvocationException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcExceptions;
import uk.ac.standrews.cs.jetson.exception.TransportException;
import uk.ac.standrews.cs.jetson.exception.UnexpectedException;
import uk.ac.standrews.cs.jetson.util.CloseableUtil;
import uk.ac.standrews.cs.jetson.util.ReflectionUtil;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonRpcProxyFactory {

    //TODO implement connection pooling
    private static final Logger LOGGER = Logger.getLogger(JsonRpcProxyFactory.class.getName());
    private static final JsonEncoding DEFAULT_ENCODING = JsonEncoding.UTF8;
    private final Class<?>[] interfaces;
    private final JsonFactory json_factory;
    private final Map<Method, String> dispatch;
    private final ClassLoader class_loader;
    private final AtomicLong next_request_id;
    private final SocketFactory socket_factory;

    public JsonRpcProxyFactory(final Class<?> service_interface, final JsonFactory json_factory) {

        this(service_interface, json_factory, ClassLoader.getSystemClassLoader());
    }

    public JsonRpcProxyFactory(final Class<?> service_interface, final JsonFactory json_factory, final ClassLoader class_loader) {

        this(SocketFactory.getDefault(), service_interface, json_factory, class_loader);

    }

    public JsonRpcProxyFactory(final SocketFactory socket_factory, final Class<?> service_interface, final JsonFactory json_factory, final ClassLoader class_loader) {

        this.socket_factory = socket_factory;
        dispatch = ReflectionUtil.mapMethodsToNames(service_interface);
        this.interfaces = new Class<?>[]{service_interface};
        this.json_factory = json_factory;
        this.class_loader = class_loader;
        next_request_id = new AtomicLong();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final InetSocketAddress address) {

        //FIXME Cache generated proxies and clone if needed.

        final JsonRpcInvocationHandler handler = new JsonRpcInvocationHandler(address);
        return (T) Proxy.newProxyInstance(class_loader, interfaces, handler);
    }

    protected void afterReadResponse(final JsonParser parser, final JsonRpcResponse response) {

    }

    protected void afterWriteRequest(final JsonGenerator generator, final JsonRpcRequest request) {

    }

    private Long generateRequestId() {

        return next_request_id.getAndIncrement();
    }

    private String getJsonRpcMethodName(final Method method) {

        return dispatch.get(method);
    }

    private class JsonRpcInvocationHandler implements InvocationHandler {

        private final InetSocketAddress address;

        public JsonRpcInvocationHandler(final InetSocketAddress address) {

            this.address = address;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] params) throws Throwable {

            Socket socket = null;
            JsonParser json_parser = null;
            JsonGenerator json_generator = null;
            try {
                try {
                    socket = socket_factory.createSocket(address.getAddress(), address.getPort());
                    json_parser = JsonRpcServer.createJsonParser(socket, json_factory, DEFAULT_ENCODING);
                    json_generator = JsonRpcServer.createJsonGenerator(socket, json_factory, DEFAULT_ENCODING);
                }
                catch (final IOException e) {
                    throw new TransportException(e);
                }
                final JsonRpcRequest request = createJsonRpcRequest(method, params);
                writeRequest(json_generator, request);
                final JsonRpcResponse response = readResponse(json_parser, method.getReturnType(), request.getId());
                if (isResponseError(response)) {
                    final JsonRpcResponseError response_error = toJsonRpcResponseError(response);
                    final JsonRpcException exception = JsonRpcExceptions.fromJsonRpcError(response_error.getError());
                    throw !isInvocationException(exception) ? exception : reconstructException(method.getExceptionTypes(), castToInvovationException(exception));
                }
                return JsonRpcResponseResult.class.cast(response).getResult();
            }
            finally {
                CloseableUtil.closeQuietly(json_parser, json_generator, CloseableUtil.toCloseable(socket));
            }
        }

        private JsonRpcRequest createJsonRpcRequest(final Method method, final Object[] params) {

            final Long request_id = generateRequestId();
            final String json_rpc_method_name = getJsonRpcMethodName(method);
            return new JsonRpcRequest(request_id, method, json_rpc_method_name, params);
        }

        private InvocationException castToInvovationException(final JsonRpcException exception) {

            assert InvocationException.class.isInstance(exception);
            return InvocationException.class.cast(exception);
        }

        private Throwable reconstructException(final Class<?>[] expected_types, final InvocationException exception) {

            final Object exception_data = exception.getData();
            final Throwable throwable;
            if (Throwable.class.isInstance(exception_data)) {
                final Throwable cause = Throwable.class.cast(exception_data);
                throwable = reconstructExceptionFromExpectedTypes(expected_types, cause);
            }
            else {
                throwable = new UnexpectedException("cannot determine the cause of remote invocation exception : " + exception_data);
            }

            return throwable;
        }

        private Throwable reconstructExceptionFromExpectedTypes(final Class<?>[] expected_types, final Throwable cause) {

            return !isExpected(expected_types, cause) ? new UnexpectedException(cause) : cause;
        }

        private boolean isExpected(final Class<?>[] expected_types, final Throwable cause) {

            return ReflectionUtil.containsAnyAssignableFrom(cause.getClass(), expected_types);
        }

        private boolean isInvocationException(final JsonRpcException exception) {

            return InvocationException.class.isInstance(exception);
        }

        private JsonRpcResponseError toJsonRpcResponseError(final JsonRpcResponse response) {

            return JsonRpcResponseError.class.cast(response);
        }

        private boolean isResponseError(final JsonRpcResponse response) {

            return JsonRpcResponseError.class.isInstance(response);
        }

        private JsonRpcResponse readResponse(final JsonParser parser, final Class<?> expected_result_type, final Long request_id) throws JsonRpcException {

            try {

                parser.nextToken();
                final String version = readAndValidateVersion(parser);
                final JsonRpcResponse response = readAndValidateResultOrError(parser, expected_result_type);
                final Long id = readAndValidateId(parser, request_id);
                response.setId(id);
                response.setVersion(version);
                afterReadResponse(parser, response);
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
        }

        private Long readAndValidateId(final JsonParser parser, final Long expected_id) throws JsonParseException, IOException {

            final Long id = readValue(parser, JsonRpcMessage.ID_KEY, Long.class);
            if (id == null || !id.equals(expected_id)) { throw new InvalidResponseException("response id must not be null, and must be equal to " + expected_id); }
            return id;
        }

        private JsonRpcResponse readAndValidateResultOrError(final JsonParser parser, final Class<?> expected_result_type) throws JsonParseException, InvalidResponseException, JsonProcessingException, IOException {

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
                    if (expected_result_type.equals(Void.TYPE)) {
                        if (parser.getCurrentToken() != JsonToken.VALUE_NULL && !parser.getText().equals("")) { throw new InvalidResponseException("expected void method return type but found value"); }
                        response_result.setResult(null);
                    }
                    else {
                        response_result.setResult(parser.readValueAs(expected_result_type));
                    }
                    return response_result;
                }
                else {
                    throw new InvalidResponseException("expected result or error key, found " + key);
                }
            }
            throw new InvalidResponseException("expected key, found " + parser.getCurrentToken());
        }

        private String readAndValidateVersion(final JsonParser parser) throws JsonParseException, IOException {

            final String version = readValue(parser, JsonRpcMessage.VERSION_KEY, String.class);
            if (version == null || !version.equals(JsonRpcMessage.DEFAULT_VERSION)) { throw new InvalidResponseException("version must be equal to " + JsonRpcMessage.DEFAULT_VERSION); }
            return version;
        }

        private void writeRequest(final JsonGenerator generator, final JsonRpcRequest request) throws TransportException {

            try {
                final Method target_method = request.getMethod();
                final Class<?>[] param_types = target_method.getParameterTypes();
                LOGGER.fine(((ObjectMapper) generator.getCodec()).writeValueAsString(request));

                final ObjectMapper mapper = (ObjectMapper) generator.getCodec();
                generator.writeStartObject();
                generator.writeObjectField(JsonRpcMessage.VERSION_KEY, request.getVersion());
                generator.writeObjectField(JsonRpcRequest.METHOD_NAME_KEY, request.getMethodName());
                generator.writeArrayFieldStart(JsonRpcRequest.PARAMETERS_KEY);
                if (request.getParameters() != null) {
                    int i = 0;
                    for (final Object param : request.getParameters()) {

                        final Class<?> static_param_type = param_types[i++];
                        if (!mapper.canSerialize(static_param_type)) {
                            LOGGER.warning("No serializer is found for the type" + static_param_type + " at " + target_method);
                        }

                        final ObjectWriter writer = mapper.writerWithType(static_param_type);
                        //FIXME cache writers
                        writer.writeValue(generator, param);
                    }
                }
                generator.writeEndArray();
                generator.writeObjectField(JsonRpcMessage.ID_KEY, request.getId());
                afterWriteRequest(generator, request);
                generator.writeEndObject();
                generator.flush();
            }
            catch (final IOException e) {
                throw new TransportException(e);
            }
        }

    }

    private <Value> Value readValue(final JsonParser parser, final String expected_key, final Class<Value> value_type) throws JsonParseException, IOException {

        if (parser.nextToken() == JsonToken.FIELD_NAME && expected_key.equals(parser.getCurrentName())) {
            parser.nextToken();
            return parser.readValueAs(value_type);
        }
        throw new InvalidResponseException("expected key " + expected_key);
    }

}
