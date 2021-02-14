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
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.OpenRDFUtil;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.common.iteration.ConvertingIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import org.eclipse.rdf4j.common.iteration.FilterIteration;
import org.eclipse.rdf4j.common.iteration.UnionIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.EvaluationStatistics;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.base.BackingSailSource;
import org.eclipse.rdf4j.sail.base.SailDataset;
import org.eclipse.rdf4j.sail.base.SailSink;
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
class NativeSailStore implements SailStore {

	static {
		RocksDB.loadLibrary();
	}

	final Logger logger = LoggerFactory.getLogger(NativeSailStore.class);

	private final TripleStore tripleStore;

	private final ValueStore valueStore;

//	private final NamespaceStore namespaceStore;

	private final ContextStore contextStore;

	/**
	 * A lock to control concurrent access by {@link NativeSailSink} to the TripleStore, ValueStore, and NamespaceStore.
	 * Each sink method that directly accesses one of these store obtains the lock and releases it immediately when
	 * done.
	 */
	private final ReentrantLock sinkStoreAccessLock = new ReentrantLock();

	/**
	 * Boolean indicating whether any {@link NativeSailSink} has started a transaction on the {@link TripleStore}.
	 */
	private final AtomicBoolean storeTxnStarted = new AtomicBoolean(false);

	/**
	 * Creates a new {@link NativeSailStore} with the default cache sizes.
	 */
	public NativeSailStore(File dataDir, String tripleIndexes) throws IOException, SailException {
		this(dataDir, tripleIndexes, false, ValueStore.VALUE_CACHE_SIZE, ValueStore.VALUE_ID_CACHE_SIZE,
				ValueStore.NAMESPACE_CACHE_SIZE, ValueStore.NAMESPACE_ID_CACHE_SIZE);
	}

	final CompressionOptions compressionOptions;
	final List cfDescriptors;
	final ColumnFamilyOptions cfOpts;
	final List<ColumnFamilyHandle> cfHandles;
	final DBOptions dbOptions;
	final RocksDB rocksDB;
	private final SstFileManager sstFileManager;
	public static final String NAMESPACES_CF = "namespaces_cf";
	public static final String CONTEXTS_CF = "contexts_cf";
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
			contextStore = new ContextStore(this, dataDir);

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
			.setCompressionPerLevel(Arrays.asList(CompressionType.LZ4_COMPRESSION))

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
					.setIncreaseParallelism(4)

//                    .setAllowMmapWrites(true)
					.setMaxSubcompactions(4);

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
		return new NativeSailSource(true);
	}

	@Override
	public SailSource getInferredSailSource() {
		return new NativeSailSource(false);
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
	CloseableIteration<? extends Statement, SailException> createStatementIterator(Resource subj, IRI pred, Value obj,
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




	private final class NativeSailSource extends BackingSailSource {

		private final boolean explicit;

		public NativeSailSource(boolean explicit) {
			this.explicit = explicit;
		}

		@Override
		public SailSource fork() {
			throw new UnsupportedOperationException("This store does not support multiple datasets");
		}

		@Override
		public SailSink sink(IsolationLevel level) throws SailException {
			return new NativeSailSink(explicit);
		}

		@Override
		public NativeSailDataset dataset(IsolationLevel level) throws SailException {
			return new NativeSailDataset(explicit);
		}

	}

	private final class NativeSailSink implements SailSink {

		// https://graphdb.ontotext.com/documentation/enterprise/reasoning.html
		private final boolean explicit;

		public NativeSailSink(boolean explicit) throws SailException {
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
			sinkStoreAccessLock.lock();
			try {
				try {
					valueStore.sync();
				} finally {
					try {
//						namespaceStore.sync();
					} finally {
						try {
							contextStore.sync();
						} finally {
							if (storeTxnStarted.get()) {
								tripleStore.commit();
								// do not set flag to false until _after_ commit is succesfully completed.
								storeTxnStarted.set(false);
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
				sinkStoreAccessLock.unlock();
			}
		}

		@Override
		public void setNamespace(String prefix, String name) throws SailException {
			sinkStoreAccessLock.lock();
			try {
				startTriplestoreTransaction();
//				namespaceStore.setNamespace(prefix, name);

				RockDbUtils.setKeyRockDb(cfHandlesMap, rocksDB, prefix, name, NAMESPACES_CF);
			} finally {
				sinkStoreAccessLock.unlock();
			}
		}

		@Override
		public void removeNamespace(String prefix) throws SailException {
			sinkStoreAccessLock.lock();
			try {
				startTriplestoreTransaction();
//				namespaceStore.removeNamespace(prefix);

				RockDbUtils.deleteKeyRockDb(cfHandlesMap, rocksDB, prefix, NAMESPACES_CF);
			} finally {
				sinkStoreAccessLock.unlock();
			}
		}



		@Override
		public void clearNamespaces() throws SailException {
			sinkStoreAccessLock.lock();
			try {
				startTriplestoreTransaction();
//				namespaceStore.clear();
				RockDbUtils.recreateCfRockDb(cfHandlesMap, cfOpts, rocksDB, NAMESPACES_CF);
			} finally {
				sinkStoreAccessLock.unlock();
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

			if (storeTxnStarted.compareAndSet(false, true)) {
				try {
					tripleStore.startTransaction();
				} catch (IOException e) {
					storeTxnStarted.set(false);
					throw new SailException(e);
				}
			}
		}

		private boolean addStatement(Resource subj, IRI pred, Value obj, boolean explicit, Resource... contexts)
				throws SailException {
			OpenRDFUtil.verifyContextNotNull(contexts);
			boolean result = false;
			sinkStoreAccessLock.lock();
			try {
				startTriplestoreTransaction();
				int subjID = valueStore.storeValue(subj);
				int predID = valueStore.storeValue(pred);
				int objID = valueStore.storeValue(obj);

				if (contexts.length == 0) {
					contexts = new Resource[] { null };
				}

				for (Resource context : contexts) {
					int contextID = 0;
					if (context != null) {
						contextID = valueStore.storeValue(context);
					}

					boolean wasNew = tripleStore.storeTriple(subjID, predID, objID, contextID, explicit);
					if (wasNew && context != null) {
						contextStore.increment(context);
					}
					result |= wasNew;
				}
			} catch (IOException e) {
				throw new SailException(e);
			} catch (RuntimeException e) {
				logger.error("Encountered an unexpected problem while trying to add a statement", e);
				throw e;
			} finally {
				sinkStoreAccessLock.unlock();
			}

			return result;
		}

		private long removeStatements(Resource subj, IRI pred, Value obj, boolean explicit, Resource... contexts)
				throws SailException {
			OpenRDFUtil.verifyContextNotNull(contexts);

			sinkStoreAccessLock.lock();
			try {
				startTriplestoreTransaction();
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

				final int[] contextIds = new int[contexts.length == 0 ? 1 : contexts.length];
				if (contexts.length == 0) { // remove from all contexts
					contextIds[0] = NativeValue.UNKNOWN_ID;
				} else {
					for (int i = 0; i < contexts.length; i++) {
						Resource context = contexts[i];
						if (context == null) {
							contextIds[i] = 0;
						} else {
							int id = valueStore.getID(context);
							// unknown_id cannot be used (would result in removal from all contexts)
							contextIds[i] = (id != NativeValue.UNKNOWN_ID) ? id : Integer.MIN_VALUE;
						}
					}
				}

				long removeCount = 0;
				for (int contextId : contextIds) {
					Map<Integer, Long> result = tripleStore.removeTriplesByContext(subjID, predID, objID, contextId,
							explicit);

					for (Entry<Integer, Long> entry : result.entrySet()) {
						Integer entryContextId = entry.getKey();
						if (entryContextId > 0) {
							Resource modifiedContext = (Resource) valueStore.getValue(entryContextId);
							contextStore.decrementBy(modifiedContext, entry.getValue());
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
				sinkStoreAccessLock.unlock();
			}
		}
	}

	/**
	 * @author James Leigh
	 */
	private final class NativeSailDataset implements SailDataset {

		private final boolean explicit;

		public NativeSailDataset(boolean explicit) throws SailException {
			this.explicit = explicit;
		}

		@Override
		public void close() {
			// no-op
		}

		@Override
		public String getNamespace(String prefix) throws SailException {
			return RockDbUtils.getKeyRockDb(cfHandlesMap, rocksDB, prefix);
		}

		@Override
		public CloseableIteration<? extends Namespace, SailException> getNamespaces() {
			return RockDbUtils.createIteratorForCFColumn(cfHandlesMap, rocksDB, NAMESPACES_CF);
		}

		@Override
		public CloseableIteration<? extends Resource, SailException> getContextIDs() throws SailException {
			return new CloseableIteratorIteration<Resource, SailException>(contextStore.iterator());
		}

		@Override
		public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, IRI pred, Value obj,
				Resource... contexts) throws SailException {
			try {
				return createStatementIterator(subj, pred, obj, explicit, contexts);
			} catch (IOException e) {
				throw new SailException("Unable to get statements", e);
			}
		}
	} // end


}
