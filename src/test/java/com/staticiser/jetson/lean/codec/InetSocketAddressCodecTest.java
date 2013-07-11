package com.staticiser.jetson.lean.codec;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class InetSocketAddressCodecTest extends CodecTest {

    public InetSocketAddressCodecTest() {

        super(new InetSocketAddressCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(InetSocketAddress.class));
        Assert.assertFalse(codec.isSupported(SocketAddress.class));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final InetSocketAddress[] addresses = {null, new InetSocketAddress("cs.st-andrews.ac.uk", 0), new InetSocketAddress("localhost", 65535), new InetSocketAddress("localhost", 45222)};
        for (final InetSocketAddress b : addresses) {
            encode(b, InetSocketAddress.class);
            final InetSocketAddress decode = decode(InetSocketAddress.class);
            Assert.assertTrue(b == null ? b == decode : decode.equals(b));
        }
    }
}
