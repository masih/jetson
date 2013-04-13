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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseError;
import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseResult;
import uk.ac.standrews.cs.jetson.exception.AccessException;
import uk.ac.standrews.cs.jetson.exception.InternalException;
import uk.ac.standrews.cs.jetson.exception.InvalidJsonException;
import uk.ac.standrews.cs.jetson.exception.InvalidParameterException;
import uk.ac.standrews.cs.jetson.exception.InvalidRequestException;
import uk.ac.standrews.cs.jetson.exception.InvalidResponseException;
import uk.ac.standrews.cs.jetson.exception.InvocationException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcError;
import uk.ac.standrews.cs.jetson.exception.JsonRpcException;
import uk.ac.standrews.cs.jetson.exception.MethodNotFoundException;
import uk.ac.standrews.cs.jetson.exception.ParseException;
import uk.ac.standrews.cs.jetson.exception.ServerException;
import uk.ac.standrews.cs.jetson.exception.ServerRuntimeException;
import uk.ac.standrews.cs.jetson.util.CloseableUtil;
import uk.ac.standrews.cs.jetson.util.ReflectionUtil;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;

public class JsonRpcServer {

    private static final ServerSocketFactory DEFAULT_SERVER_SOCKET_FACTORY = ServerSocketFactory.getDefault();
    private static final Logger LOGGER = Logger.getLogger(JsonRpcServer.class.getName());
    private static final JsonEncoding DEFAULT_JSON_ENCODING = JsonEncoding.UTF8;
    private static final int DEFAULT_SOCKET_READ_TIMEOUT_MILLISECONDS = 10 * 1000;
    private static final AtomicLong NEXT_REQUEST_HANDLER_ID = new AtomicLong();
    private final Object service;
    private final ServerSocketFactory server_socket_factory;
    private final JsonFactory json_factory;
    private final ExecutorService request_handler_executor;
    private final Thread server_thread;
    private final Map<String, Method> dispatch;
    private final ConcurrentSkipListSet<JsonRpcRequestHandler> request_handlers;
    private final ReentrantLock exposure_lock;
    private volatile ServerSocket server_socket;
    private volatile JsonEncoding encoding;
    private volatile int socket_read_timeout;
    private volatile InetSocketAddress endpoint;

    public <T> JsonRpcServer(final Class<T> service_interface, final T service, final JsonFactory json_factory, final ExecutorService request_handler_executor) {

        this(DEFAULT_SERVER_SOCKET_FACTORY, service_interface, service, json_factory, request_handler_executor);
    }

    public <T> JsonRpcServer(final ServerSocketFactory server_socket_factory, final Class<T> service_interface, final T service, final JsonFactory json_factory, final ExecutorService request_handler_executor) {

        this.server_socket_factory = server_socket_factory;
        this.service = service;
        this.json_factory = json_factory;
        this.request_handler_executor = request_handler_executor;
        request_handlers = new ConcurrentSkipListSet<JsonRpcRequestHandler>();
        exposure_lock = new ReentrantLock();

        dispatch = ReflectionUtil.mapNamesToMethods(service_interface);
        server_thread = new ServerThread();
        setDefaultConfigurations();
    }

    protected void afterReadRequest(final JsonParser parser, final JsonRpcRequest request) {

    }

    protected void afterWriteResponse(final JsonGenerator generator, final JsonRpcResponse response) {

    }

    private void setDefaultConfigurations() {

        setEncoding(DEFAULT_JSON_ENCODING);
        setSocketReadTimeout(DEFAULT_SOCKET_READ_TIMEOUT_MILLISECONDS);
    }

    public void setSocketReadTimeout(final int milliseconds) {

        socket_read_timeout = milliseconds;
    }

    public void setEncoding(final JsonEncoding encoding) {

        this.encoding = encoding;
    }

    public void setBindAddress(final InetSocketAddress endpoint) {

        this.endpoint = endpoint;
    }

    public void expose() throws IOException {

        exposure_lock.lock();
        try {
            createServerSocket();
            server_socket.bind(endpoint);
            server_thread.start();
        }
        finally {
            exposure_lock.unlock();
        }
    }

    private void createServerSocket() throws IOException {

        server_socket = server_socket_factory.createServerSocket();
    }

    public void unexpose() throws IOException {

        exposure_lock.lock();
        try {
            server_socket.close();
            server_thread.interrupt();
        }
        finally {
            exposure_lock.unlock();
        }
    }

    public InetSocketAddress getLocalSocketAddress() {

        exposure_lock.lock();
        try {
            return server_socket == null ? null : (InetSocketAddress) server_socket.getLocalSocketAddress();
        }
        finally {
            exposure_lock.unlock();
        }
    }

    public void shutdown() {

        unexposeScilently();
        shutdownRequestHandlers();
        request_handler_executor.shutdownNow();
    }

    private void unexposeScilently() {

        try {
            unexpose();
        }
        catch (final IOException e) {
            LOGGER.log(Level.WARNING, "error occured while unexposing json rpc server as part of shutdown", e);
        }
    }

    private void shutdownRequestHandlers() {

        for (final JsonRpcRequestHandler request_handler : request_handlers) {
            request_handler.shutdown();
        }
    }

    private Method findServiceMethodByName(final String method_name) throws MethodNotFoundException {

        if (!dispatch.containsKey(method_name)) { throw new MethodNotFoundException(); }
        return dispatch.get(method_name);
    }

    private Class<?>[] findParameterTypesByMethodName(final String method_name) {

        final Method method = dispatch.get(method_name);
        return method != null ? method.getParameterTypes() : null;
    }

    private Object invoke(final Method method, final Object... parameters) throws IllegalAccessException, InvocationTargetException {

        return method.invoke(service, parameters);
    }

    private void handleSocket(final Socket socket) {

        try {
            final JsonRpcRequestHandler request_handler = new JsonRpcRequestHandler(socket);
            request_handler_executor.execute(request_handler);
            request_handlers.add(request_handler);
        }
        catch (final IOException e) {
            LOGGER.log(Level.WARNING, "failed to handle established socket", e);
        }
    }

    static JsonParser createJsonParser(final Socket socket, final JsonFactory json_factory, final JsonEncoding encoding) throws IOException {

        final BufferedReader buffered_reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), encoding.getJavaName()));
        return json_factory.createParser(buffered_reader);
    }

    static JsonGenerator createJsonGenerator(final Socket socket, final JsonFactory json_factory, final JsonEncoding encoding) throws IOException {

        final BufferedWriter buffered_writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encoding.getJavaName()));
        return json_factory.createGenerator(buffered_writer);
    }

    private class ServerThread extends Thread {

        public ServerThread() {

        }

        @Override
        public void run() {

            setName("server_thread_" + server_socket.getLocalPort());
            while (!isInterrupted() && !server_socket.isClosed()) {

                try {
                    final Socket socket = server_socket.accept();
                    socket.setSoTimeout(socket_read_timeout);
                    handleSocket(socket);
                }
                catch (final Exception e) {
                    interrupt();
                }
            }
            LOGGER.fine("server stopped listening for incomming connections");
        }
    }

    private class JsonRpcRequestHandler implements Runnable, Comparable<JsonRpcRequestHandler> {

        private final Long id;
        private final Socket socket;
        private final JsonGenerator generator;
        private final JsonParser parser;
        private volatile Long current_request_id;

        public JsonRpcRequestHandler(final Socket socket) throws IOException {

            id = NEXT_REQUEST_HANDLER_ID.getAndIncrement();
            this.socket = socket;
            generator = createJsonGenerator(socket, json_factory, encoding);
            parser = createJsonParser(socket, json_factory, encoding);
        }

        @Override
        public void run() {

            try {
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    final JsonRpcRequest request = readRequest();
                    final JsonRpcResponseResult result = handleRequest(request);
                    writeResponse(result);
                }
            }
            catch (final RuntimeException e) {
                handleException(new ServerRuntimeException(e));
            }
            catch (final JsonRpcException e) {
                handleException(e);
            }
            finally {
                shutdown();
            }
        }

        @Override
        public int compareTo(final JsonRpcRequestHandler other) {

            return id.compareTo(other.id);
        }

        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {

            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            final JsonRpcRequestHandler other = (JsonRpcRequestHandler) obj;
            if (!getOuterType().equals(other.getOuterType())) { return false; }
            if (id == null) {
                if (other.id != null) { return false; }
            }
            else if (!id.equals(other.id)) { return false; }
            return true;
        }

        private void shutdown() {

            CloseableUtil.closeQuietly(generator, parser, CloseableUtil.toCloseable(socket));
            request_handlers.remove(this);
        }

        private void handleException(final JsonRpcException exception) {

            if (!socket.isClosed()) {
                final JsonRpcResponseError error = new JsonRpcResponseError(current_request_id, exception);
                try {
                    writeResponse(error);
                }
                catch (final Throwable e) {
                    LOGGER.log(Level.FINE, "failed to notify JSON RPC error", e);
                }
            }
        }

        private JsonRpcResponseResult handleRequest(final JsonRpcRequest request) throws ServerException {

            final Method method = request.getMethod();
            final Object[] parameters = request.getParameters();

            try {
                final Object result = invoke(method, parameters);
                return new JsonRpcResponseResult(request.getId(), result);
            }
            catch (final IllegalArgumentException e) {
                throw new InvalidParameterException(e);
            }
            catch (final RuntimeException e) {
                throw new ServerRuntimeException(e);
            }
            catch (final InvocationTargetException e) {
                throw new InvocationException(e);
            }
            catch (final IllegalAccessException e) {
                throw new AccessException(e);
            }
            catch (final ExceptionInInitializerError e) {
                throw new InternalException(e);
            }
        }

        private void setCurrentRequestId(final Long request_id) {

            this.current_request_id = request_id;
        }

        private void resetCurrentRequestId() {

            setCurrentRequestId(null);
        }

        private JsonRpcRequest readRequest() throws ServerException, ParseException {

            try {
                resetCurrentRequestId();

                final JsonRpcRequest request = new JsonRpcRequest();
                parser.nextToken();
                final String version = readAndValidateVersion();
                final String method_name = readAndValidateMethodName();
                final Object[] params = readRequestParameters(method_name);
                final Long id = readAndValidateId();
                request.setVersion(version);
                request.setMethod(findServiceMethodByName(method_name));
                request.setMethodName(method_name);
                request.setId(id);
                request.setParams(params);
                setCurrentRequestId(request.getId());
                afterReadRequest(parser, request);
                parser.nextToken();
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
        }

        private Long readAndValidateId() throws JsonParseException, IOException {

            final Long id = readValue(parser, JsonRpcMessage.ID_KEY, Long.class);
            if (id == null) { throw new InvalidResponseException("request id of null is not supported"); }
            return id;
        }

        private String readAndValidateMethodName() throws JsonParseException, IOException {

            final String method_name = readValue(parser, JsonRpcRequest.METHOD_NAME_KEY, String.class);
            if (method_name == null) { throw new InvalidRequestException("method name cannot be null"); }
            return method_name;
        }

        private String readAndValidateVersion() throws JsonParseException, IOException {

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

        private Object[] readRequestParameters(final String method_name) throws IOException, JsonProcessingException, JsonParseException {

            if (parser.nextToken() != JsonToken.FIELD_NAME || !JsonRpcRequest.PARAMETERS_KEY.equals(parser.getCurrentName())) { throw new InvalidRequestException("params must not be omitted"); }
            final Object[] params;
            if (method_name == null) {
                LOGGER.warning("unspecified method name, or params is passed before method name in JSON request; deserializing parameters without type information.");
                params = readRequestParametersWithoutTypeInformation();
            }
            else {
                final Class<?>[] param_types = findParameterTypesByMethodName(method_name);
                if (param_types == null) {
                    LOGGER.warning("no parameter types was found for method " + method_name + "; deserializing parameters without type information.");
                    return readRequestParametersWithoutTypeInformation();
                }
                else {
                    params = readRequestParametersWithTypes(param_types);
                }
            }
            return params;
        }

        private Object[] readRequestParametersWithTypes(final Class<?>[] types) throws IOException, JsonParseException, JsonProcessingException {

            final Object[] params = new Object[types.length];
            int index = 0;
            if (parser.nextToken() != JsonToken.START_ARRAY) { throw new InvalidRequestException("expected start array"); }
            while (parser.nextToken() != JsonToken.END_ARRAY && parser.getCurrentToken() != null) {
                params[index] = parser.readValueAs(types[index]);
                index++;
            }
            return params;
        }

        private Object[] readRequestParametersWithoutTypeInformation() throws IOException, JsonProcessingException {

            parser.nextToken();
            return parser.readValueAs(Object[].class);
        }

        private void writeResponse(final JsonRpcResponse response) throws ServerException {

            try {
                generator.writeStartObject();
                generator.writeObjectField(JsonRpcMessage.VERSION_KEY, response.getVersion());
                if (response instanceof JsonRpcResponseResult) {
                    generator.writeObjectField(JsonRpcResponse.RESULT_KEY, ((JsonRpcResponseResult) response).getResult());
                }
                else {
                    final JsonRpcError error = ((JsonRpcResponseError) response).getError();
                    LOGGER.fine("error occured on server " + error);
                    generator.writeObjectField(JsonRpcResponse.ERROR_KEY, error);
                }
                generator.writeObjectField(JsonRpcMessage.ID_KEY, response.getId());
                afterWriteResponse(generator, response);
                generator.writeEndObject();
                generator.flush();
            }
            catch (final IOException e) {
                throw new InternalException(e);
            }
        }

        private JsonRpcServer getOuterType() {

            return JsonRpcServer.this;
        }
    }
}
