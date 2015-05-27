/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mashti.jetson.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Presents a JSON RPC error object as described in the <a href="http://www.jsonrpc.org/specification#error_object"> JSON RPC specification</a>.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
class JsonRpcError {

    /** The JSON code key. */
    private static final String CODE_KEY = "code";
    /** The JSON message key. */
    private static final String MESSAGE_KEY = "message";
    /** The JSON data key. */
    private static final String DATA_KEY = "data";
    private volatile int code;
    private volatile String message;
    private volatile Object object;

    JsonRpcError() {

    }

    JsonRpcError(final int code, final String message, final Object object) {

        this.code = code;
        this.message = message;
        this.object = object;
    }

    void setCode(final int code) {

        this.code = code;
    }

    void setMessage(final String message) {

        this.message = message;
    }

    void setData(final Object object) {

        this.object = object;
    }

    /**
     * Gets the error code.
     *
     * @return the code
     */
    @JsonProperty(CODE_KEY)
    int getCode() {

        return code;
    }

    /**
     * Gets the short description of the error.
     *
     * @return the short description of the error
     */
    @JsonProperty(MESSAGE_KEY)
    String getMessage() {

        return message;
    }

    /**
     * Gets the additional information about the error.
     *
     * @return the additional information about the error
     */
    @JsonProperty(value = DATA_KEY)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
    @JsonInclude(Include.NON_NULL)
    Object getData() {

        return object;
    }

    void reset() {

        setCode(0);
        setMessage(null);
        setData(null);
    }
}
