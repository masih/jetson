/*
 * Copyright 2013 Masih Hajiarabderkani
 *
 * This file is part of Jetson.
 *
 * Jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Jetson.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.staticiser.jetson.util;

import java.io.Closeable;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for closing {@code Closeable} resources.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class CloseableUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseableUtil.class);

    private CloseableUtil() {

    }

    /**
     * Closes the given {@code closeables} without throwing exception in case of failure.
     * If a given closeable is {@code null} it is skipped. Consumes any failures during the closure of {@code closeables}.
     *
     * @param closeables the resources to close
     */
    public static void closeQuietly(final Closeable... closeables) {

        for (final Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                }
                catch (final IOException e) {
                    LOGGER.debug("IO error occured while closing" + closeable, e);
                }
            }
        }
    }
}
