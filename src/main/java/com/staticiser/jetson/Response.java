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
package com.staticiser.jetson;

public class Response extends Message {

    private Object result;
    private Throwable error;

    public Response() {

    }

    public Object getResult() {

        return result;
    }

    public void setResult(final Object result) {

        this.result = result;
    }

    public Throwable getException() {

        return error;
    }

    public boolean isError() {

        return error != null;
    }

    public void setException(final Throwable error) {

        this.error = error;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Response{");
        sb.append("result=").append(result);
        sb.append(", error=").append(error);
        sb.append('}');
        return sb.toString();
    }

    @Override
    protected void reset() {

        super.reset();
        setResult(null);
        setException(null);
    }
}
