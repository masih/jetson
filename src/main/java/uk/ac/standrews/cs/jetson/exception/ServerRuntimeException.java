package uk.ac.standrews.cs.jetson.exception;

public class ServerRuntimeException extends ServerException {

    private static final long serialVersionUID = -869413349986223849L;
    static final int CODE = -326054;

    public ServerRuntimeException() {

        setCode(CODE);
    }

    public ServerRuntimeException(final Throwable cause) {

        super(CODE, cause);
    }
}
