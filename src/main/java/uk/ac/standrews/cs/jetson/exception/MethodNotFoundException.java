package uk.ac.standrews.cs.jetson.exception;

public class MethodNotFoundException extends ServerException {

    private static final long serialVersionUID = 4963177125593522650L;
    public static final int CODE = -32601;
    private static final String METHOD_NOT_FOUND_ERROR_MESSAGE = "Method not found";

    public MethodNotFoundException() {

        super(CODE, METHOD_NOT_FOUND_ERROR_MESSAGE);
    }
}
