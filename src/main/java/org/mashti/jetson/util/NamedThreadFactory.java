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
package org.mashti.jetson.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around {@link Executors#defaultThreadFactory()} that names the threads using a given prefix concatenated with an atomically increasing integer starting from <code>0</code>.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class NamedThreadFactory implements ThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NamedThreadFactory.class);
    private static final UncaughtExceptionHandler PRINT_UNCAUGHT_EXCEPTIONS = new UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(final Thread t, final Throwable e) {

            LOGGER.error("", e);
        }
    };
    private final AtomicLong sequence_number;
    private final String naming_prefix;
    private final boolean debug;
    private boolean daemon;

    /**
     * Instantiates a new naming thread factory.
     *
     * @param naming_prefix the naming prefix to be given to generated threads
     */
    public NamedThreadFactory(final String naming_prefix) {

        this(naming_prefix, false);
    }

    /**
     * Instantiates a new naming thread factory.
     *
     * @param naming_prefix the naming prefix to be given to generated threads
     * @param debug whether to print out the stack trace of uncaught exceptions within a created thread
     */
    public NamedThreadFactory(final String naming_prefix, final boolean debug) {

        this.naming_prefix = naming_prefix;
        this.debug = debug;
        sequence_number = new AtomicLong(0);
    }

    @Override
    public Thread newThread(final Runnable task) {

        final Thread new_thread = Executors.defaultThreadFactory().newThread(task);
        final String name = generateName();

        if (debug) {
            new_thread.setUncaughtExceptionHandler(PRINT_UNCAUGHT_EXCEPTIONS);
        }

        new_thread.setName(name);
        new_thread.setDaemon(daemon);
        return new_thread;
    }

    public boolean isDaemon() {

        return daemon;
    }

    public void setDaemon(final boolean daemon) {

        this.daemon = daemon;
    }

    private String generateName() {

        final StringBuilder builder = new StringBuilder();
        builder.append(naming_prefix);
        builder.append(sequence_number.getAndIncrement());
        return builder.toString();
    }
}
