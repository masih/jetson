/*
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
package uk.ac.standrews.cs.jetson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

abstract class JsonRpcMessage {

    static final String ID_KEY = "id";
    static final String VERSION_KEY = "jsonrpc";
    static final String DEFAULT_VERSION = "2.0";
    private String version;
    private Long id;

    JsonRpcMessage() {

        setVersion(DEFAULT_VERSION);
    }

    @JsonProperty(ID_KEY)
    @JsonInclude(Include.ALWAYS)
    public Long getId() {

        return id;
    }

    void setId(final Long id) {

        this.id = id;
    }

    @JsonProperty(VERSION_KEY)
    @JsonInclude(Include.ALWAYS)
    public String getVersion() {

        return version;
    }

    void setVersion(final String version) {

        this.version = version;
    }
}
