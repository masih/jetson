package org.mashti.jetson.lean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import org.mashti.jetson.ClientChannelInitializer;
import org.mashti.jetson.lean.codec.Codecs;
import org.mashti.jetson.util.ReflectionUtil;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanClientChannelInitializer extends ClientChannelInitializer {

    public LeanClientChannelInitializer(Class<?> service_interface, Codecs codecs) {
        super(new LeanRequestEncoder(new ArrayList<Method>(ReflectionUtil.mapMethodsToNames(service_interface).keySet()), codecs), new LeanResponseDecoder(codecs));
    }
}
