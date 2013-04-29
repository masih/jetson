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
package uk.ac.standrews.cs.jetson.exception;

import java.io.IOException;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/*
-32700 ---> parse error
-32600 ---> server error
-32500 ---> application error
-32400 ---> system error
-32300 ---> transport error
 */
@JsonSerialize(as = JsonRpcError.class)
public class JsonRpcException extends IOException implements JsonRpcError {

    private static final long serialVersionUID = 2666032021060461206L;
    private int code;
    private Object data;
    private String message;

    public JsonRpcException() {

        super();
    }

    public JsonRpcException(final int code, final String message) {

        super(message);
        initCodeAndMessage(code);
    }

    private void initCodeAndMessage(final int code) {

        setCode(code);
        setMessage(super.getMessage());
    }

    public JsonRpcException(final int code, final Throwable cause) {

        super(cause);
        initCodeAndMessage(code);
    }

    public JsonRpcException(final int code, final String message, final Throwable cause) {

        super(message, cause);
        initCodeAndMessage(code);
    }

    @Override
    public int getCode() {

        return code;
    }

    @Override
    public String getMessage() {

        return message;
    }

    @Override
    public Object getData() {

        return data;
    }

    public void setCode(final int code) {

        this.code = code;
    }

    public void setMessage(final String message) {

        this.message = message;
    }

    public void setData(final Object data) {

        this.data = data;
    }

    protected static String toString(final Object... messages) {

        final String message;
        if (messages != null && messages.length != 0) {
            final StringBuilder builder = new StringBuilder();
            for (final Object msg : messages) {
                builder.append(msg);
            }
            message = builder.toString();
        }
        else {
            message = null;
        }
        return message;
    }
}
