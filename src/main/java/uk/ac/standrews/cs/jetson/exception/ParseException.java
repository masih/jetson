package uk.ac.standrews.cs.jetson.exception;

public abstract class ParseException extends JsonRpcException {

    private static final long serialVersionUID = -2941914104875913075L;

    protected ParseException() {

        super();
    }

    protected ParseException(final int code,  final String message) {
        super(code, message);
    }
    protected ParseException(final int code, final Throwable cause) {

        super(code, cause);
    }
}
