/**
 * This file is part of jetson.
 *
 * jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jetson.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mashti.jetson.exception;

import java.lang.reflect.Type;

public class UnknownTypeException extends RPCException {

    public UnknownTypeException() {

        super();
    }

    public UnknownTypeException(final Type unknown_type) {

        super("no codec is registered for type: " + unknown_type);
    }

    public UnknownTypeException(final String message) {

        super(message);
    }

    public UnknownTypeException(final Throwable cause) {

        super(cause);
    }

    public UnknownTypeException(final String message, final Throwable cause) {

        super(message, cause);
    }
}
