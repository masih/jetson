package com.staticiser.jetson;

import com.google.common.util.concurrent.AbstractFuture;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class FutureResponse<Result> extends AbstractFuture<Result> implements Comparable<FutureResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FutureResponse.class);
    private final Integer id;
    private volatile Method method;
    private volatile Object[] arguments;

    FutureResponse(final Integer id) {

        this.id = id;

    }

    public FutureResponse(final Integer id, final Method method, final Object[] arguments) {

        this.id = id;
        this.method = method;
        this.arguments = arguments;
    }

    public Integer getId() {

        return id;
    }

    public Method getMethod() {

        return method;
    }

    public Object[] getArguments() {

        return arguments;
    }

    public void setMethod(final Method method) {

        this.method = method;
    }

    public void setArguments(final Object[] arguments) {

        this.arguments = arguments;
    }

    @Override
    public boolean set(final Result value) {

        return super.set(value);
    }

    @Override
    public boolean setException(final Throwable exception) {

        return super.setException(exception);
    }

    @Override
    public int compareTo(final FutureResponse o) {

        return getId().compareTo(o.getId());
    }
}
