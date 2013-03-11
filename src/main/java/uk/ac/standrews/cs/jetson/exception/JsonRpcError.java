package uk.ac.standrews.cs.jetson.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public interface JsonRpcError {

    String CODE_KEY = "code";
    String MESSAGE_KEY = "message";
    String DATA_KEY = "data";

    @JsonProperty(CODE_KEY)
    int getCode();

    @JsonProperty(MESSAGE_KEY)
    String getMessage();

    @JsonProperty(value = DATA_KEY)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
    @JsonInclude(Include.NON_NULL)
    Object getData();
}
