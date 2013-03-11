package uk.ac.standrews.cs.jetson;

import uk.ac.standrews.cs.jetson.exception.JsonRpcError;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonPropertyOrder({JsonRpcMessage.VERSION_KEY, JsonRpcResponse.ERROR_KEY, JsonRpcMessage.ID_KEY})
final class JsonRpcResponseError extends JsonRpcResponse {

    private JsonRpcError error;

    JsonRpcResponseError() {

    }

    JsonRpcResponseError(final Long id, final JsonRpcError error) {

        super(id);
        setError(error);
    }

    @JsonProperty(ERROR_KEY)
    @JsonInclude(Include.ALWAYS)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    public JsonRpcError getError() {

        return error;
    }

    public void setError(final JsonRpcError error) {

        this.error = error;
    }
}
