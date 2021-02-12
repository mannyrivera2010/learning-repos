package com.earasoft.rdf4j.sail.kvstore;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.EvaluationStatistics;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.UnknownSailTransactionStateException;
import org.eclipse.rdf4j.sail.UpdateContext;
import org.eclipse.rdf4j.sail.base.SailSource;
import org.eclipse.rdf4j.sail.base.SailStore;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;
import org.eclipse.rdf4j.sail.helpers.AbstractSailConnection;

import java.io.File;
import java.util.List;

/**
 *
 */
public class KvStoreSail implements SailStore {

    @Override
    public ValueFactory getValueFactory() {
        return null;
    }

    @Override
    public EvaluationStatistics getEvaluationStatistics() {
        return null;
    }

    @Override
    public SailSource getExplicitSailSource() {
        return null;
    }

    @Override
    public SailSource getInferredSailSource() {
        return null;
    }

    @Override
    public void close() throws SailException {

    }

//    @Override
//    public void setDataDir(File dataDir) {
//
//    }
//
//    @Override
//    public File getDataDir() {
//        return null;
//    }
//
//    @Override
//    public void initialize() throws SailException {
//
//    }
//
//    @Override
//    public void shutDown() throws SailException {
//
//    }
//
//    @Override
//    protected void shutDownInternal() throws SailException {
//
//    }
//
//    @Override
//    public boolean isWritable() throws SailException {
//        return false;
//    }
//
//
//    @Override
//    protected SailConnection getConnectionInternal() throws SailException {
//        //        try {
////        return new KvStoreConnection(this);
//        return null;
////        } catch (IOException e) {
////            throw new SailException(e);
////        }
//    }
//
//    @Override
//    public ValueFactory getValueFactory() {
//        return null;
//    }
//
//    @Override
//    public List<IsolationLevel> getSupportedIsolationLevels() {
//        return null;
//    }
//
//    @Override
//    public IsolationLevel getDefaultIsolationLevel() {
//        return null;
//    }
}
