/***************************************************************************
 *                                                                         *
 * nds Library                                                             *
 * Copyright (C) 2005-2010 Distributed Systems Architecture Research Group *
 * University of St Andrews, Scotland                                      *
 * http://www-systems.cs.st-andrews.ac.uk/                                 *
 *                                                                         *
 * This file is part of nds, a package of utility classes.                 *
 *                                                                         *
 * nds is free software: you can redistribute it and/or modify             *
 * it under the terms of the GNU General Public License as published by    *
 * the Free Software Foundation, either version 3 of the License, or       *
 * (at your option) any later version.                                     *
 *                                                                         *
 * nds is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of          *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
 * GNU General Public License for more details.                            *
 *                                                                         *
 * You should have received a copy of the GNU General Public License       *
 * along with nds.  If not, see <http://www.gnu.org/licenses/>.            *
 *                                                                         *
 ***************************************************************************/
package com.staticiser.jetson.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A wrapper around {@link Executors#defaultThreadFactory()} that names the threads using a given prefix concatenated with an atomically increasing integer starting from <code>0</code>.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class NamingThreadFactory implements ThreadFactory {

    private static final UncaughtExceptionHandler PRINT_UNCAUGHT_EXCEPTIONS = new UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(final Thread t, final Throwable e) {

            e.printStackTrace();
        }
    };
    private final AtomicLong sequence_number;
    private final String naming_prefix;
    private final boolean debug;

    /**
     * Instantiates a new naming thread factory.
     *
     * @param naming_prefix the naming prefix to be given to generated threads
     */
    public NamingThreadFactory(final String naming_prefix) {

        this(naming_prefix, false);
    }

    /**
     * Instantiates a new naming thread factory.
     *
     * @param naming_prefix the naming prefix to be given to generated threads
     * @param debug whether to print out the stack trace of uncaught exceptions within a created thread
     */
    public NamingThreadFactory(final String naming_prefix, final boolean debug) {

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
        return new_thread;
    }

    private String generateName() {

        final StringBuilder builder = new StringBuilder();
        builder.append(naming_prefix);
        builder.append(sequence_number.getAndIncrement());
        return builder.toString();
    }
}
