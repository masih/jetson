package uk.ac.standrews.cs.jetson.exception;

public class InvalidResponseException extends JsonRpcException {

    private static final long serialVersionUID = 5797975215122969810L;
    public static final int CODE = -32501;
    private static final String MESSAGE = "Invalid Response";

    public InvalidResponseException() {

        super(CODE, MESSAGE);
    }

    public InvalidResponseException(final Throwable cause) {

        super(CODE, cause);
    }
}
