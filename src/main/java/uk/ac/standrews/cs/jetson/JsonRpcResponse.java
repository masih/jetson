package uk.ac.standrews.cs.jetson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

//FIXME implement serializer and get rid of type proerty
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = JsonRpcResponseResult.class, visible = false)
@JsonSubTypes({@JsonSubTypes.Type(value = JsonRpcResponseResult.class, name = "result"), @JsonSubTypes.Type(value = JsonRpcResponseError.class, name = "error")})
abstract class JsonRpcResponse extends JsonRpcMessage {

    static final String RESULT_KEY = "result";
    static final String ERROR_KEY = "error";

    JsonRpcResponse() {

    }

    JsonRpcResponse(final Long id) {

        setId(id);
    }
}
