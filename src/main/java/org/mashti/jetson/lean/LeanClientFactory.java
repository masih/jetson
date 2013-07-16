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
package org.mashti.jetson.lean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import org.mashti.jetson.ClientChannelInitializer;
import org.mashti.jetson.ClientFactory;
import org.mashti.jetson.lean.codec.Codecs;
import org.mashti.jetson.util.ReflectionUtil;

/**
 * A factory for creating JSON RPC clients. The created clients are cached for future reuse. This class is thread-safe.
 *
 * @param <Service> the type of the remote service
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LeanClientFactory<Service> extends ClientFactory<Service> {

    /**
     * Instantiates a new JSON RPC client factory. The {@link ClassLoader#getSystemClassLoader() system class loader} used for constructing new proxy instances.
     *
     * @param service_interface the interface presenting the remote service
     * @param codecs the codecs
     */
    public LeanClientFactory(final Class<Service> service_interface, final Codecs codecs) {

        super(service_interface, new ClientChannelInitializer(new LeanRequestEncoder(new ArrayList<Method>(ReflectionUtil.mapMethodsToNames(service_interface).keySet()), codecs), new LeanResponseDecoder(codecs)));
    }
}
