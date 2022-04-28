package com.earasoft.rdf4j.sail.rocksDbStore.rockdb;

import com.earasoft.rdf4j.sail.rocksDbStore.rockdb.iterator.RockDbIteratorEntry;
import com.earasoft.rdf4j.sail.rocksDbStore.rockdb.iterator.RockDbWrapperIterator;
import com.earasoft.rdf4j.sail.rocksDbStore.rockdb.iterator.RockRegIterator;
import org.eclipse.rdf4j.sail.SailException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rocksdb.*;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class RockDbState {

    @NotNull
    public static <S> RockDbWrapperIterator<S> createRocksDbIteratorForCFColumn(
            RockDbHolding rhold,
            String namespacesCf,
            Function<RockDbIteratorEntry, S> simpleNamespaceFunc) {
        ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(namespacesCf);
        RockDbWrapperIterator<S> rocksWrapperIterator = new RockDbWrapperIterator<>(
                columnFamilyHandle, rhold.rocksDB, simpleNamespaceFunc);
        return rocksWrapperIterator;
    }

    @NotNull
    public static <S> RockRegIterator<S> createRocksRegIteratorForCFColumn(
            RockDbHolding rhold,
            String namespacesCf,
            Function<RockDbIteratorEntry, S> simpleNamespaceFunc) {
        ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(namespacesCf);
        RockRegIterator<S> rocksWrapperIterator = new RockRegIterator<>(
                columnFamilyHandle, rhold.rocksDB, simpleNamespaceFunc);
        // Iterator.next(); // this causes seg fault
        return rocksWrapperIterator;
    }

    @Nullable
    public static byte[] getKey(RockDbHolding rhold, String columnFamily, byte[] keyBytes) {
        ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(columnFamily);
        try {
            return rhold.rocksDB.get(
                    columnFamilyHandle,
                    keyBytes
            );
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    /**
     * https://github.com/hugegraph/hugegraph/commit/8c396015eb37027ea8aa12b20e5372b5d5dbf9e1
     *
     * @param columnFamily
     * @throws SailException
     */
    public static void recreateColumnFamily(RockDbHolding rhold, String columnFamily) throws SailException {
        try {
            if(!rhold.cfHandlesMap.containsKey(columnFamily)){
                throw new SailException("column family [" + columnFamily + "] does not exits");
            }

            ColumnFamilyHandle cfh = rhold.cfHandlesMap.get(columnFamily);
            rhold.rocksDB.dropColumnFamily(cfh);
            cfh.close(); // free cfHandle
            rhold.cfHandlesMap.remove(columnFamily);

            ColumnFamilyDescriptor cfd = new ColumnFamilyDescriptor(
                    columnFamily.getBytes(), rhold.cfOpts); // memory leak need to close?
            rhold.cfHandlesMap.put(columnFamily, rhold.rocksDB.createColumnFamily(cfd));
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    public static void deleteKeyRockDb(RockDbHolding rhold, String key, String columnFamilyName) throws SailException {
        ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(columnFamilyName);
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            rhold.rocksDB.delete(
                    columnFamilyHandle,
                    keyBytes
            );
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    public static void setKeyRockDbBytes(RockDbHolding rhold,
                                         byte[] key, byte[]  value, String columnFamilyName) throws SailException {
        try {
            ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(columnFamilyName);

            WriteBatch writeBatch = new WriteBatch();

            if(key.length == 0){
                throw new RuntimeException("WHY");
            }
            writeBatch.put(columnFamilyHandle,
                    key,
                    value);


            WriteOptions writeOptions = new WriteOptions();
            rhold.rocksDB.write(writeOptions, writeBatch);
            writeBatch.close();
            writeOptions.close();

        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    public static void setKeyRockDb(RockDbHolding rhold,
                                    String key, String value, String columnFamilyName) throws SailException {
        try {
            ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(columnFamilyName);


//            rhold.rocksDB.put(
//                    columnFamilyHandle,
//                    key.getBytes(StandardCharsets.UTF_8),
//                    value.getBytes(StandardCharsets.UTF_8)
//            );

            WriteBatch writeBatch = new WriteBatch();

            writeBatch.put(columnFamilyHandle,
                    key.getBytes(StandardCharsets.UTF_8),
                    value.getBytes(StandardCharsets.UTF_8)
            );


            rhold.rocksDB.write(new WriteOptions(), writeBatch);

            writeBatch.close(); // so important to close this
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }
}
