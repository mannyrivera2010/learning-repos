/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package com.earasoft.rdf4j.sail.nativerockrdf;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.earasoft.rdf4j.sail.nativerockrdf.eval.NativeEvaluationStatistics;
import com.earasoft.rdf4j.sail.nativerockrdf.pip.NativeSailSink;
import com.earasoft.rdf4j.sail.nativerockrdf.pip.NativeSailSource;
import com.earasoft.rdf4j.sail.nativerockrdf.pip.NativeStatementIterator;
import com.earasoft.rdf4j.sail.nativerockrdf.zstore.ContextStore;
import com.earasoft.rdf4j.sail.nativerockrdf.zstore.TripleStore;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.ConvertingIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import org.eclipse.rdf4j.common.iteration.FilterIteration;
import org.eclipse.rdf4j.common.iteration.UnionIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.EvaluationStatistics;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.base.SailSource;
import org.eclipse.rdf4j.sail.base.SailStore;
import com.earasoft.rdf4j.sail.nativerockrdf.btree.RecordIterator;
import com.earasoft.rdf4j.sail.nativerockrdf.model.NativeValue;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A disk based {@link SailStore} implementation that keeps committed statements in a {@link TripleStore}.
 *
 * @author James Leigh
 */
public class NativeSailStore implements SailStore {

	static {
		RocksDB.loadLibrary();
	}

	public final Logger logger = LoggerFactory.getLogger(NativeSailStore.class);

	public final TripleStore tripleStore;
	public final ValueStore valueStore;

//	private final NamespaceStore namespaceStore;

	public final ContextStore contextStore;

	/**
	 * A lock to control concurrent access by {@link NativeSailSink} to the TripleStore, ValueStore, and NamespaceStore.
	 * Each sink method that directly accesses one of these store obtains the lock and releases it immediately when
	 * done.
	 */
	public final ReentrantLock sinkStoreAccessLock = new ReentrantLock();

	/**
	 * Boolean indicating whether any {@link NativeSailSink} has started a transaction on the {@link TripleStore}.
	 */
	public final AtomicBoolean storeTxnStarted = new AtomicBoolean(false);

	/**
	 * Creates a new {@link NativeSailStore} with the default cache sizes.
	 */
	public NativeSailStore(File dataDir, String tripleIndexes) throws IOException, SailException {
		this(dataDir, tripleIndexes, false, ValueStore.VALUE_CACHE_SIZE, ValueStore.VALUE_ID_CACHE_SIZE,
				ValueStore.NAMESPACE_CACHE_SIZE, ValueStore.NAMESPACE_ID_CACHE_SIZE);
	}

	public final CompressionOptions compressionOptions;
	public final List cfDescriptors;
	public final ColumnFamilyOptions cfOpts;
	public final List<ColumnFamilyHandle> cfHandles;
	public final DBOptions dbOptions;
	public final RocksDB rocksDB;
	private final SstFileManager sstFileManager;
	public static final String NAMESPACES_CF = "namespaces_cf";
	public static final String CONTEXTS_CF = "contexts_cf";
	public static final String VALUE_CF = "value_cf";
	public static final String INDEX_SPOC_CF = "index_spoc_cf";
	public static final String INDEX_POSC_CF = "index_posc_cf";
	public static final String INDEX_COSP_CF = "index_cosp_cf";

	public Map<String, ColumnFamilyHandle> cfHandlesMap = new HashMap<>();;
	/**
	 * Creates a new {@link NativeSailStore}.
	 */
	public NativeSailStore(File dataDir, String tripleIndexes, boolean forceSync, int valueCacheSize,
						   int valueIDCacheSize, int namespaceCacheSize, int namespaceIDCacheSize) throws IOException, SailException {
		boolean initialized = false;
		try {
//			namespaceStore = new NamespaceStore(dataDir);
			valueStore = new ValueStore(this, dataDir, forceSync, valueCacheSize, valueIDCacheSize, namespaceCacheSize,
					namespaceIDCacheSize);
			tripleStore = new TripleStore(this, dataDir, tripleIndexes, forceSync);
			contextStore = new ContextStore(this);

			// rocksdb
			https://github.com/hugegraph/hugegraph/blob/master/hugegraph-rocksdb/src/main/java/com/baidu/hugegraph/backend/store/rocksdb/RocksDBOptions.java
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
						,new ColumnFamilyDescriptor(NAMESPACES_CF.getBytes(), cfOpts) // done
						// replaces contextStore
						,new ColumnFamilyDescriptor(CONTEXTS_CF.getBytes(), cfOpts)
						// Replaces tripleStore
						,new ColumnFamilyDescriptor(INDEX_SPOC_CF.getBytes(), cfOpts)
						,new ColumnFamilyDescriptor(INDEX_POSC_CF.getBytes(), cfOpts)
						,new ColumnFamilyDescriptor(INDEX_COSP_CF.getBytes(), cfOpts)
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
				for(ColumnFamilyHandle columnFamilyHandle: cfHandles){
					this.cfHandlesMap.put(new String(columnFamilyHandle.getName()), columnFamilyHandle);
				}
			} catch (RocksDBException e) {
				throw new SailException(e);
			}
			// end rocksdb
			initialized = true;
		} finally {
			if (!initialized) {
				close();
			}
		}
	}

	@Override
	public ValueFactory getValueFactory() {
		return valueStore;
	}

	@Override
	public void close() throws SailException {
		try {
			try {
//				if (namespaceStore != null) {
//					namespaceStore.close();
//				}
			} finally {
				try {
					if (contextStore != null) {
						contextStore.close();
					}
				} finally {
					try {
						if (valueStore != null) {
							valueStore.close();
						}
					} finally {
						if (tripleStore != null) {
							tripleStore.close();
						}
					}
				}
			}

			// rocksdb
			closeRockdb();
		} catch (IOException e) {
			logger.warn("Failed to close store", e);
			throw new SailException(e);
		}
	}

	private void closeRockdb() {
		try{
			compressionOptions.close();
		}finally {
			try{
				cfOpts.close();
			}finally {
				try{
					dbOptions.close();
				}finally {
					try{
						// NOTE frees the column family handles before freeing the db
						for (final ColumnFamilyHandle cfHandle : this.cfHandles) {
							cfHandle.close();
						}
					}finally {
						rocksDB.close();
					}
				}
			}
		}
	}

	@Override
	public EvaluationStatistics getEvaluationStatistics() {
		return new NativeEvaluationStatistics(valueStore, tripleStore);
	}

	@Override
	public SailSource getExplicitSailSource() {
		return new NativeSailSource(this, true);
	}

	@Override
	public SailSource getInferredSailSource() {
		return new NativeSailSource(this, false);
	}

	List<Integer> getContextIDs(Resource... contexts) throws IOException {
		assert contexts.length > 0 : "contexts must not be empty";

		// Filter duplicates
		LinkedHashSet<Resource> contextSet = new LinkedHashSet<>();
		Collections.addAll(contextSet, contexts);

		// Fetch IDs, filtering unknown resources from the result
		List<Integer> contextIDs = new ArrayList<>(contextSet.size());
		for (Resource context : contextSet) {
			if (context == null) {
				contextIDs.add(0);
			} else {
				int contextID = valueStore.getID(context);
				if (contextID != NativeValue.UNKNOWN_ID) {
					contextIDs.add(contextID);
				}
			}
		}

		return contextIDs;
	}

	CloseableIteration<Resource, SailException> getContexts() throws IOException {
		RecordIterator btreeIter = tripleStore.getAllTriplesSortedByContext(false);
		CloseableIteration<? extends Statement, SailException> stIter1;
		if (btreeIter == null) {
			// Iterator over all statements
			stIter1 = createStatementIterator(null, null, null, true);
		} else {
			stIter1 = new NativeStatementIterator(btreeIter, valueStore);
		}

		FilterIteration<Statement, SailException> stIter2 = new FilterIteration<Statement, SailException>(
				stIter1) {
			@Override
			protected boolean accept(Statement st) {
				return st.getContext() != null;
			}
		};

		return new ConvertingIteration<Statement, Resource, SailException>(stIter2) {
			@Override
			protected Resource convert(Statement sourceObject) throws SailException {
				return sourceObject.getContext();
			}
		};
	}

	/**
	 * Creates a statement iterator based on the supplied pattern.
	 *
	 * @param subj     The subject of the pattern, or <tt>null</tt> to indicate a wildcard.
	 * @param pred     The predicate of the pattern, or <tt>null</tt> to indicate a wildcard.
	 * @param obj      The object of the pattern, or <tt>null</tt> to indicate a wildcard.
	 * @param contexts The context(s) of the pattern. Note that this parameter is a vararg and as such is optional. If
	 *                 no contexts are supplied the method operates on the entire repository.
	 * @return A StatementIterator that can be used to iterate over the statements that match the specified pattern.
	 */
	public CloseableIteration<? extends Statement, SailException> createStatementIterator(Resource subj, IRI pred, Value obj,
																				   boolean explicit, Resource... contexts) throws IOException {
		int subjID = NativeValue.UNKNOWN_ID;
		if (subj != null) {
			subjID = valueStore.getID(subj);
			if (subjID == NativeValue.UNKNOWN_ID) {
				return new EmptyIteration<>();
			}
		}

		int predID = NativeValue.UNKNOWN_ID;
		if (pred != null) {
			predID = valueStore.getID(pred);
			if (predID == NativeValue.UNKNOWN_ID) {
				return new EmptyIteration<>();
			}
		}

		int objID = NativeValue.UNKNOWN_ID;
		if (obj != null) {
			objID = valueStore.getID(obj);

			if (objID == NativeValue.UNKNOWN_ID) {
				return new EmptyIteration<>();
			}
		}

		List<Integer> contextIDList = new ArrayList<>(contexts.length);
		if (contexts.length == 0) {
			contextIDList.add(NativeValue.UNKNOWN_ID);
		} else {
			for (Resource context : contexts) {
				if (context == null) {
					contextIDList.add(0);
				} else {
					int contextID = valueStore.getID(context);

					if (contextID != NativeValue.UNKNOWN_ID) {
						contextIDList.add(contextID);
					}
				}
			}
		}

		ArrayList<NativeStatementIterator> perContextIterList = new ArrayList<>(contextIDList.size());

		for (int contextID : contextIDList) {
			RecordIterator btreeIter = tripleStore.getTriples(subjID, predID, objID, contextID, explicit, false);

			perContextIterList.add(new NativeStatementIterator(btreeIter, valueStore));
		}

		if (perContextIterList.size() == 1) {
			return perContextIterList.get(0);
		} else {
			return new UnionIteration<>(perContextIterList);
		}
	}

	double cardinality(Resource subj, IRI pred, Value obj, Resource context) throws IOException {
		int subjID = NativeValue.UNKNOWN_ID;
		if (subj != null) {
			subjID = valueStore.getID(subj);
			if (subjID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		int predID = NativeValue.UNKNOWN_ID;
		if (pred != null) {
			predID = valueStore.getID(pred);
			if (predID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		int objID = NativeValue.UNKNOWN_ID;
		if (obj != null) {
			objID = valueStore.getID(obj);
			if (objID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		int contextID = NativeValue.UNKNOWN_ID;
		if (context != null) {
			contextID = valueStore.getID(context);
			if (contextID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		return tripleStore.cardinality(subjID, predID, objID, contextID);
	}


}
