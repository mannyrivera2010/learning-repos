package com.earasoft.rdf4j.sail.nativerockrdf.zstore;

import com.earasoft.rdf4j.sail.nativerockrdf.NativeSailStore;
import com.earasoft.rdf4j.sail.nativerockrdf.rockdb.RockDbUtils;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Longs;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * An rockdb column family for context information that uses a file for persistence.
 *
 * @author Jeen Broekstra
 *
 */
public class ContextStore implements Iterable<Resource> {

    static final Logger logger = LoggerFactory.getLogger(ContextStore.class);

    /**
     * Flag indicating whether the contents of this {@link ContextStore} are different from what is stored on disk.
     */
    private volatile boolean contentsChanged;

    private final ValueFactory valueFactory;

    private final NativeSailStore store;

    public ContextStore(NativeSailStore store) throws IOException {
        Objects.requireNonNull(store);

//        this.file = new File(dataDir, FILE_NAME);
        this.valueFactory = store.getValueFactory();
        this.store = store;

//        contextInfoMap = new HashMap<>(16);

//        try {
//            readContextsFromFile();
//        } catch (IOException e) {
//            logger.info("could not read context index: " + e.getMessage(), e);
//            logger.debug("attempting reconstruction from store (this may take a while)");
//            initializeContextCache();
//            writeContextsToFile();
//            logger.info("context index reconstruction complete");
//        }
    }

    /**
     * Increase the size of the context. If the context was not yet known, it is created with a size of 1.
     *
     * @param context the context identifier.
     */
    void increment(Resource context) {
//        contextInfoMap.merge(context, 1L, (size, one) -> size + one);
        incrementRockerDb(context);
        contentsChanged = true;
    }

    /**
     * implemented as get -> modify -> set
     *
     * @param context
     */
    public void incrementRockerDb(Resource context) {
        ColumnFamilyHandle cfHandle = store.cfHandlesMap.get(store.CONTEXTS_CF);

        ByteArrayDataOutput keyBuffer = ByteStreams.newDataOutput();
        keyBuffer.writeUTF(context.stringValue());
        keyBuffer.writeBoolean(context instanceof IRI);

        try {
            Long counterValue = 0L;

//            // GET
            //                counterValue = byteAToLong(value);
            byte[] value = store.rocksDB.get(cfHandle, keyBuffer.toByteArray());
            if(value != null){
                counterValue = Longs.fromByteArray(value);
            }
//           // modify
            counterValue = counterValue + 1;
//            // set
            store.rocksDB.put(cfHandle, keyBuffer.toByteArray(), Longs.toByteArray(counterValue));

//            store.rocksDB.merge(cfHandle, keyBuffer.toByteArray(), Longs.toByteArray(1));

//            System.out.println(Longs.fromByteArray(store.rocksDB.get(cfHandle, keyBuffer.toByteArray())));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }




    /**
     * Decrease the size of the context by the given amount. If the size reaches zero, the context is removed.
     *
     * @param context the context identifier.
     * @param amount  the number by which to decrease the size
     */
    public void decrementBy(Resource context, long amount) {
//        contextInfoMap.computeIfPresent(context, (c, size) -> size <= amount ? null : size - amount);
        decrementBytRockerDb(context, amount);
        contentsChanged = true;
    }

    public void decrementBytRockerDb(Resource context, long amount){
        ColumnFamilyHandle cfHandle = store.cfHandlesMap.get(store.CONTEXTS_CF);

        ByteArrayDataOutput keyBuffer = ByteStreams.newDataOutput();
        keyBuffer.writeUTF(context.stringValue());
        keyBuffer.writeBoolean(context instanceof IRI);

        try {
            // GET
            byte[] value = store.rocksDB.get(cfHandle, keyBuffer.toByteArray());
            Long counterValue = 1L;
            if(value != null){
                counterValue = Longs.fromByteArray(value);
            }
            // modify
            counterValue = counterValue - amount;
            // set
            store.rocksDB.put(cfHandle, keyBuffer.toByteArray(), Longs.toByteArray(counterValue));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Iterator<Resource> iterator() {
        return RockDbUtils.createRocksRegIteratorForCFColumn(
                store.cfHandlesMap,
                store.rocksDB,
                store.CONTEXTS_CF,
                iteratorEntry -> {
                    ByteArrayDataInput keyBuffer = ByteStreams.newDataInput(iteratorEntry.keyBytes);

                    String contextId = keyBuffer.readUTF();
                    boolean isIRI = keyBuffer.readBoolean();

//                    System.out.println(byteAToLong(iteratorEntry.valueBytes));
                    Resource context = isIRI ? valueFactory.createIRI(contextId)
                            : valueFactory.createBNode(contextId);
                    return context;
                }
        );
    }

    public void clear() {
        RockDbUtils.recreateCfRockDb(
                this.store.cfHandlesMap,
                this.store.cfOpts,
                this.store.rocksDB,
                NativeSailStore.CONTEXTS_CF
        );
        contentsChanged = true;
    }

   public void close() {
    }

    public void sync() throws IOException {
        if (contentsChanged) {
            // Should rockDb do something
            contentsChanged = false;
        }
    }

}
