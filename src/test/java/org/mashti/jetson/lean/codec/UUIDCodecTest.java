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
