package com.earasoft.rdf4j.sail.nativerockrdf.pip;

import com.earasoft.rdf4j.sail.nativerockrdf.NativeSailStore;
import com.earasoft.rdf4j.sail.nativerockrdf.rockdb.RockDbUtils;
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

/**
 * @author James Leigh
 */
public final class NativeSailDataset implements SailDataset {

    private final NativeSailStore nativeSailStore;
    private final boolean explicit;

    public NativeSailDataset(NativeSailStore nativeSailStore, boolean explicit) throws SailException {
        this.nativeSailStore = nativeSailStore;
        this.explicit = explicit;
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getNamespace(String prefix) throws SailException {
        return RockDbUtils.getKeyRockDb(nativeSailStore.cfHandlesMap, nativeSailStore.rocksDB, prefix);
    }

    @Override
    public CloseableIteration<SimpleNamespace, SailException> getNamespaces() {
        return RockDbUtils.createRocksDbIteratorForCFColumn(
                nativeSailStore.cfHandlesMap,
                nativeSailStore.rocksDB,
                NativeSailStore.NAMESPACES_CF,
                iteratorEntry -> {
                    String key = new String(iteratorEntry.keyBytes);
                    String value = new String(iteratorEntry.valueBytes);
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
} // end
