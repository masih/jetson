/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mashti.jetson;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class FutureResponse<Result> extends CompletableFuture<Result> implements Comparable<FutureResponse<?>>, WrittenByteCountListener {

    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    private final Integer id;
    private volatile Method method;
    private volatile Object[] arguments;
    private volatile WrittenByteCountListener written_byte_count_listener;

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
    public int compareTo(final FutureResponse<?> o) {

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
        final FutureResponse<?> that = (FutureResponse<?>) other;
        return id.equals(that.id);
    }

    @Override
    public synchronized void notifyWrittenByteCount(int count) {

        if (written_byte_count_listener != null) {
            written_byte_count_listener.notifyWrittenByteCount(count);
        }
    }

    public synchronized void setWrittenByteCountListener(WrittenByteCountListener written_byte_count_listener) {

        this.written_byte_count_listener = written_byte_count_listener;
    }

    @Override
    public String toString() {

        return "FutureResponse{" + "id=" + id + ", method=" + method + ", arguments=" + Arrays.toString(arguments) + '}';
    }
}
