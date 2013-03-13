package uk.ac.standrews.cs.jetson.exception;

public class AccessException extends ServerException {

    private static final long serialVersionUID = -869413349986223849L;
    static final int CODE = -32604;
    private static final String MESSAGE = "cannot access remote method";

    public AccessException() {

        super(CODE, MESSAGE);
    }

    public AccessException(final Throwable cause) {

        super(CODE, cause);
    }
}
