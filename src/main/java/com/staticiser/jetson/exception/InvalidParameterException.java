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

public class InvalidParameterException extends ServerException {

    private static final long serialVersionUID = 7924439351907808359L;
    public static final int CODE = -32602;
    private static final String MESSAGE = "Invalid method parameters";

    public InvalidParameterException() {

        super(CODE, MESSAGE);
    }

    public InvalidParameterException(final Throwable cause) {

        super(CODE, cause);
    }
}
