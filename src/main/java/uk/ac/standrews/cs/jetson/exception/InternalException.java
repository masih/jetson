package uk.ac.standrews.cs.jetson.exception;

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
