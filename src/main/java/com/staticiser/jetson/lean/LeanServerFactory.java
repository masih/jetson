package com.staticiser.jetson.lean;

import com.staticiser.jetson.ServerChannelInitializer;
import com.staticiser.jetson.ServerFactory;
import com.staticiser.jetson.lean.codec.Codecs;
import com.staticiser.jetson.util.ReflectionUtil;
import java.lang.reflect.Method;
import java.util.ArrayList;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanServerFactory<Service> extends ServerFactory<Service> {

    public LeanServerFactory(final Class<Service> service_type, final Codecs marshallers) {

        super(service_type, new ServerChannelInitializer(new LeanRequestDecoder(new ArrayList<Method>(ReflectionUtil.mapMethodsToNames(service_type).keySet()), marshallers), new LeanResponseEncoder(marshallers)));
    }
}
