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
package org.mashti.jetson.lean.codec;

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
