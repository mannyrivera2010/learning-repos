package com.earasoft.rdf4j.sail.kvstore;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.EvaluationStatistics;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.base.SailSource;
import org.eclipse.rdf4j.sail.base.SailStore;

import java.io.File;
import java.util.List;

public class KvStoreSail implements Sail  {

    @Override
    public void setDataDir(File dataDir) {

    }

    @Override
    public File getDataDir() {
        return null;
    }

    @Override
    public void initialize() throws SailException {

    }

    @Override
    public void shutDown() throws SailException {

    }

    @Override
    public boolean isWritable() throws SailException {
        return false;
    }

    @Override
    public SailConnection getConnection() throws SailException {
        return null;
    }

    @Override
    public ValueFactory getValueFactory() {
        return null;
    }

    @Override
    public List<IsolationLevel> getSupportedIsolationLevels() {
        return null;
    }

    @Override
    public IsolationLevel getDefaultIsolationLevel() {
        return null;
    }
}
