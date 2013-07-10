package com.staticiser.jetson.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.staticiser.jetson.ServerChannelInitializer;
import com.staticiser.jetson.ServerFactory;
import com.staticiser.jetson.util.ReflectionUtil;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class JsonServerFactory<Service> extends ServerFactory<Service> {

    public JsonServerFactory(final Class<Service> service_type, final JsonFactory json_factory) {

        super(new ServerChannelInitializer(new JsonRequestDecoder(json_factory, ReflectionUtil.mapNamesToMethods(service_type)), new JsonResponseEncoder(json_factory)));
    }
}
