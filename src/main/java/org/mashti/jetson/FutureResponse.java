package org.mashti.jetson;

import com.google.common.util.concurrent.AbstractFuture;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class FutureResponse extends AbstractFuture<Object> implements Comparable<FutureResponse>, WrittenByteCountListenner {

    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    private final Integer id;
    private volatile Method method;
    private volatile Object[] arguments;
    private volatile WrittenByteCountListenner written_byte_count_listener;

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
    public final boolean set(final Object value) {

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

    @Override
    public int hashCode() {

        return id.hashCode();
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof FutureResponse)) { return false; }
        final FutureResponse that = (FutureResponse) other;
        return id.equals(that.id);
    }

    @Override
    public synchronized void notifyWrittenByteCount(int count) {

        if (written_byte_count_listener != null) {
            written_byte_count_listener.notifyWrittenByteCount(count);
        }
    }

    synchronized void setWrittenByteCountListener(WrittenByteCountListenner written_byte_count_listener) {

        this.written_byte_count_listener = written_byte_count_listener;
    }
}
