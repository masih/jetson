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
package uk.ac.standrews.cs.jetson;

/**
 * The base class for any exchanged JSON RPC message.
 * 
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public abstract class Message {

    protected static final String ID_KEY = "id";
    protected static final String VERSION_KEY = "jsonrpc";
    protected static final String DEFAULT_VERSION = "2.0";

    private String version;
    private Long id;

    Message() {

        resetVersion();
    }

    Long getId() {

        return id;
    }

    void setId(final Long id) {

        this.id = id;
    }

    String getVersion() {

        return version;
    }

    void setVersion(final String version) {

        this.version = version;
    }

    protected void reset() {

        resetVersion();
        setId(null);
    }

    private void resetVersion() {

        setVersion(DEFAULT_VERSION);
    }

}
