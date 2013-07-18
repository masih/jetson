package org.mashti.jetson;

import com.google.common.util.concurrent.AbstractFuture;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class FutureResponse<Result> extends AbstractFuture<Result> implements Comparable<FutureResponse> {

    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    private final Integer id;
    private volatile Method method;
    private volatile Object[] arguments;

    public FutureResponse() {

        this(NEXT_ID.incrementAndGet());
    }

    public FutureResponse(Method method, Object... arguments) {

        this(NEXT_ID.incrementAndGet());
        setMethod(method);
        setArguments(arguments);
    }

    protected FutureResponse(final Integer id) {

        this.id = id;
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
    public final boolean set(final Result value) {

        return super.set(value);
    }

    @Override
    public final boolean setException(final Throwable exception) {

        return super.setException(exception);
    }

    @Override
    public int compareTo(final FutureResponse o) {

        return getId().compareTo(o.getId());
    }
}
