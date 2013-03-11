package uk.ac.standrews.cs.jetson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.standrews.cs.jetson.exception.AccessException;
import uk.ac.standrews.cs.jetson.exception.InternalException;
import uk.ac.standrews.cs.jetson.exception.InvalidJsonException;
import uk.ac.standrews.cs.jetson.exception.InvalidParameterException;
import uk.ac.standrews.cs.jetson.exception.InvalidRequestException;
import uk.ac.standrews.cs.jetson.exception.InvocationException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcException;
import uk.ac.standrews.cs.jetson.exception.MethodNotFoundException;
import uk.ac.standrews.cs.jetson.exception.ParseException;
import uk.ac.standrews.cs.jetson.exception.ServerException;
import uk.ac.standrews.cs.jetson.exception.ServerRuntimeException;
import uk.ac.standrews.cs.jetson.util.CloseableUtil;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonRpcServer {

    private static final Logger LOGGER = Logger.getLogger(JsonRpcServer.class.getName());
    private static final JsonEncoding DEFAULT_JSON_ENCODING = JsonEncoding.UTF8;
    private static final int DEFAULT_SOCKET_READ_TIMEOUT_MILLISECONDS = 10 * 1000;
    private static final AtomicLong NEXT_REQUEST_HANDLER_ID = new AtomicLong();
    private final Object service;
    private final ServerSocket server_socket;
    private final JsonFactory json_factory;
    private final ExecutorService executor;
    private final Thread server_thread;
    private final Map<String, Method> dispatch;
    private final ConcurrentSkipListSet<JsonRpcRequestHandler> request_handlers;
    private volatile JsonEncoding encoding;
    private volatile int socket_read_timeout;

    public <T> JsonRpcServer(final ServerSocket server_socket, final Class<T> service_interface, final T service, final JsonFactory json_factory, final ExecutorService executor) {

        this.server_socket = server_socket;
        this.service = service;
        this.json_factory = json_factory;
        this.executor = executor;
        request_handlers = new ConcurrentSkipListSet<JsonRpcRequestHandler>();
        dispatch = ReflectionUtil.mapNamesToMethods(service_interface);
        server_thread = new ServerThread();
        setEncoding(DEFAULT_JSON_ENCODING);
        setSocketReadTimeout(DEFAULT_SOCKET_READ_TIMEOUT_MILLISECONDS);
    }

    public void setSocketReadTimeout(final int milliseconds) {

        socket_read_timeout = milliseconds;
    }

    public void setEncoding(final JsonEncoding encoding) {

        this.encoding = encoding;
    }

    public void expose() {

        server_thread.start();
    }

    public void unexpose() {

        server_thread.interrupt();
    }

    public void shutdown() {

        unexpose();
        shutdownRequestHandlers();
        executor.shutdownNow();
        CloseableUtil.closeQuietly(CloseableUtil.toCloseable(server_socket));
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

    private Object invoke(final Method method, final Object... parameters) throws IllegalAccessException, InvocationTargetException {

        return method.invoke(service, parameters);
    }

    private void handleSocket(final Socket socket) {

        final JsonRpcRequestHandler request_handler;
        try {
            request_handler = new JsonRpcRequestHandler(socket);

        }
        catch (final IOException e) {
            LOGGER.log(Level.WARNING, "failed to construct request handler", e);
            return;
        }
        executor.execute(request_handler);
        request_handlers.add(request_handler);
    }

    @Override
    protected void finalize() throws Throwable {

        try {
            shutdown();
        }
        finally {
            super.finalize();
        }
    }

    private JsonParser createJsonParser(final Socket socket) throws IOException {

        final BufferedReader buffered_reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), encoding.getJavaName()));
        return json_factory.createParser(buffered_reader);
    }

    private JsonGenerator createJsonGenerator(final Socket socket) throws IOException {

        final BufferedWriter buffered_writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encoding.getJavaName()));
        return json_factory.createGenerator(buffered_writer);
    }

    private class ServerThread extends Thread {

        @Override
        public void run() {

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
            LOGGER.info("server stopped listening for incomming connections");
        }
    }

    private class JsonRpcRequestHandler implements Runnable, Comparable<JsonRpcRequestHandler> {

        private final Long id;
        private final Socket socket;
        private final JsonGenerator json_generator;
        private final JsonParser json_parser;
        private volatile Long request_id;

        public JsonRpcRequestHandler(final Socket socket) throws IOException {

            id = NEXT_REQUEST_HANDLER_ID.getAndIncrement();
            this.socket = socket;
            json_generator = createJsonGenerator(socket);
            json_parser = createJsonParser(socket);
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
            catch (final JsonRpcException e) {
                handleException(e);
            }
            catch (final RuntimeException e) {
                handleException(new ServerRuntimeException(e));
            }
            finally {
                shutdown();
            }
        }

        @Override
        public int compareTo(final JsonRpcRequestHandler other) {

            return id.compareTo(other.id);
        }

        void shutdown() {

            Thread.currentThread().interrupt();
            CloseableUtil.closeQuietly(json_generator, json_parser, CloseableUtil.toCloseable(socket));
            request_handlers.remove(this);
        }

        private void handleException(final JsonRpcException exception) {

            final JsonRpcResponseError error = new JsonRpcResponseError(request_id, exception);
            try {
                writeResponse(error);
            }
            catch (final IOException e) {
                LOGGER.log(Level.FINE, "failed to notify JSON RPC error", e);
            }
        }

        private JsonRpcResponseResult handleRequest(final JsonRpcRequest request) throws ServerException {

            final String method_name = request.getMethodName();
            final Method method = findServiceMethodByName(method_name);
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

        private void setRequestId(final Long request_id) {

            this.request_id = request_id;
        }

        private void resetRequestId() {

            this.request_id = null;
        }

        private JsonRpcRequest readRequest() throws ServerException, ParseException {

            try {
                resetRequestId();
                final JsonRpcRequest request = json_parser.readValueAs(JsonRpcRequest.class);
                setRequestId(request.getId());
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

        private void writeResponse(final JsonRpcResponse response) throws ServerException {

            try {
                json_generator.writeObject(response);
            }
            catch (final IOException e) {
                throw new InternalException(e);
            }
        }
    }
}
