package uk.ac.standrews.cs.jetson.exception;

public class InvalidRequestException extends ServerException {

    private static final long serialVersionUID = 4266496242494342297L;
    public static final int CODE = -32600;
    private static final String MESSAGE = "Invalid Request";

    public InvalidRequestException() {

        super(CODE, MESSAGE);
    }

    public InvalidRequestException(final Throwable cause) {

        super(CODE, cause);
    }
}
