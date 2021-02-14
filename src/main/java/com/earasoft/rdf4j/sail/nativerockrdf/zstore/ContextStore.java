package com.earasoft.rdf4j.sail.nativerockrdf.zstore;

import com.earasoft.rdf4j.sail.nativerockrdf.NativeSailStore;
import com.earasoft.rdf4j.sail.nativerockrdf.rockdb.RockDbHolding;
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

    public static class ContextCfSerializer{

        public static Resource fromBytesKey(ValueFactory valueFactory, byte[] keyBytes) {
            ByteArrayDataInput keyBuffer = ByteStreams.newDataInput(keyBytes);
            String contextId = keyBuffer.readUTF();
            boolean isIRI = keyBuffer.readBoolean();

            if (isIRI) {
                return valueFactory.createIRI(contextId);
            } else
                return valueFactory.createBNode(contextId); // why would context be a blank node
        }

        public static byte[] toBytesKey(Resource context) {
            ByteArrayDataOutput keyBuffer = ByteStreams.newDataOutput();
            keyBuffer.writeUTF(context.stringValue());
            keyBuffer.writeBoolean(context instanceof IRI);
            byte[] keyBufferBytes =  keyBuffer.toByteArray();
            return keyBufferBytes;
        }
    }

    public ContextStore(NativeSailStore store) throws IOException {
        Objects.requireNonNull(store);
        this.valueFactory = store.getValueFactory();
        this.store = store;
    }

    /**
     * Increase the size of the context. If the context was not yet known, it is created with a size of 1.
     *
     * @param context the context identifier.
     */
    public void increment(Resource context) {
        // contextInfoMap.merge(context, 1L, (size, one) -> size + one);
        incrementRockerDb(context);
        contentsChanged = true;
    }

    /**
     * implemented as get -> modify -> set
     * should be sync?
     * @param context
     */
    public void incrementRockerDb(Resource context) {
        ColumnFamilyHandle cfHandle = store.rockDbHolding.cfHandlesMap.get(RockDbHolding.CONTEXTS_CF);

        byte[] keyBytes = ContextCfSerializer.toBytesKey(context);
        try {
            Long counterValue = 0L;

//            // GET
            //                counterValue = byteAToLong(value);
            byte[] value = store.rockDbHolding.rocksDB.get(cfHandle, keyBytes);
            if(value != null){
                counterValue = Longs.fromByteArray(value);
            }
//           // modify
            counterValue = counterValue + 1;
//            // set
            store.rockDbHolding.rocksDB.put(cfHandle, keyBytes, Longs.toByteArray(counterValue));

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
        ColumnFamilyHandle cfHandle = store.rockDbHolding.cfHandlesMap.get(RockDbHolding.CONTEXTS_CF);

        byte[] keyBufferBytes = ContextCfSerializer.toBytesKey(context);
        try {
            // GET
            byte[] value = store.rockDbHolding.rocksDB.get(cfHandle, keyBufferBytes);
            Long counterValue = 1L;
            if(value != null){
                counterValue = Longs.fromByteArray(value);
            }
            // modify
            counterValue = counterValue - amount;
            // set
            store.rockDbHolding.rocksDB.put(cfHandle, keyBufferBytes, Longs.toByteArray(counterValue));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Iterator<Resource> iterator() {
        return RockDbUtils.createRocksRegIteratorForCFColumn(
                this.store.rockDbHolding,
                RockDbHolding.CONTEXTS_CF,
                iteratorEntry -> {
                    byte[] keyBytes = iteratorEntry.keyBytes;
                    Resource context = ContextCfSerializer.fromBytesKey(valueFactory, keyBytes);
                    return context;
                }
        );
    }

    public void clear() {
        RockDbUtils.recreateCfRockDb(
                this.store.rockDbHolding,
                RockDbHolding.CONTEXTS_CF
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

    public static void main(String[] args) {

    }

}
