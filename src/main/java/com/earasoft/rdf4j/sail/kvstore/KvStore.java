package com.earasoft.rdf4j.sail.kvstore;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractNotifyingSail;

public class KvStore extends AbstractNotifyingSail implements FederatedServiceResolverClient {

    @Override
    public void setFederatedServiceResolver(FederatedServiceResolver federatedServiceResolver) {

    }

    @Override
    protected void shutDownInternal() throws SailException {

    }

    @Override
    protected NotifyingSailConnection getConnectionInternal() throws SailException {
        return null;
    }

    @Override
    public boolean isWritable() throws SailException {
        return false;
    }

    @Override
    public ValueFactory getValueFactory() {
        return null;
    }
}
