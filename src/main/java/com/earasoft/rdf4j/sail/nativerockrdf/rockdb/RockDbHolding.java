package com.earasoft.rdf4j.sail.nativerockrdf.rockdb;

import org.eclipse.rdf4j.sail.SailException;
import org.rocksdb.*;

import java.util.*;

public class RockDbHolding {
    public static final String NAMESPACES_CF = "namespaces_cf";
    public static final String CONTEXTS_CF = "contexts_cf";
    public static final String VALUE_CF = "value_cf";
    public static final String INDEX_SPOC_CF = "index_spoc_cf";
    public static final String INDEX_POSC_CF = "index_posc_cf";
    public static final String INDEX_COSP_CF = "index_cosp_cf";

    public final CompressionOptions compressionOptions;
    public final List cfDescriptors;
    public final ColumnFamilyOptions cfOpts;
    public final List<ColumnFamilyHandle> cfHandles;
    public final DBOptions dbOptions;
    public final RocksDB rocksDB;
    private final SstFileManager sstFileManager;

    public Map<String, ColumnFamilyHandle> cfHandlesMap = new HashMap<>();

    public RockDbHolding() {
        // rocksdb
        https:
//github.com/hugegraph/hugegraph/blob/master/hugegraph-rocksdb/src/main/java/com/baidu/hugegraph/backend/store/rocksdb/RocksDBOptions.java
        try {
            // TODO initOptions
            this.sstFileManager = new SstFileManager(Env.getDefault());

            compressionOptions = new CompressionOptions()
                    .setEnabled(true);

            cfOpts = new ColumnFamilyOptions()
                    .setOptimizeFiltersForHits(true)
                    .optimizeLevelStyleCompaction()
                    .optimizeUniversalStyleCompaction()
                    .setCompressionType(CompressionType.LZ4_COMPRESSION)
                    .setCompressionOptions(compressionOptions)
                    .setBottommostCompressionType(CompressionType.LZ4_COMPRESSION)
                    .setBottommostCompressionOptions(compressionOptions)
//						.setMergeOperator(mergeOp)
                    .setCompressionPerLevel(Arrays.asList(CompressionType.LZ4_COMPRESSION))
                    // https://github.com/facebook/rocksdb/tree/master/utilities/merge_operators
                    .setMergeOperatorName("uint64add"); // uint64add/stringappend
            ;

            cfDescriptors = Arrays.asList(
                    // RocksDB.DEFAULT_COLUMN_FAMILY is required as first column family
                    new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts)
                    // replaces namespaceStore
                    , new ColumnFamilyDescriptor(RockDbHolding.NAMESPACES_CF.getBytes(), cfOpts) // done
                    // replaces contextStore
                    , new ColumnFamilyDescriptor(RockDbHolding.CONTEXTS_CF.getBytes(), cfOpts)
                    // Replaces tripleStore
                    , new ColumnFamilyDescriptor(RockDbHolding.INDEX_SPOC_CF.getBytes(), cfOpts)
                    , new ColumnFamilyDescriptor(RockDbHolding.INDEX_POSC_CF.getBytes(), cfOpts)
                    , new ColumnFamilyDescriptor(RockDbHolding.INDEX_COSP_CF.getBytes(), cfOpts)
            );
            dbOptions = new DBOptions()
                    .setCreateIfMissing(true)
                    .setCreateMissingColumnFamilies(true)
                    .setAllowConcurrentMemtableWrite(true)
                    .setIncreaseParallelism(12)

//                    .setAllowMmapWrites(true)
                    .setMaxSubcompactions(12);

            cfHandles = new ArrayList<>();

            rocksDB = RocksDB.open(this.dbOptions,
                    "data2_rocksdb",
                    cfDescriptors,
                    cfHandles);
            for (ColumnFamilyHandle columnFamilyHandle : cfHandles) {
                this.cfHandlesMap.put(new String(columnFamilyHandle.getName()), columnFamilyHandle);
            }
        } catch (RocksDBException e) {
            throw new SailException(e);
        }
    }

    public void closeRockdb() {
        try {
            compressionOptions.close();
        } finally {
            try {
                cfOpts.close();
            } finally {
                try {
                    dbOptions.close();
                } finally {
                    try {
                        // NOTE frees the column family handles before freeing the db
                        for (final ColumnFamilyHandle cfHandle : this.cfHandles) {
                            cfHandle.close();
                        }
                    } finally {
                        rocksDB.close();
                    }
                }
            }
        }
    }

}
