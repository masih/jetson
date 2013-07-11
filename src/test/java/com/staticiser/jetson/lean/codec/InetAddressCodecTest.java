package com.staticiser.jetson.lean.codec;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class InetAddressCodecTest extends CodecTest {

    public InetAddressCodecTest() {

        super(new InetAddressCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(InetAddress.class));
        Assert.assertTrue(codec.isSupported(Inet4Address.class));
        Assert.assertTrue(codec.isSupported(Inet6Address.class));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final InetAddress[] addresses = {null, InetAddress.getLocalHost(), InetAddress.getByName("cs.st-andrews.ac.uk"), Inet6Address.getLocalHost(), Inet6Address.getByName("cs.st-andrews.ac.uk")};
        for (final InetAddress b : addresses) {
            encode(b, InetAddress.class);
            final InetAddress decode = decode(InetAddress.class);
            Assert.assertTrue(b == null ? b == decode : decode.equals(b));
        }
    }
}
