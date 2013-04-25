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
package uk.ac.standrews.cs.jetson;

import java.lang.reflect.Method;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The Class JsonRpcRequest.
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@JsonPropertyOrder({JsonRpcMessage.VERSION_KEY, JsonRpcRequest.METHOD_NAME_KEY, JsonRpcRequest.PARAMETERS_KEY, JsonRpcMessage.ID_KEY})
public class JsonRpcRequest extends JsonRpcMessage {

    static final String PARAMETERS_KEY = "params";
    static final String METHOD_NAME_KEY = "method";

    private String method_name;
    private Object[] params;
    private Method method;

    JsonRpcRequest() {

    }

    JsonRpcRequest(final Long id, final Method target_method, final String method_name, final Object... params) {

        setId(id);
        setMethod(target_method);
        setMethodName(method_name);
        setParams(params);
    }

    void setMethod(final Method target_method) {

        this.method = target_method;
    }

    /**
     * Gets the method name.
     *
     * @return the method name
     */
    @JsonProperty(METHOD_NAME_KEY)
    @JsonInclude(Include.ALWAYS)
    String getMethodName() {

        return method_name;
    }

    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
    @JsonProperty(PARAMETERS_KEY)
    @JsonInclude(Include.NON_NULL)
    Object[] getParameters() {

        return params == null ? null : params.clone();
    }

    void setMethodName(final String method_name) {

        this.method_name = method_name;
    }

    void setParams(final Object... params) {

        this.params = params;
    }

    /**
     * Gets the method.
     *
     * @return the method
     */
    @JsonIgnore
    Method getMethod() {

        return method;
    }
}
