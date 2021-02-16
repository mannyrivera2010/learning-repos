package com.earasoft.rdf4j.sail.rocksDbStore.pipe;

import com.earasoft.rdf4j.sail.rocksDbStore.RocksDbSailStore;
import com.earasoft.rdf4j.sail.rocksDbStore.rockdb.RockDbHolding;
import com.earasoft.rdf4j.sail.rocksDbStore.rockdb.RockDbState;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.base.SailDataset;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author James Leigh
 */
public final class NativeSailDataset implements SailDataset {

    private final RocksDbSailStore nativeSailStore;
    private final boolean explicit;

    public NativeSailDataset(RocksDbSailStore nativeSailStore, boolean explicit) throws SailException {
        this.nativeSailStore = nativeSailStore;
        this.explicit = explicit;
    }

    @Override
    public void close() {
        // no-op
    }

    /**
     * Rocked
     * @param prefix
     * @return
     * @throws SailException
     */
    @Override
    public String getNamespace(String prefix) throws SailException {
        byte[] keyBytes = prefix.getBytes(StandardCharsets.UTF_8);
        byte[] value = RockDbState.getKey(
                nativeSailStore.rockDbHolding,
                RockDbHolding.NAMESPACES_CF,
                keyBytes
        );

        if(value == null)
            return null;

        return new String(value, StandardCharsets.UTF_8);
    }

    @Override
    public CloseableIteration<SimpleNamespace, SailException> getNamespaces() {
        return RockDbState.createRocksDbIteratorForCFColumn(
                nativeSailStore.rockDbHolding,
                RockDbHolding.NAMESPACES_CF,
                iteratorEntry -> {
                    String key = new String(iteratorEntry.keyBytes, StandardCharsets.UTF_8);
                    String value = new String(iteratorEntry.valueBytes, StandardCharsets.UTF_8);
                    SimpleNamespace ns = new SimpleNamespace(key, value);
                    return ns;
                }
        );
    }

    @Override
    public CloseableIteration<? extends Resource, SailException> getContextIDs() throws SailException {
        return new CloseableIteratorIteration<Resource, SailException>(nativeSailStore.contextStore.iterator());
    }

    @Override
    public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, IRI pred, Value obj,
                                                                                Resource... contexts) throws SailException {
        try {
            return nativeSailStore.createStatementIterator(subj, pred, obj, explicit, contexts);
        } catch (IOException e) {
            throw new SailException("Unable to get statements", e);
        }
    }
}