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
package uk.ac.standrews.cs.jetson.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CloseableUtil {

    private static final Logger LOGGER = Logger.getLogger(CloseableUtil.class.getName());

    private CloseableUtil() {

    }

    public static Closeable toCloseable(final Socket socket) {

        //Make a closeable wrapper around socket if the socket is not null.
        //In JDK 7 Socket implements Closeable as it should.
        return socket != null ? new Closeable() {

            @Override
            public void close() throws IOException {
                socket.close();

            }
        } : null;
    }

    public static Closeable toCloseable(final ServerSocket server_socket) {

        //Make a closeable wrapper around socket if the socket is not null.
        //In JDK 7 Socket implements Closeable as it should.
        return server_socket != null ? new Closeable() {

            @Override
            public void close() throws IOException {

                server_socket.close();
            }
        } : null;
    }

    public static void closeQuietly(final Closeable... closeables) {

        for (final Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                }
                catch (final IOException e) {
                    LOGGER.log(Level.FINE, "IO error occured while closing" + closeable, e);
                };
            }
        }
    }
}
