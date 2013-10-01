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
package org.mashti.jetson.json;

import com.fasterxml.jackson.core.JsonFactory;
import org.mashti.jetson.ServerChannelInitializer;
import org.mashti.jetson.ServerFactory;
import org.mashti.jetson.util.ReflectionUtil;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class JsonServerFactory<Service> extends ServerFactory<Service> {

    public JsonServerFactory(final Class<Service> service_type, final JsonFactory json_factory) {

        super(new ServerChannelInitializer(new JsonRequestDecoder(json_factory, ReflectionUtil.mapNamesToMethods(service_type)), new JsonResponseEncoder(json_factory)));
    }
}
