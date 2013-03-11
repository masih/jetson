package uk.ac.standrews.cs.jetson.exception;

public class UnexpectedException extends JsonRpcException {

    private static final long serialVersionUID = -4415616684672785380L;
    public static final int CODE = -32501;

    public UnexpectedException(final Throwable cause) {
        this(cause.getMessage());
    }
    public UnexpectedException(final String message) {

        super(CODE, message);
    }
}
