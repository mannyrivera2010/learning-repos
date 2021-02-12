package com.earasoft.rdf4j.sail.rockdbstore;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.EvaluationStatistics;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.base.SailSource;
import org.eclipse.rdf4j.sail.base.SailStore;

public class RockDbStore implements SailStore {
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
}
