package com.staticiser.jetson;

import io.netty.channel.Channel;
import java.lang.reflect.Method;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class FutureResponse implements Future<Object> {

    private final CountDownLatch job_done_latch;
    private final Channel channel;
    private volatile Integer id;
    private volatile Method method;
    private volatile Object[] arguments;
    private volatile State current_state;
    private volatile Throwable exception;
    private volatile Object result;

    public FutureResponse(final Channel channel) {

        this(channel, null, null, null);
    }

    public FutureResponse(Channel channel, final Integer id, final Method method, final Object[] arguments) {

        this.channel = channel;
        this.id = id;
        this.method = method;
        this.arguments = arguments;
        current_state = State.PENDING;
        job_done_latch = new CountDownLatch(1);
    }

    @Override
    public synchronized boolean cancel(final boolean interrupt) {

        if (isDone()) { return false; }
        return updateState(State.CANCELLED);
    }

    @Override
    public synchronized boolean isCancelled() {

        return current_state == State.CANCELLED;
    }

    @Override
    public synchronized boolean isDone() {

        return current_state != State.PENDING;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {

        job_done_latch.await(); // Wait until the job is done

        switch (current_state) {
            case DONE_WITH_RESULT:
                return result;
                // FIXME cache exceptions
            case DONE_WITH_EXCEPTION:
                throw new ExecutionException(exception);
            case CANCELLED:
                throw new CancellationException();
            default:
                throw new IllegalStateException("The latch count is zero when the job is not done");
        }

    }

    @Override
    public Object get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

        if (job_done_latch.await(timeout, unit)) { return get(); }

        throw new TimeoutException();
    }

    public Integer getId() {

        return id;
    }

    public void setId(final Integer id) {

        this.id = id;
    }

    public Channel getChannel() {

        return channel;
    }

    public synchronized void setResult(Object result) {

        this.result = result;
        updateState(State.DONE_WITH_RESULT);
    }

    public synchronized void setException(Throwable exception) {

        this.exception = exception;
        updateState(State.DONE_WITH_EXCEPTION);
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

    private synchronized boolean updateState(final State new_state) {

        if (isDone()) { return false; }

        State old_state = current_state;
        current_state = new_state;

        if (current_state != State.PENDING) { // Check whether this future is no longer pending
            job_done_latch.countDown(); // Release the waiting latch
        }
        return old_state == current_state;
    }

    private enum State {

        /** Indicates that this future is pending for the notification from the remote worker. */
        PENDING,

        /** Indicates that pending has ended in a result. */
        DONE_WITH_RESULT,

        /** Indicates that pending has ended in an exception. */
        DONE_WITH_EXCEPTION,

        /** Indicates that pending has ended in cancellation of the job. */
        CANCELLED;
    }
}
