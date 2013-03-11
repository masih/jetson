package uk.ac.standrews.cs.jetson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

abstract class JsonRpcMessage {

    static final String ID_KEY = "id";
    static final String VERSION_KEY = "jsonrpc";
    static final String DEFAULT_VERSION = "2.0";
    private String version;
    private Long id;

    JsonRpcMessage() {

        setVersion(DEFAULT_VERSION);
    }

    @JsonProperty(ID_KEY)
    @JsonInclude(Include.ALWAYS)
    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    @JsonProperty(VERSION_KEY)
    @JsonInclude(Include.ALWAYS)
    public String getVersion() {

        return version;
    }

    public void setVersion(final String version) {

        this.version = version;
    }
}
