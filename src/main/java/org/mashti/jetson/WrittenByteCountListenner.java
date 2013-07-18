package org.mashti.jetson;

import java.util.EventListener;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface WrittenByteCountListenner extends EventListener {

    void notifyWrittenByteCount(int byte_count);
}
