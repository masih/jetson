package uk.ac.standrews.cs.jetson;

import uk.ac.standrews.cs.jetson.exception.JsonRpcError;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

abstract class JsonRpcResponse extends JsonRpcMessage {

    static final String RESULT_KEY = "result";
    static final String ERROR_KEY = "error";

    private JsonRpcResponse() {

    }

    private JsonRpcResponse(final Long id) {

        setId(id);
    }

    @JsonPropertyOrder({JsonRpcMessage.VERSION_KEY, JsonRpcResponse.RESULT_KEY, JsonRpcMessage.ID_KEY})
    static final class JsonRpcResponseResult extends JsonRpcResponse {

        private Object result;

        JsonRpcResponseResult() {

        }

        JsonRpcResponseResult(final Long id, final Object result) {

            super(id);
            setResult(result);
        }

        @JsonProperty(RESULT_KEY)
        @JsonInclude(Include.ALWAYS)
        public Object getResult() {

            return result;
        }

        public void setResult(final Object result) {

            this.result = result;
        }
    }

    @JsonPropertyOrder({JsonRpcMessage.VERSION_KEY, JsonRpcResponse.ERROR_KEY, JsonRpcMessage.ID_KEY})
    static final class JsonRpcResponseError extends JsonRpcResponse {

        private JsonRpcError error;

        JsonRpcResponseError() {

        }

        JsonRpcResponseError(final Long id, final JsonRpcError error) {

            super(id);
            setError(error);
        }

        @JsonProperty(ERROR_KEY)
        @JsonInclude(Include.ALWAYS)
        public JsonRpcError getError() {

            return error;
        }

        public void setError(final JsonRpcError error) {

            this.error = error;
        }
    }
}
