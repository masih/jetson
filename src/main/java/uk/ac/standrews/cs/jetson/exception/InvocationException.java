/*
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
package uk.ac.standrews.cs.jetson.exception;

import java.lang.reflect.InvocationTargetException;

public class InvocationException extends ServerException {

    private static final long serialVersionUID = 8618421321786954804L;
    public static final int CODE = -32606;

    public InvocationException() {

        setCode(CODE);
    }

    public InvocationException(final InvocationTargetException cause) {

        this(cause.getCause());
    }

    private InvocationException(final Throwable cause) {

        super(CODE, cause.getMessage());
        setData(cause); // this is a serialization issue FIXME fix this
    }
}

