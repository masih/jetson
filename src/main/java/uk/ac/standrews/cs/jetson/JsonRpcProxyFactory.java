package uk.ac.standrews.cs.jetson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseError;
import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseResult;
import uk.ac.standrews.cs.jetson.exception.InvalidResponseException;
import uk.ac.standrews.cs.jetson.exception.InvocationException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcExceptions;
import uk.ac.standrews.cs.jetson.exception.TransportException;
import uk.ac.standrews.cs.jetson.exception.UnexpectedException;
import uk.ac.standrews.cs.jetson.util.CloseableUtil;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRpcProxyFactory {

    //TODO implement connection pooling
    private static final Logger LOGGER = Logger.getLogger(JsonRpcProxyFactory.class.getName());
    private final Class<?>[] interfaces;
    private final JsonFactory json_factory;
    private final Map<Method, String> dispatch;
    private final ClassLoader class_loader;
    private final AtomicLong next_request_id;

    public JsonRpcProxyFactory(final Class<?> service_interface, final JsonFactory json_factory) {

        this(service_interface, json_factory, ClassLoader.getSystemClassLoader());
    }

    public JsonRpcProxyFactory(final Class<?> service_interface, final JsonFactory json_factory, final ClassLoader class_loader) {

        dispatch = ReflectionUtil.mapMethodsToNames(service_interface);
        this.interfaces = new Class<?>[]{service_interface};
        this.json_factory = json_factory;
        this.class_loader = class_loader;
        next_request_id = new AtomicLong();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final InetSocketAddress address) throws IllegalArgumentException, IOException {

        //FIXME Cache generated proxies and clone if needed.

        final JsonRpcInvocationHandler handler = new JsonRpcInvocationHandler(address);
        return (T) Proxy.newProxyInstance(class_loader, interfaces, handler);
    }

    private Long generateRequestId() {

        return next_request_id.getAndIncrement();
    }

    private String getJsonRpcMethodName(final Method method) {

        return dispatch.get(method);
    }

    private class JsonRpcInvocationHandler implements InvocationHandler {

        private final InetSocketAddress address;

        public JsonRpcInvocationHandler(final InetSocketAddress address) throws IOException {

            this.address = address;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] params) throws Throwable {

            Socket socket = null;
            JsonParser json_parser = null;
            JsonGenerator json_generator = null;
            try {
                try {
                    socket = new Socket(address.getAddress(), address.getPort());
                    final InputStream input = socket.getInputStream();
                    json_parser = json_factory.createParser(new InputStreamReader(input));
                    final OutputStream outputStream = socket.getOutputStream();
                    json_generator = json_factory.createGenerator(outputStream);
                }
                catch (final IOException e) {
                    throw new TransportException(e);
                }
                final JsonRpcRequest request = createJsonRpcRequest(method, params);

                writeRequest(json_generator, request);
                final JsonRpcResponse response = readResponse(json_parser, method.getReturnType());
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

        private JsonRpcResponse readResponse(final JsonParser parser, final Class<?> expected_result_type) throws JsonRpcException {

            try {

                JsonRpcResponse response = null;
                String version = null;
                Long id = null;
                parser.nextToken();
                while (parser.nextToken() != JsonToken.END_OBJECT && parser.getCurrentToken() != null) {

                    final String fieldname = parser.getCurrentName();
                    if (JsonRpcMessage.VERSION_KEY.equals(fieldname)) {
                        parser.nextToken();
                        version = parser.getText();
                    }
                    if (JsonRpcResponse.ERROR_KEY.equals(fieldname)) {
                        parser.nextToken();
                        final JsonRpcResponseError response_error = new JsonRpcResponseError();
                        response_error.setError(parser.readValueAs(JsonRpcException.class));
                        response = response_error;
                    }
                    if (JsonRpcResponse.RESULT_KEY.equals(fieldname)) {
                        parser.nextToken();
                        final JsonRpcResponseResult response_result = new JsonRpcResponseResult();
                        if (expected_result_type.equals(Void.TYPE)) {
                            if (parser.getCurrentToken() != JsonToken.VALUE_NULL && !parser.getText().equals("")) { throw new InvalidResponseException(); }
                            response_result.setResult(null);
                        }
                        else {
                            response_result.setResult(parser.readValueAs(expected_result_type));
                        }
                        response = response_result;
                    }
                    if (JsonRpcMessage.ID_KEY.equals(fieldname)) {
                        parser.nextToken();
                        id = parser.getLongValue();
                    }
                }
                if (response == null) { throw new InvalidResponseException(); }
                response.setId(id);
                response.setVersion(version);
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

        private void writeRequest(final JsonGenerator json_generator, final JsonRpcRequest request) throws TransportException {

            try {
                final Method target_method = request.getTargetMethod();
                final Class<?>[] param_types = target_method.getParameterTypes();
                LOGGER.fine(((ObjectMapper) json_generator.getCodec()).writeValueAsString(request));

                final ObjectMapper mapper = (ObjectMapper) json_generator.getCodec();
                json_generator.writeStartObject();
                json_generator.writeObjectField(JsonRpcMessage.VERSION_KEY, request.getVersion());
                json_generator.writeObjectField(JsonRpcRequest.METHOD_KEY, request.getMethodName());
                if (request.getParameters() != null) {
                    json_generator.writeArrayFieldStart(JsonRpcRequest.PARAMETERS_KEY);
                    int i = 0;
                    for (final Object param : request.getParameters()) {

                        final Class<?> static_param_type = param_types[i++];
                        if (!mapper.canSerialize(static_param_type)) {
                            LOGGER.warning("No serializer is found for the type" + static_param_type + " at " + target_method);
                        }
                        //FIXME cache objectWriter
                        mapper.writerWithType(static_param_type).writeValue(json_generator, param);

                    }
                    json_generator.writeEndArray();
                }
                json_generator.writeObjectField(JsonRpcMessage.ID_KEY, request.getId());
                json_generator.writeEndObject();
                json_generator.flush();
            }
            catch (final IOException e) {
                throw new TransportException(e);
            }
        }
    }
}
