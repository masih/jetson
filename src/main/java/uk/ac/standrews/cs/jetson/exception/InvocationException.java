package uk.ac.standrews.cs.jetson.exception;

import java.lang.reflect.InvocationTargetException;

public class InvocationException extends ServerException {

    private static final long serialVersionUID = 8618421321786954804L;
    public static final int CODE = -32606;

    public InvocationException() {

        setCode(CODE);
    }

    public InvocationException(final InvocationTargetException cause) {

        this(cause.getCause());
    }

    private InvocationException(final Throwable cause) {

        super(CODE, cause.getMessage());
        setData(cause); // this is a serialization issue FIXME fix this
    }
}

