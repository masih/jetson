package uk.ac.standrews.cs.jetson.exception;

import java.util.HashMap;
import java.util.Map;

public final class JsonRpcExceptions {

    private static final Map<Integer, Class<? extends JsonRpcException>> CODE_TO_EXCEPTION = new HashMap<Integer, Class<? extends JsonRpcException>>();
    static {
        CODE_TO_EXCEPTION.put(AccessException.CODE, AccessException.class);
        CODE_TO_EXCEPTION.put(InternalException.CODE, InternalException.class);
        CODE_TO_EXCEPTION.put(InvalidJsonException.CODE, InvalidJsonException.class);
        CODE_TO_EXCEPTION.put(InvalidParameterException.CODE, InvalidParameterException.class);
        CODE_TO_EXCEPTION.put(InvalidRequestException.CODE, InvalidRequestException.class);
        CODE_TO_EXCEPTION.put(InvalidResponseException.CODE, InvalidResponseException.class);
        CODE_TO_EXCEPTION.put(InvocationException.CODE, InvocationException.class);
        CODE_TO_EXCEPTION.put(MethodNotFoundException.CODE, MethodNotFoundException.class);
        CODE_TO_EXCEPTION.put(ServerRuntimeException.CODE, ServerRuntimeException.class);
        CODE_TO_EXCEPTION.put(TransportException.CODE, TransportException.class);
        //        CODE_TO_EXCEPTION.put(UnexpectedException.CODE,UnexpectedException.class);
    }

    public static JsonRpcException fromJsonRpcError(final JsonRpcError error) {

        final Integer code = error.getCode();
        final JsonRpcException exception = isRegistered(code) ? attemptCodeBasedInstantiation(code) : new JsonRpcException();
        exception.setCode(code);
        exception.setMessage(error.getMessage());
        exception.setData(error.getData());
        return exception;
    }

    private static boolean isRegistered(final Integer code) {

        return CODE_TO_EXCEPTION.containsKey(code);
    }

    private static JsonRpcException attemptCodeBasedInstantiation(final Integer code) {

        assert isRegistered(code);
        try {
            return CODE_TO_EXCEPTION.get(code).newInstance();
        }
        catch (final Exception e) {
            return new JsonRpcException();
        }
    }
}
