/**
 * This file is part of jetson.
 *
 * jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jetson.  If not, see <http://www.gnu.org/licenses/>.
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
