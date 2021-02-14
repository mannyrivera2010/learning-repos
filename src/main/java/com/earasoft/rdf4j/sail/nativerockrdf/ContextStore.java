package com.earasoft.rdf4j.sail.nativerockrdf;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Longs;
import org.eclipse.rdf4j.common.io.IOUtil;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.sail.SailException;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * An in-memory index for context information that uses a file for persistence.
 * <p>
 * The context index file has an 8-byte header consisting of:
 *
 * <pre>
 * 	byte 1-3         : the magic number marker
 *  byte 4           : the file format version
 *  byte 5-8         : the number of mapped contexts contained in the file, as an int.
 * </pre>
 *
 * Each context is encoded in the file as a record, as follows:
 *
 * <pre>
 *   byte 1 - 8      : the number of statements in the content, as a long.
 *   byte 9          : boolean flag indicating the type of context identifier (1 = IRI, 0 = blank node)
 *   byte 10 - 11    : the length of the encoded context identifier
 *   byte 12 - A     : the UTF-8 encoded the encoded context identifer
 * </pre>
 *
 * @author Jeen Broekstra
 *
 */
class ContextStore implements Iterable<Resource> {

    static final Logger logger = LoggerFactory.getLogger(ContextStore.class);

    private static final String FILE_NAME = "contexts.dat";

    /**
     * Magic number "Native Context File" to detect whether the file is actually a context file. The first three bytes
     * of the file should be equal to this magic number.
     */
    private static final byte[] MAGIC_NUMBER = new byte[] { 'n', 'c', 'f' };

    /**
     * File format version, stored as the fourth byte in context files.
     */
    private static final byte FILE_FORMAT_VERSION = 1;

    /**
     * The data file for this {@link ContextStore}.
     */
    private final File file;

    private final Map<Resource, Long> contextInfoMap;

    /**
     * Flag indicating whether the contents of this {@link ContextStore} are different from what is stored on disk.
     */
    private volatile boolean contentsChanged;

    private final ValueFactory valueFactory;

    private final NativeSailStore store;

    ContextStore(NativeSailStore store, File dataDir) throws IOException {
        Objects.requireNonNull(store);
        Objects.requireNonNull(dataDir);

        this.file = new File(dataDir, FILE_NAME);
        this.valueFactory = store.getValueFactory();
        this.store = store;

        contextInfoMap = new HashMap<>(16);

        try {
            readContextsFromFile();
        } catch (IOException e) {
            logger.info("could not read context index: " + e.getMessage(), e);
            logger.debug("attempting reconstruction from store (this may take a while)");
            initializeContextCache();
            writeContextsToFile();
            logger.info("context index reconstruction complete");
        }
    }

    /**
     * Increase the size of the context. If the context was not yet known, it is created with a size of 1.
     *
     * @param context the context identifier.
     */
    void increment(Resource context) {
        contextInfoMap.merge(context, 1L, (size, one) -> size + one);
        contentsChanged = true;

        incrementRockerDb(context);
    }

    /**
     * implemented as get -> modify -> set
     * @param context
     */
    public void incrementRockerDb(Resource context){
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
            counterValue = counterValue + 1;
            // set
            store.rocksDB.put(cfHandle, keyBuffer.toByteArray(), Longs.toByteArray(counterValue));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
//	// implemented as get -> modify -> set
//	virtual void Add(const string& key, uint64_t value) {
//		uint64_t base;
//		if (!Get(key, &base)) {
//			base = kDefaultValue;
//		}
//		Set(key, base + value);
//	}

    /**
     * Decrease the size of the context by the given amount. If the size reaches zero, the context is removed.
     *
     * @param context the context identifier.
     * @param amount  the number by which to decrease the size
     */
    void decrementBy(Resource context, long amount) {
        contextInfoMap.computeIfPresent(context, (c, size) -> size <= amount ? null : size - amount);
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
        return contextInfoMap.keySet().iterator();
    }

    public void clear() {
        if (!contextInfoMap.isEmpty()) {
            contextInfoMap.clear();
            contentsChanged = true;
        }
        RockDbUtils.recreateCfRockDb(this.store.cfHandlesMap, this.store.cfOpts, this.store.rocksDB, NativeSailStore.CONTEXTS_CF);
    }

    void close() {
    }

    void sync() throws IOException {
        if (contentsChanged) {
            // Flush the changes to disk
            writeContextsToFile();
            contentsChanged = false;
        }
    }

    private void writeContextsToFile() throws IOException {
        synchronized (file) {
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                out.write(MAGIC_NUMBER);
                out.writeByte(FILE_FORMAT_VERSION);
                out.writeInt(contextInfoMap.size());
                for (Map.Entry<Resource, Long> entry : contextInfoMap.entrySet()) {
                    out.writeLong(entry.getValue());
                    out.writeBoolean(entry.getKey() instanceof IRI);
                    out.writeUTF(entry.getKey().stringValue());
                }
            }
        }
    }

    private void initializeContextCache() throws IOException {
        logger.debug("initializing context cache");
        try (CloseableIteration<Resource, SailException> contextIter = store.getContexts()) {
            while (contextIter.hasNext()) {
                increment(contextIter.next());
            }
        }
    }

    private void readContextsFromFile() throws IOException {
        synchronized (file) {
            if (!file.exists()) {
                throw new IOException("context index file " + file + " does not exist");
            }

            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                byte[] magicNumber = IOUtil.readBytes(in, MAGIC_NUMBER.length);
                if (!Arrays.equals(magicNumber, MAGIC_NUMBER)) {
                    throw new IOException("File doesn't contain compatible context data");
                }

                byte version = in.readByte();
                if (version > FILE_FORMAT_VERSION) {
                    throw new IOException("Unable to read context file; it uses a newer file format");
                } else if (version != FILE_FORMAT_VERSION) {
                    throw new IOException("Unable to read context file; invalid file format version: " + version);
                }

                final int size = in.readInt();

                while (true) {
                    try {
                        long contextSize = in.readLong();
                        boolean isIRI = in.readBoolean();
                        String contextId = in.readUTF();

                        Resource context = isIRI ? valueFactory.createIRI(contextId)
                                : valueFactory.createBNode(contextId);
                        contextInfoMap.put(context, contextSize);
                    } catch (EOFException e) {
                        break;
                    } catch (IllegalArgumentException e) {
                        throw new IOException("unable to parse context identifier: ", e);
                    }
                }

                if (contextInfoMap.size() != size) {
                    throw new IOException("Unable to read context file; size checksum validation failed");
                }
            }
        }
    }

}
