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
package com.staticiser.jetson.exception;

public class InternalException extends ServerException {

    private static final long serialVersionUID = -7936488738060772425L;
    public static final int CODE = -32603;
    private static final String MESSAGE = "Internal server error";

    public InternalException() {

        super(CODE, MESSAGE);
    }

    public InternalException(final Throwable cause) {

        super(CODE, cause);
    }
}
