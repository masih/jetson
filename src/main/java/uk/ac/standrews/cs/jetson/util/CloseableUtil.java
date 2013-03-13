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
