/*
 * Copyright 2013 Masih Hajiarabderkani
 * 
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

import uk.ac.standrews.cs.jetson.exception.JsonRpcError;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public abstract class JsonRpcResponse extends JsonRpcMessage {

    public static final String RESULT_KEY = "result";
    public static final String ERROR_KEY = "error";

    private JsonRpcResponse() {

    }

    private JsonRpcResponse(final Long id) {

        setId(id);
    }

    @JsonPropertyOrder({JsonRpcMessage.VERSION_KEY, JsonRpcResponse.RESULT_KEY, JsonRpcMessage.ID_KEY})
    public static final class JsonRpcResponseResult extends JsonRpcResponse {

        private Object result;

        public JsonRpcResponseResult() {

        }

        public JsonRpcResponseResult(final Long id, final Object result) {

            super(id);
            setResult(result);
        }

        /**
         * Gets the result.
         *
         * @return the result
         */
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
    public static final class JsonRpcResponseError extends JsonRpcResponse {

        private JsonRpcError error;

        public JsonRpcResponseError() {

        }

        public JsonRpcResponseError(final Long id, final JsonRpcError error) {

            super(id);
            setError(error);
        }

        /**
         * Gets the error.
         *
         * @return the error
         */
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
