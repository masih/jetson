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

import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class UUIDCodecTest extends CodecTest {

    public UUIDCodecTest() {

        super(new UUIDCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(UUID.class));
        Assert.assertFalse(codec.isSupported(Integer.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final UUID[] uuids = {UUID.randomUUID(), new UUID(0, 0), new UUID(65465465, 44), null};
        for (final UUID b : uuids) {
            encode(b, UUID.class);
            if (b == null) {
                Assert.assertEquals(1, buffer.readableBytes());
                Assert.assertSame(b, decode(UUID.class));
            }
            else {
                Assert.assertEquals(Long.SIZE / 8 * 2 + 1, buffer.readableBytes());
                Assert.assertEquals(decode(UUID.class), b);
            }
        }
    }
}
