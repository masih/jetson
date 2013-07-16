/*
 * Copyright 2013 Masih Hajiarabderkani
 *
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
package org.mashti.jetson.json;

import java.util.HashMap;
import java.util.Map;
import org.mashti.jetson.exception.IllegalAccessException;
import org.mashti.jetson.exception.InternalServerException;
import org.mashti.jetson.exception.InvalidRequestException;
import org.mashti.jetson.exception.MethodNotFoundException;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.ServerRuntimeException;
import org.mashti.jetson.exception.TransportException;

/**
 * A utility class for converting JSON RPC error codes to {@link RPCException exceptions}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
final class JsonRpcExceptions {

    private static final Map<Integer, Class<? extends Throwable>> JSON_RPC_ERROR_CODES = new HashMap<Integer, Class<? extends Throwable>>();
    private static final Integer JSON_RPC_APPLICATION_ERROR_CODE = -32500;
    static {
        JSON_RPC_ERROR_CODES.put(-32300, TransportException.class);
        JSON_RPC_ERROR_CODES.put(-32600, InvalidRequestException.class);
        JSON_RPC_ERROR_CODES.put(-32601, MethodNotFoundException.class);
        JSON_RPC_ERROR_CODES.put(-32602, org.mashti.jetson.exception.IllegalArgumentException.class);
        JSON_RPC_ERROR_CODES.put(-32603, InternalServerException.class);
        JSON_RPC_ERROR_CODES.put(-32604, IllegalAccessException.class);
        JSON_RPC_ERROR_CODES.put(-32605, ServerRuntimeException.class);
    }

    private JsonRpcExceptions() {

    }

    /**
     * Produces a {@link RPCException} from a given {@link JsonRpcError}.
     *
     * @param error the error
     * @return the exception
     */
    public static Throwable fromJsonRpcError(final JsonRpcError error) {

        final Integer code = error.getCode();
        final String message = error.getMessage();
        final Object data = error.getData();
        if (isRegistered(code)) { return attemptCodeBasedInstantiation(code, message); }
        if (code.equals(JSON_RPC_APPLICATION_ERROR_CODE) && data instanceof Throwable) { return (Throwable) data; }
        return new RPCException(message);
    }

    public static JsonRpcError toJsonRpcError(final Throwable throwable) {

        final Integer code;
        final Object data;
        final Class<? extends Throwable> throwable_class = throwable.getClass();
        if (isRegistered(throwable_class)) {
            code = getCode(throwable_class);
            data = null;
        }
        else {
            code = JSON_RPC_APPLICATION_ERROR_CODE;
            data = throwable;
        }

        return new JsonRpcError(code, throwable.getMessage(), data);

    }

    private static Integer getCode(final Class<? extends Throwable> throwable_class) {

        assert isRegistered(throwable_class);
        for (final Map.Entry<Integer, Class<? extends Throwable>> entry : JSON_RPC_ERROR_CODES.entrySet()) {
            if (entry.getValue().equals(throwable_class)) { return entry.getKey(); }
        }
        return null;
    }

    private static boolean isRegistered(final Class<? extends Throwable> throwable_class) {

        return JSON_RPC_ERROR_CODES.containsValue(throwable_class);
    }

    private static boolean isRegistered(final Integer code) {

        return JSON_RPC_ERROR_CODES.containsKey(code);
    }

    private static Throwable attemptCodeBasedInstantiation(final Integer code, final String message) {

        assert isRegistered(code);
        final Class<? extends Throwable> throwable_class = JSON_RPC_ERROR_CODES.get(code);
        try {
            return throwable_class.getConstructor(String.class).newInstance(message);
        }
        catch (final Exception e) {
            try {
                return throwable_class.newInstance();
            }
            catch (final Exception e1) {
                return new RPCException("failed to reconstruct error", e);
            }
        }
    }
}
