package uk.ac.standrews.cs.jetson.pool;

import java.net.Socket;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

public class ScoketPool extends GenericObjectPool<Socket> {

    public ScoketPool() {

        super(new PoolableObjectFactory<Socket>() {

            @Override
            public Socket makeObject() throws Exception {

                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void destroyObject(final Socket obj) throws Exception {

                // TODO Auto-generated method stub

            }

            @Override
            public boolean validateObject(final Socket obj) {

                return !obj.isClosed() && obj.isConnected();
            }

            @Override
            public void activateObject(final Socket obj) throws Exception {

            }

            @Override
            public void passivateObject(final Socket obj) throws Exception {

            }
        });
    }
}
