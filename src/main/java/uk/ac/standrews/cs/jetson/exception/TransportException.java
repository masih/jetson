package uk.ac.standrews.cs.jetson.exception;

public class TransportException extends JsonRpcException {

    private static final long serialVersionUID = 5882567563244238779L;
    static final int CODE = -32300;
    private static final String MESSAGE = null;

    public TransportException() {

        super(CODE, MESSAGE);
    }

    public TransportException(final Throwable cause) {

        super(CODE, cause);
    }
}
