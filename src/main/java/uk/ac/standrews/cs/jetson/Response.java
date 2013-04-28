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

class Response extends Message {

    static final String RESULT_KEY = "result";
    static final String ERROR_KEY = "error";

    private Object result;
    private JsonRpcError error;

    Response() {

    }

    Object getResult() {

        return result;
    }

    void setResult(final Object result) {

        this.result = result;
    }

    JsonRpcError getError() {

        return error;
    }

    void setError(final JsonRpcError error) {

        this.error = error;
    }

    boolean isError() {

        return error != null;
    }

    @Override
    protected void reset() {

        super.reset();
        setResult(null);
        setError(null);
    }
}
