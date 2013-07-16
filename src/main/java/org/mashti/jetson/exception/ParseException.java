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
package org.mashti.jetson.exception;

public class ParseException extends RPCException {

    private static final long serialVersionUID = 5797975215122969810L;

    ParseException() {

        super();
    }

    public ParseException(final Throwable cause) {

        super(cause);
    }

    ParseException(final String message) {

        super(message);
    }

    ParseException(final String message, final Throwable cause) {

        super(message, cause);
    }
}
