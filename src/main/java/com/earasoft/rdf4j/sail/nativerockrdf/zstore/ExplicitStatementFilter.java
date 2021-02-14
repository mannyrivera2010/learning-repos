package com.earasoft.rdf4j.sail.nativerockrdf.zstore;

import com.earasoft.rdf4j.sail.nativerockrdf.btree.RecordIterator;

import java.io.IOException;

class ExplicitStatementFilter implements RecordIterator {

    private final RecordIterator wrappedIter;

    public ExplicitStatementFilter(RecordIterator wrappedIter) {
        this.wrappedIter = wrappedIter;
    }

    @Override
    public byte[] next() throws IOException {
        byte[] result;

        while ((result = wrappedIter.next()) != null) {
            byte flags = result[TripleStore.FLAG_IDX];
            boolean explicit = (flags & TripleStore.EXPLICIT_FLAG) != 0;
            boolean toggled = (flags & TripleStore.TOGGLE_EXPLICIT_FLAG) != 0;

            if (explicit != toggled) {
                // Statement is either explicit and hasn't been toggled, or vice
                // versa
                break;
            }
        }

        return result;
    }

    @Override
    public void set(byte[] value) throws IOException {
        wrappedIter.set(value);
    }

    @Override
    public void close() throws IOException {
        wrappedIter.close();
    }
} // end inner class ExplicitStatementFilter
