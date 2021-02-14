package com.earasoft.rdf4j.sail.nativerockrdf.rockdb;

import org.eclipse.rdf4j.sail.SailException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rocksdb.*;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class RockDbUtils {

    @NotNull
    public static <S> RockDbWrapperIterator<S> createRocksDbIteratorForCFColumn(
            RockDbHolding rhold,
            String namespacesCf,
            Function<RockDbIteratorEntry, S> simpleNamespaceFunc) {
        ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(namespacesCf);
        RockDbWrapperIterator<S> rocksWrapperIterator = new RockDbWrapperIterator<>(columnFamilyHandle, rhold.rocksDB, simpleNamespaceFunc);
        return rocksWrapperIterator;
    }

    @NotNull
    public static <S> RockRegIterator<S> createRocksRegIteratorForCFColumn(
            RockDbHolding rhold,
            String namespacesCf,
            Function<RockDbIteratorEntry, S> simpleNamespaceFunc) {
        ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(namespacesCf);
        RockRegIterator<S> rocksWrapperIterator = new RockRegIterator<>(columnFamilyHandle, rhold.rocksDB, simpleNamespaceFunc);
        // Iterator.next(); // this causes seg fault
        return rocksWrapperIterator;
    }

    @Nullable
    public static byte[] getKeyRockDbBytes(RockDbHolding rhold, RocksDB rocksDB, String key) {
        ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(RockDbHolding.NAMESPACES_CF);
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] value = rocksDB.get(
                    columnFamilyHandle,
                    keyBytes
            );

            if (value == null)
                return null;

            return value;
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    @Nullable
    public static String getKeyRockDb(RockDbHolding rhold, String key) {
        ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(RockDbHolding.NAMESPACES_CF);
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] value = rhold.rocksDB.get(
                    columnFamilyHandle,
                    keyBytes
            );

            if (value == null)
                return null;

            return new String(value);
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    /**
     * https://github.com/hugegraph/hugegraph/commit/8c396015eb37027ea8aa12b20e5372b5d5dbf9e1
     *
     * @param namespacesCf
     * @throws SailException
     */
    public static void recreateCfRockDb(RockDbHolding rhold, String namespacesCf) throws SailException {
        ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(namespacesCf);
        try {
            rhold.rocksDB.dropColumnFamily(columnFamilyHandle);
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
        columnFamilyHandle.close(); // free cfHandle
        rhold.cfHandlesMap.remove(namespacesCf);
        try {
            ColumnFamilyDescriptor columnFamilyDescriptor = new ColumnFamilyDescriptor(namespacesCf.getBytes(), rhold.cfOpts); // memory leak need to close?
            rhold.cfHandlesMap.put(namespacesCf, rhold.rocksDB.createColumnFamily(columnFamilyDescriptor));
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

            rhold.rocksDB.put(
                    columnFamilyHandle,
                    key,
                    value
            );
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    public static void setKeyRockDb(RockDbHolding rhold,
                                    String key, String value, String columnFamilyName) throws SailException {
        try {
            ColumnFamilyHandle columnFamilyHandle = rhold.cfHandlesMap.get(columnFamilyName);

            rhold.rocksDB.put(
                    columnFamilyHandle,
                    key.getBytes(StandardCharsets.UTF_8),
                    value.getBytes(StandardCharsets.UTF_8)
            );
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }
}
