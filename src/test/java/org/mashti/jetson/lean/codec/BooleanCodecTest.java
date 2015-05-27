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
package org.mashti.jetson.lean.codec;

import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class BooleanCodecTest extends CodecTest {

    public BooleanCodecTest() {

        super(new BooleanCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Boolean.class));
        Assert.assertTrue(codec.isSupported(Boolean.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodecPrimitive() throws Exception {

        final boolean[] flags = {true, false, false, true};
        for (final boolean b : flags) {
            encode(b, Boolean.TYPE);
        }
        Assert.assertEquals(flags.length, buffer.readableBytes());
        for (final boolean b : flags) {
            Assert.assertTrue(decode(Boolean.TYPE) == b);
        }
    }

    @Test
    public void testCodec() throws Exception {

        final Boolean[] flags = {true, false, null, true};
        for (final Boolean b : flags) {
            encode(b, Boolean.class);
        }
        // The number of written bytes is 2 for each non-null boolean and 1 for each null boolean
        Assert.assertEquals(flags.length * 2 - 1, buffer.readableBytes());
        for (final Boolean b : flags) {
            Assert.assertEquals(b, decode(Boolean.class));
        }
    }
}
