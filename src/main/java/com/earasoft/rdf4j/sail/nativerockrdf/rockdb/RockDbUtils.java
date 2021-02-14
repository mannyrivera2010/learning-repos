package com.earasoft.rdf4j.sail.nativerockrdf.rockdb;

import com.earasoft.rdf4j.sail.nativerockrdf.NativeSailStore;
import org.eclipse.rdf4j.sail.SailException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rocksdb.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

public class RockDbUtils {

    private static byte[] longToByteA(long value) {
        return ByteBuffer.allocate(Long.BYTES)
                .order(ByteOrder.nativeOrder())
                .putLong(value).array();
    }

    private static long byteAToLong(byte[] bytes) {
        assert bytes.length == Long.BYTES;
        return ByteBuffer.wrap(bytes)
                .order(ByteOrder.nativeOrder())
                .getLong();
    }

    private static byte[] intToByteA(int value) {
        return ByteBuffer.allocate(Integer.BYTES)
                .order(ByteOrder.nativeOrder())
                .putLong(value).array();
    }

    private static int byteAToInt(byte[] bytes) {
        assert bytes.length == Integer.BYTES;
        return ByteBuffer.wrap(bytes)
                .order(ByteOrder.nativeOrder())
                .getInt();
    }



    @NotNull
    public static <S> RockDbWrapperIterator<S> createRocksDbIteratorForCFColumn(
            Map<String, ColumnFamilyHandle> cfHandlesMap,
            RocksDB rocksDB,
            String namespacesCf,
            Function<RockDbIteratorEntry, S> simpleNamespaceFunc) {
        ColumnFamilyHandle columnFamilyHandle = cfHandlesMap.get(namespacesCf);
        RockDbWrapperIterator<S> rocksWrapperIterator = new RockDbWrapperIterator<>(columnFamilyHandle, rocksDB, simpleNamespaceFunc);
        return rocksWrapperIterator;
    }

    @NotNull
    public static <S> RockRegIterator<S> createRocksRegIteratorForCFColumn(
            Map<String, ColumnFamilyHandle> cfHandlesMap,
            RocksDB rocksDB,
            String namespacesCf,
            Function<RockDbIteratorEntry, S> simpleNamespaceFunc) {
        ColumnFamilyHandle columnFamilyHandle = cfHandlesMap.get(namespacesCf);
        RockRegIterator<S> rocksWrapperIterator = new RockRegIterator<>(columnFamilyHandle, rocksDB, simpleNamespaceFunc);
        // Iterator.next(); // this causes seg fault
        return rocksWrapperIterator;
    }

    @Nullable
    public static byte[] getKeyRockDbBytes(Map<String, ColumnFamilyHandle> cfHandlesMap, RocksDB rocksDB, String key) {
        ColumnFamilyHandle columnFamilyHandle = cfHandlesMap.get(NativeSailStore.NAMESPACES_CF);
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
    public static String getKeyRockDb(Map<String, ColumnFamilyHandle> cfHandlesMap, RocksDB rocksDB, String key) {
        ColumnFamilyHandle columnFamilyHandle = cfHandlesMap.get(NativeSailStore.NAMESPACES_CF);
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] value = rocksDB.get(
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
            rocksDB.delete(
                    columnFamilyHandle,
                    keyBytes
            );
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    public static void setKeyRockDbBytes(Map<String, ColumnFamilyHandle> cfHandlesMap, RocksDB rocksDB,
                                    byte[] key,  byte[]  value, String columnFamilyName) throws SailException {
        try {
            ColumnFamilyHandle columnFamilyHandle = cfHandlesMap.get(columnFamilyName);

            rocksDB.put(
                    columnFamilyHandle,
                    key,
                    value
            );
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    public static void setKeyRockDb(Map<String, ColumnFamilyHandle> cfHandlesMap, RocksDB rocksDB,
                                    String key, String value, String columnFamilyName) throws SailException {
        try {
            ColumnFamilyHandle columnFamilyHandle = cfHandlesMap.get(columnFamilyName);

            rocksDB.put(
                    columnFamilyHandle,
                    key.getBytes(StandardCharsets.UTF_8),
                    value.getBytes(StandardCharsets.UTF_8)
            );
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }
}
