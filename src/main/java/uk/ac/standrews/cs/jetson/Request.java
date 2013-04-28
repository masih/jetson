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

import java.lang.reflect.Method;

class Request extends Message {

    static final String PARAMETERS_KEY = "params";
    static final String METHOD_NAME_KEY = "method";

    private String method_name;
    private Object[] params;
    private transient Method method;

    Request() {

    }

    Request(final Long id, final Method target_method, final String method_name, final Object[] params) {

        setId(id);
        setMethod(target_method);
        setMethodName(method_name);
        setParams(params);
    }

    @Override
    protected void reset() {

        super.reset();
        setMethod(null);
        setMethodName(null);
        setParams(null);
    }

    void setMethod(final Method target_method) {

        this.method = target_method;
    }

    String getMethodName() {

        return method_name;
    }

    Object[] getParameters() {

        return params == null ? null : params.clone();
    }

    void setMethodName(final String method_name) {

        this.method_name = method_name;
    }

    void setParams(final Object[] params) {

        this.params = params;
    }

    Method getMethod() {

        return method;
    }
}
