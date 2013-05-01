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
package com.staticiser.jetson.exception;

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
