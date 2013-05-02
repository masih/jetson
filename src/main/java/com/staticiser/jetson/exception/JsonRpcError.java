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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Presents a JSON RPC error object as described in the <a href="http://www.jsonrpc.org/specification#error_object">specifications</a>.
 * 
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface JsonRpcError {

    /** The JSON code key. */
    String CODE_KEY = "code";

    /** The JSON message key. */
    String MESSAGE_KEY = "message";

    /** The JSON data key. */
    String DATA_KEY = "data";

    /**
     * Gets the error code.
     *
     * @return the code
     */
    @JsonProperty(CODE_KEY)
    int getCode();

    /**
     * Gets the short description of the error.
     *
     * @return the short description of the error
     */
    @JsonProperty(MESSAGE_KEY)
    String getMessage();

    /**
     * Gets the additional information about the error.
     *
     * @return the additional information about the error
     */
    @JsonProperty(value = DATA_KEY)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
    @JsonInclude(Include.NON_NULL)
    Object getData();
}
