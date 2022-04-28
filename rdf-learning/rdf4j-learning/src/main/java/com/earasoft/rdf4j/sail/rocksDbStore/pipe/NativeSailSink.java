package com.earasoft.rdf4j.sail.rocksDbStore.pipe;

import com.earasoft.rdf4j.sail.rocksDbStore.RocksDbSailStore;
import com.earasoft.rdf4j.sail.rocksDbStore.rockdb.RockByteUtils;
import com.earasoft.rdf4j.sail.rocksDbStore.rockdb.RockDbHolding;
import com.earasoft.rdf4j.sail.rocksDbStore.rockdb.RockDbState;
import com.earasoft.rdf4j.sail.rocksDbStore.model.NativeValue;
import com.earasoft.rdf4j.utils.HUtils;
import org.eclipse.rdf4j.OpenRDFUtil;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.base.SailSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public final class NativeSailSink implements SailSink {
    public final Logger logger = LoggerFactory.getLogger(NativeSailSink.class);

    private final RocksDbSailStore nativeSailStore;
    // https://graphdb.ontotext.com/documentation/enterprise/reasoning.html
    private final boolean explicit;

    public NativeSailSink(RocksDbSailStore nativeSailStore, boolean explicit) throws SailException {
        this.nativeSailStore = nativeSailStore;
        this.explicit = explicit;
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public void prepare() throws SailException {
        // serializable is not supported at this level
    }

    @Override
    public synchronized void flush() throws SailException {
        nativeSailStore.sinkStoreAccessLock.lock();
        try {
            try {
                nativeSailStore.valueStore.sync();
            } finally {
                try {
//						namespaceStore.sync();
                } finally {
                    try {
                        nativeSailStore.contextStore.sync();
                    } finally {
                        if (nativeSailStore.storeTxnStarted.get()) {
                            nativeSailStore.tripleStore.commit();
                            // do not set flag to false until _after_ commit is succesfully completed.
                            nativeSailStore.storeTxnStarted.set(false);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Encountered an unexpected problem while trying to commit", e);
            throw new SailException(e);
        } catch (RuntimeException e) {
            logger.error("Encountered an unexpected problem while trying to commit", e);
            throw e;
        } finally {
            nativeSailStore.sinkStoreAccessLock.unlock();
        }
    }

    @Override
    public void setNamespace(String prefix, String name) throws SailException {
        nativeSailStore.sinkStoreAccessLock.lock();
        try {
            startTriplestoreTransaction();
            RockDbState.setKeyRockDb(
                    nativeSailStore.rockDbHolding,
                    prefix,
                    name,
                    RockDbHolding.NAMESPACES_CF);
        } finally {
            nativeSailStore.sinkStoreAccessLock.unlock();
        }
    }

    @Override
    public void removeNamespace(String prefix) throws SailException {
        nativeSailStore.sinkStoreAccessLock.lock();
        try {
            startTriplestoreTransaction();
            RockDbState.deleteKeyRockDb(
                    nativeSailStore.rockDbHolding,
                    prefix,
                    RockDbHolding.NAMESPACES_CF);
        } finally {
            nativeSailStore.sinkStoreAccessLock.unlock();
        }
    }

    @Override
    public void clearNamespaces() throws SailException {
        nativeSailStore.sinkStoreAccessLock.lock();
        try {
            startTriplestoreTransaction();
            RockDbState.recreateColumnFamily(
                    nativeSailStore.rockDbHolding,
                    RockDbHolding.NAMESPACES_CF
            );
        } finally {
            nativeSailStore.sinkStoreAccessLock.unlock();
        }
    }

    @Override
    public void observe(Resource subj, IRI pred, Value obj, Resource... contexts) throws SailException {
        // serializable is not supported at this level
    }

    @Override
    public void clear(Resource... contexts) throws SailException {
        removeStatements(null, null, null, explicit, contexts);
    }

    @Override
    public void approve(Resource subj, IRI pred, Value obj, Resource ctx) throws SailException {
        addStatement(subj, pred, obj, explicit, ctx);
    }

    @Override
    public void deprecate(Resource subj, IRI pred, Value obj, Resource ctx) throws SailException {
        removeStatements(subj, pred, obj, explicit, ctx);
    }

    /**
     * Starts a transaction on the triplestore, if necessary.
     *
     * @throws SailException if a transaction could not be started.
     */
    private synchronized void startTriplestoreTransaction() throws SailException {

        if (nativeSailStore.storeTxnStarted.compareAndSet(false, true)) {
            try {
                nativeSailStore.tripleStore.startTransaction();
            } catch (IOException e) {
                nativeSailStore.storeTxnStarted.set(false);
                throw new SailException(e);
            }
        }
    }


    private boolean addStatement(Resource subj, IRI pred, Value obj, boolean explicit, Resource... contexts)
            throws SailException {
        OpenRDFUtil.verifyContextNotNull(contexts);
        boolean result = false;
        nativeSailStore.sinkStoreAccessLock.lock();
        try {
            startTriplestoreTransaction();

            if (contexts.length == 0) {
                contexts = new Resource[]{null};
            }

            // rockdb
            for (Resource context : contexts) {
                byte[] value = HUtils.toKeyValues(subj, pred, obj, explicit, context);
                byte[] key = RockByteUtils.getIdAsByteUUID(value);

                // TODO how to deal with explicit flag

                byte[] dbValue = RockDbState.getKey(
                        nativeSailStore.rockDbHolding,
                        "default",
                        key
                );

                if(dbValue == null){ // if value already exist then don't insert again
                    RockDbState.setKeyRockDbBytes(
                            nativeSailStore.rockDbHolding,
                            key, value,
                            "default"
                    );

                    if(context != null){
                        nativeSailStore.contextStore.increment(context); // needed?
                    }
                    result = true;
                }
            }

            // the id start at 0 increasing in value by 1 based on the time of insersion
            int subjID = nativeSailStore.valueStore.storeValue(subj);
            int predID = nativeSailStore.valueStore.storeValue(pred);
            int objID = nativeSailStore.valueStore.storeValue(obj);

            // native store
            for (Resource context : contexts) {
                int contextID = 0;
                if (context != null) {
                    contextID = nativeSailStore.valueStore.storeValue(context);
                }

                boolean wasNew = nativeSailStore.tripleStore.storeTriple(subjID, predID, objID, contextID, explicit);
                if (wasNew && context != null) {
//                    nativeSailStore.contextStore.increment(context); // needed?
                }
                result |= wasNew;
            }
        } catch (IOException e) {
            throw new SailException(e);
        } catch (RuntimeException e) {
            logger.error("Encountered an unexpected problem while trying to add a statement", e);
            throw e;
        } finally {
            nativeSailStore.sinkStoreAccessLock.unlock();
        }

        return result;
    }

    private long removeStatements(Resource subj, IRI pred, Value obj, boolean explicit, Resource... contexts)
            throws SailException {
        OpenRDFUtil.verifyContextNotNull(contexts);

        nativeSailStore.sinkStoreAccessLock.lock();
        try {
            startTriplestoreTransaction();
            int subjID = NativeValue.UNKNOWN_ID;
            if (subj != null) {
                subjID = nativeSailStore.valueStore.getID(subj);
                if (subjID == NativeValue.UNKNOWN_ID) {
                    return 0;
                }
            }
            int predID = NativeValue.UNKNOWN_ID;
            if (pred != null) {
                predID = nativeSailStore.valueStore.getID(pred);
                if (predID == NativeValue.UNKNOWN_ID) {
                    return 0;
                }
            }
            int objID = NativeValue.UNKNOWN_ID;
            if (obj != null) {
                objID = nativeSailStore.valueStore.getID(obj);
                if (objID == NativeValue.UNKNOWN_ID) {
                    return 0;
                }
            }

            final int[] contextIds = new int[contexts.length == 0 ? 1 : contexts.length];
            if (contexts.length == 0) { // remove from all contexts
                contextIds[0] = NativeValue.UNKNOWN_ID;
            } else {
                for (int i = 0; i < contexts.length; i++) {
                    Resource context = contexts[i];
                    if (context == null) {
                        contextIds[i] = 0;
                    } else {
                        int id = nativeSailStore.valueStore.getID(context);
                        // unknown_id cannot be used (would result in removal from all contexts)
                        contextIds[i] = (id != NativeValue.UNKNOWN_ID) ? id : Integer.MIN_VALUE;
                    }
                }
            }

            long removeCount = 0;
            for (int contextId : contextIds) {
                Map<Integer, Long> result = nativeSailStore.tripleStore.removeTriplesByContext(subjID, predID, objID, contextId,
                        explicit);

                for (Map.Entry<Integer, Long> entry : result.entrySet()) {
                    Integer entryContextId = entry.getKey();
                    if (entryContextId > 0) {
                        Resource modifiedContext = (Resource) nativeSailStore.valueStore.getValue(entryContextId);
                        nativeSailStore.contextStore.decrementBy(modifiedContext, entry.getValue());
                    }
                    removeCount += entry.getValue();
                }
            }
            return removeCount;
        } catch (IOException e) {
            throw new SailException(e);
        } catch (RuntimeException e) {
            logger.error("Encountered an unexpected problem while trying to remove statements", e);
            throw e;
        } finally {
            nativeSailStore.sinkStoreAccessLock.unlock();
        }
    }
}
