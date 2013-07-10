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

import java.io.IOException;

/**
 * The base of all Remote Procedure Call exception.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RPCException extends IOException {

    private static final long serialVersionUID = 2666032021060461206L;

    public RPCException() {

        super();
    }

    public RPCException(final String message) {

        super(message);
    }

    public RPCException(final Throwable cause) {

        super(cause);
    }

    public RPCException(final String message, final Throwable cause) {

        super(message, cause);
    }

    static String toString(final Object... messages) {

        final String message;
        if (messages != null && messages.length != 0) {
            final StringBuilder builder = new StringBuilder();
            for (final Object msg : messages) {
                builder.append(msg);
            }
            message = builder.toString();
        }
        else {
            message = null;
        }
        return message;
    }
}
