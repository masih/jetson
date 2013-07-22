package org.mashti.jetson.lean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import org.mashti.jetson.ServerChannelInitializer;
import org.mashti.jetson.ServerFactory;
import org.mashti.jetson.lean.codec.Codecs;
import org.mashti.jetson.util.ReflectionUtil;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanServerFactory<Service> extends ServerFactory<Service> {

    public LeanServerFactory(final Class<Service> service_type) {

        this(service_type, LeanClientFactory.DEFAULT_CODECS);
    }

    public LeanServerFactory(final Class<Service> service_type, final Codecs codecs) {

        super(new ServerChannelInitializer(new LeanRequestDecoder(new ArrayList<Method>(ReflectionUtil.mapMethodsToNames(service_type).keySet()), codecs), new LeanResponseEncoder(codecs)));
    }
}
