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

public class JsonRpcProxyFactory {

    private static final AtomicLong NEXT_REQUEST_ID = new AtomicLong();

    //TODO implement connection pooling
    public JsonRpcProxyFactory() {

        // TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final InetSocketAddress address, final Class<T> service_interface, final JsonFactory json_factory) throws IllegalArgumentException, IOException {

        //FIXME Cache generated proxies and clone if needed.

        final Map<Method, String> dispatch = ReflectionUtil.mapMethodsToNames(service_interface);

        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{service_interface}, new JsonRpcInvocationHandler(address, json_factory, dispatch));
    }

    private static class JsonRpcInvocationHandler implements InvocationHandler {

        private final InetSocketAddress address;
        private final JsonFactory json_factory;
        private final Map<Method, String> dispatch;

        public JsonRpcInvocationHandler(final InetSocketAddress address, final JsonFactory json_factory, final Map<Method, String> dispatch) throws IOException {

            this.address = address;
            this.json_factory = json_factory;
            this.dispatch = dispatch;
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
                final JsonRpcRequest request = new JsonRpcRequest(NEXT_REQUEST_ID.getAndIncrement(), dispatch.get(method), params);

                writeRequest(json_generator, request);
                final JsonRpcResponse response = readResponse(json_parser);
                if (isResponseError(response)) {
                    final JsonRpcResponseError response_error = toJsonRpcResponseError(response);
                    final JsonRpcException exception = JsonRpcExceptions.fromJsonRpcError(response_error.getError());
                    throw !isInvocationException(exception) ? exception : reconstructException(method.getExceptionTypes(), asInvovationException(exception));
                }
                return JsonRpcResponseResult.class.cast(response).getResult();
            }
            finally {
                CloseableUtil.closeQuietly(json_parser, json_generator, CloseableUtil.toCloseable(socket));
            }
        }

        private InvocationException asInvovationException(final JsonRpcException exception) {

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

        private JsonRpcResponse readResponse(final JsonParser json_parser) throws InvalidResponseException, TransportException {

            try {
                //                final TreeNode readValueAsTree = json_parser.readValueAsTree();
                //                System.out.println(readValueAsTree);
                //                return json_parser.getCodec().treeToValue(readValueAsTree, JsonRpcResponse.class);
                return json_parser.readValueAs(JsonRpcResponse.class);
            }
            catch (final JsonProcessingException e) {
                throw new InvalidResponseException(e);
            }
            catch (final IOException e) {
                throw new TransportException(e);
            }
        }

        private void writeRequest(final JsonGenerator json_generator, final JsonRpcRequest request) throws TransportException {

            try {
                json_generator.writeObject(request);
            }
            catch (final IOException e) {
                throw new TransportException(e);
            }
        }
    }
}
