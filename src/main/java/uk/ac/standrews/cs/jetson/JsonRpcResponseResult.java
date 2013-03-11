package uk.ac.standrews.cs.jetson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonPropertyOrder({JsonRpcMessage.VERSION_KEY, JsonRpcResponse.RESULT_KEY, JsonRpcMessage.ID_KEY})
final class JsonRpcResponseResult extends JsonRpcResponse {

    private Object result;

    JsonRpcResponseResult() {

    }

    JsonRpcResponseResult(final Long id, final Object result) {

        super(id);
        setResult(result);
    }

    @JsonProperty(RESULT_KEY)
    @JsonInclude(Include.ALWAYS)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    public Object getResult() {

        return result;
    }

    public void setResult(final Object result) {

        this.result = result;
    }
}
