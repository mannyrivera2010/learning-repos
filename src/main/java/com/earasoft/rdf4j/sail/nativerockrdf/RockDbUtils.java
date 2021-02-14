package com.earasoft.rdf4j.sail.nativerockrdf;

import org.eclipse.rdf4j.sail.SailException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rocksdb.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RockDbUtils {

    @NotNull
    public static RocksWrapperIterator createIteratorForCFColumn(Map<String, ColumnFamilyHandle> cfHandlesMap, RocksDB rocksDB, String namespacesCf) {
        ColumnFamilyHandle columnFamilyHandle = cfHandlesMap.get(namespacesCf);
        RocksWrapperIterator rocksWrapperIterator = new RocksWrapperIterator(columnFamilyHandle, rocksDB);
        return rocksWrapperIterator;
    }

    @Nullable
    public static String getKeyRockDb(Map<String, ColumnFamilyHandle> cfHandlesMap, RocksDB rocksDB, String key) {
        ColumnFamilyHandle columnFamilyHandle = cfHandlesMap.get(NativeSailStore.NAMESPACES_CF);
        try {
            byte[] stringBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] value = rocksDB.get(columnFamilyHandle, stringBytes);

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
     * @param cfHandlesMap
     * @param cfOpts
     * @param rocksDB
     * @param namespacesCf
     * @throws SailException
     */
    public static void recreateCfRockDb(Map<String, ColumnFamilyHandle> cfHandlesMap, ColumnFamilyOptions cfOpts, RocksDB rocksDB, String namespacesCf) throws SailException {
        ColumnFamilyHandle columnFamilyHandle = cfHandlesMap.get(namespacesCf);
        try {
            rocksDB.dropColumnFamily(columnFamilyHandle);
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
        columnFamilyHandle.close(); // free cfHandle
        cfHandlesMap.remove(namespacesCf);
        try {
            ColumnFamilyDescriptor columnFamilyDescriptor = new ColumnFamilyDescriptor(namespacesCf.getBytes(), cfOpts); // memory leak need to close?
            cfHandlesMap.put(namespacesCf, rocksDB.createColumnFamily(columnFamilyDescriptor));
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    public static void deleteKeyRockDb(Map<String, ColumnFamilyHandle> cfHandlesMap, RocksDB rocksDB, String key, String columnFamilyName) throws SailException {
        ColumnFamilyHandle columnFamilyHandle = cfHandlesMap.get(columnFamilyName);
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            rocksDB.delete(columnFamilyHandle, keyBytes);
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    public static void setKeyRockDb(Map<String, ColumnFamilyHandle> cfHandlesMap, RocksDB rocksDB, String prefix, String name, String columnFamilyName) throws SailException {
        try {
            ColumnFamilyHandle columnFamilyHandle = cfHandlesMap.get(columnFamilyName);

            rocksDB.put(
                    columnFamilyHandle,
                    prefix.getBytes(StandardCharsets.UTF_8),
                    name.getBytes(StandardCharsets.UTF_8)
            );
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }
}
