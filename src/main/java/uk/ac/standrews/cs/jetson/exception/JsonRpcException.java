package uk.ac.standrews.cs.jetson.exception;

import java.io.IOException;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/*
-32700 ---> parse error
-32600 ---> server error
-32500 ---> application error
-32400 ---> system error
-32300 ---> transport error
 */
@JsonSerialize(as = JsonRpcError.class)
public class JsonRpcException extends IOException implements JsonRpcError {

    private static final long serialVersionUID = 2666032021060461206L;
    private int code;
    private Object data;
    private String message;

    public JsonRpcException() {

        super();
    }

    public JsonRpcException(final int code, final String message) {

        super(message);
        setCode(code);
        setMessage(super.getMessage());
    }

    public JsonRpcException(final int code, final Throwable cause) {

        super(cause);
        setCode(code);
        setMessage(super.getMessage());
    }

    @Override
    public int getCode() {

        return code;
    }

    @Override
    public String getMessage() {

        return message;
    }

    @Override
    public Object getData() {

        return data;
    }

    public void setCode(final int code) {

        this.code = code;
    }

    public void setMessage(final String message) {

        this.message = message;
    }

    public void setData(final Object data) {

        this.data = data;
    }
}
