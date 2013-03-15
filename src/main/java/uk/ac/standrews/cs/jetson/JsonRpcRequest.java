/*
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

@JsonPropertyOrder({JsonRpcMessage.VERSION_KEY, JsonRpcRequest.METHOD_KEY, JsonRpcRequest.PARAMETERS_KEY, JsonRpcMessage.ID_KEY})
class JsonRpcRequest extends JsonRpcMessage {

    static final String PARAMETERS_KEY = "params";
    static final String METHOD_KEY = "method";

    private String method_name;
    private Object[] params;
    private Method target_method;

    JsonRpcRequest() {

    }

    JsonRpcRequest(final Long id, final Method target_method, final String method_name, final Object... params) {

        setId(id);
        setTargetMethod(target_method);
        setMethodName(method_name);
        setParams(params);
    }

    void setTargetMethod(final Method target_method) {

        this.target_method = target_method;
    }

    @JsonProperty(METHOD_KEY)
    @JsonInclude(Include.ALWAYS)
    public String getMethodName() {

        return method_name;
    }

    @JsonProperty(PARAMETERS_KEY)
    @JsonInclude(Include.NON_NULL)
    public Object[] getParameters() {

        return params;
    }

    public void setMethodName(final String method_name) {

        this.method_name = method_name;
    }

    public void setParams(final Object... params) {

        this.params = params;
    }

    @JsonIgnore
    public Method getTargetMethod() {

        return target_method;
    }
}
