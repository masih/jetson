package com.staticiser.jetson;

import io.netty.channel.Channel;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class FutureResponse implements Future<Object> {

    private final CountDownLatch job_done_latch;
    private final Request request;
    private final Channel channel;
    private State current_state;
    private volatile Throwable error;
    private volatile Object result;

    public FutureResponse(Request request, final Channel channel) {
        this.request = request;
        this.channel = channel;
        current_state = State.PENDING;
        job_done_latch = new CountDownLatch(1);
    }

    @Override
    public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        if (isDone()) { return false; }
        return updateState(State.CANCELLED);
    }

    @Override
    public boolean isCancelled() {
        return current_state == State.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return current_state != State.PENDING;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        job_done_latch.await(); // Wait until the job is done

        switch (current_state) {
            case DONE_WITH_RESULT:
                return result;

            case DONE_WITH_EXCEPTION:
                throw new ExecutionException(error);
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

    public Channel getChannel() {
        return channel;
    }

    public void setException(final Throwable exception) {
        this.error = exception;
        updateState(State.DONE_WITH_EXCEPTION);
    }

    public Request getRequest() {
        return request;
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

    synchronized void setResponse(Response response) {
        if (response.isError()) {
            setException(response.getException());
        }
        else {
            result = response.getResult();
            updateState(State.DONE_WITH_RESULT);
        }
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

