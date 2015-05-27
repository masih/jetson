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
package org.mashti.jetson.json;

import com.fasterxml.jackson.core.JsonFactory;
import org.mashti.jetson.ClientChannelInitializer;
import org.mashti.jetson.ClientFactory;
import org.mashti.jetson.util.ReflectionUtil;

/**
 * A factory for creating JSON RPC clients. The created clients are cached for future reuse. This class is thread-safe.
 *
 * @param <Service> the type of the remote service
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class JsonClientFactory<Service> extends ClientFactory<Service> {

    /**
     * Instantiates a new JSON RPC client factory. The {@link ClassLoader#getSystemClassLoader() system class loader} used for constructing new proxy instances.
     *
     * @param service_interface the interface presenting the remote service
     * @param json_factory the provider of JSON serializer and deserializer
     */
    public JsonClientFactory(final Class<Service> service_interface, final JsonFactory json_factory) {

        super(service_interface, new ClientChannelInitializer(new JsonRequestEncoder(json_factory, ReflectionUtil.mapMethodsToNames(service_interface)), new JsonResponseDecoder(json_factory)));
    }
}
