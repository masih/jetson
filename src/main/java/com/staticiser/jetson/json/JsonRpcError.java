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
package com.staticiser.jetson.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Presents a JSON RPC error object as described in the <a href="http://www.jsonrpc.org/specification#error_object"> JSON RPC specification</a>.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
class JsonRpcError {

    /** The JSON code key. */
    private static final String CODE_KEY = "code";
    /** The JSON message key. */
    private static final String MESSAGE_KEY = "message";
    /** The JSON data key. */
    private static final String DATA_KEY = "data";
    private volatile int code;
    private volatile String message;
    private volatile Object object;

    JsonRpcError() {

    }

    JsonRpcError(final int code, final String message, final Object object) {

        this.code = code;
        this.message = message;
        this.object = object;
    }

    void setCode(final int code) {

        this.code = code;
    }

    void setMessage(final String message) {

        this.message = message;
    }

    void setData(final Object object) {

        this.object = object;
    }

    /**
     * Gets the error code.
     *
     * @return the code
     */
    @JsonProperty(CODE_KEY)
    int getCode() {

        return code;
    }

    /**
     * Gets the short description of the error.
     *
     * @return the short description of the error
     */
    @JsonProperty(MESSAGE_KEY)
    String getMessage() {

        return message;
    }

    /**
     * Gets the additional information about the error.
     *
     * @return the additional information about the error
     */
    @JsonProperty(value = DATA_KEY)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
    @JsonInclude(Include.NON_NULL)
    Object getData() {

        return object;
    }

    void reset() {

        setCode(0);
        setMessage(null);
        setData(null);
    }
}
