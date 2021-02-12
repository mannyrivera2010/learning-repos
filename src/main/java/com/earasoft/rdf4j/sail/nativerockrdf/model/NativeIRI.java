/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package com.earasoft.rdf4j.sail.nativerockrdf.model;

import org.eclipse.rdf4j.model.impl.SimpleIRI;
import com.earasoft.rdf4j.sail.nativerockrdf.ValueStoreRevision;

public class NativeIRI extends SimpleIRI implements NativeResource {

	private static final long serialVersionUID = -5888138591826143179L;

	/*-----------*
	 * Constants *
	 *-----------*/

	private volatile ValueStoreRevision revision;

	private volatile int internalID;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected NativeIRI(ValueStoreRevision revision, int internalID) {
		super();
		setInternalID(internalID, revision);
	}

	public NativeIRI(ValueStoreRevision revision, String uri) {
		this(revision, uri, UNKNOWN_ID);
	}

	public NativeIRI(ValueStoreRevision revision, String uri, int internalID) {
		super(uri);
		setInternalID(internalID, revision);
	}

	public NativeIRI(ValueStoreRevision revision, String namespace, String localname) {
		this(revision, namespace + localname);
	}

	public NativeIRI(ValueStoreRevision revision, String namespace, String localname, int internalID) {
		this(revision, namespace + localname, internalID);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setInternalID(int internalID, ValueStoreRevision revision) {
		this.internalID = internalID;
		this.revision = revision;
	}

	@Override
	public ValueStoreRevision getValueStoreRevision() {
		return revision;
	}

	@Override
	public int getInternalID() {
		return internalID;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof NativeIRI && internalID != UNKNOWN_ID) {
			NativeIRI otherNativeURI = (NativeIRI) o;

			if (otherNativeURI.internalID != UNKNOWN_ID && revision.equals(otherNativeURI.revision)) {
				// NativeURI's from the same revision of the same native store, with
				// both ID's set
				return internalID == otherNativeURI.internalID;
			}
		}

		return super.equals(o);
	}
}
