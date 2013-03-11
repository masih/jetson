package uk.ac.standrews.cs.jetson.exception;

import com.fasterxml.jackson.core.JsonParseException;

public class InvalidJsonException extends ParseException {

    private static final long serialVersionUID = -8074257320543449403L;
    public static final int CODE = -32700;
    private static final String MESSAGE = "JSON request not well formed";

    public InvalidJsonException() {

        super(CODE, MESSAGE);
    }

    public InvalidJsonException(final JsonParseException cause) {

        super(CODE, cause);
    }
}
