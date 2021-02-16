package com.earasoft.rdf4j.sail.rocksDbStore.zstore;

import com.earasoft.rdf4j.sail.nativeStore.btree.RecordIterator;

import java.io.IOException;

class ImplicitStatementFilter implements RecordIterator {

    private final RecordIterator wrappedIter;

    public ImplicitStatementFilter(RecordIterator wrappedIter) {
        this.wrappedIter = wrappedIter;
    }

    @Override
    public byte[] next() throws IOException {
        byte[] result;

        while ((result = wrappedIter.next()) != null) {
            byte flags = result[TripleStore.FLAG_IDX];
            boolean explicit = (flags & TripleStore.EXPLICIT_FLAG) != 0;

            if (!explicit) {
                // Statement is implicit
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
} // end inner class ImplicitStatementFilter
