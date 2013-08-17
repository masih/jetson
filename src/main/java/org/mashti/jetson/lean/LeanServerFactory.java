package org.mashti.jetson.lean;

import org.mashti.jetson.ServerFactory;
import org.mashti.jetson.lean.codec.Codecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanServerFactory<Service> extends ServerFactory<Service> {

    public LeanServerFactory(final Class<Service> service_type) {

        this(service_type, LeanClientFactory.DEFAULT_CODECS);
    }

    public LeanServerFactory(final Class<Service> service_type, final Codecs codecs) {

        super(new LeanServerChannelInitializer<Service>(service_type, codecs));
    }
}
