package com.earasoft.rdf4j.sail.rocksDbStore.pipe;

import com.earasoft.rdf4j.sail.rocksDbStore.RocksDbSailStore;
import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.base.BackingSailSource;
import org.eclipse.rdf4j.sail.base.SailSink;
import org.eclipse.rdf4j.sail.base.SailSource;

public final class NativeSailSource extends BackingSailSource {

    private final RocksDbSailStore nativeSailStore;
    private final boolean explicit;

    public NativeSailSource(RocksDbSailStore nativeSailStore, boolean explicit) {
        this.nativeSailStore = nativeSailStore;
        this.explicit = explicit;
    }

    @Override
    public SailSource fork() {
        throw new UnsupportedOperationException("This store does not support multiple datasets");
    }

    @Override
    public SailSink sink(IsolationLevel level) throws SailException {
        return new NativeSailSink(nativeSailStore, explicit);
    }

    @Override
    public NativeSailDataset dataset(IsolationLevel level) throws SailException {
        return new NativeSailDataset(nativeSailStore, explicit);
    }

}
