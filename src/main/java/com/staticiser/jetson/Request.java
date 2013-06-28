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

import java.lang.reflect.Method;

public class Request extends Message {

    private Method method;
    private Object[] arguments;

    public Request() {

    }

    public Request(Integer id, final Method method, final Object[] arguments) {
        setId(id);
        setMethod(method);
        setArguments(arguments);
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(final Method method) {
        this.method = method;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(final Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    protected synchronized void reset() {
        super.reset();
        setArguments(null);
        setMethod(null);
    }
}
