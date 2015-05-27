/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
