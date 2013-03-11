package uk.ac.standrews.cs.jetson.exception;

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
