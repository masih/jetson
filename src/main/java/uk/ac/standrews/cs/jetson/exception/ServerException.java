package uk.ac.standrews.cs.jetson.exception;

public abstract class ServerException extends JsonRpcException {

    private static final long serialVersionUID = -3781403268838978789L;

    protected ServerException() {

        super();
    }

    protected ServerException(final int code, final String message) {

        super(code, message);
    }

    protected ServerException(final int code, final Throwable cause) {

        super(code, cause);
    }
}
