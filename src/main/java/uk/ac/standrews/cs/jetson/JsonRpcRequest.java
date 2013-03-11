package uk.ac.standrews.cs.jetson;

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

    JsonRpcRequest() { // Empty constructor is required by Jackson framework

    }

    JsonRpcRequest(final Long id, final String method_name, final Object... params) {

        setId(id);
        setMethodName(method_name);
        setParams(params);
    }

    @JsonProperty(METHOD_KEY)
    @JsonInclude(Include.ALWAYS)
    public String getMethodName() {

        return method_name;
    }

    @JsonProperty(PARAMETERS_KEY)
    @JsonInclude(Include.NON_NULL)
    public Object[] getParameters() {

        return getParams();
    }

    public void setMethodName(final String method_name) {

        this.method_name = method_name;
    }

    public Object[] getParams() {

        return params;
    }

    public void setParams(final Object... params) {

        this.params = params;
    }
}
