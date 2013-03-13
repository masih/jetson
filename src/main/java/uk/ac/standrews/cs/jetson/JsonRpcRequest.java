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
