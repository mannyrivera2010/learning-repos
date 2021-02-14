package com.earasoft.rdf4j.sail.nativerockrdf;

import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.sail.SailException;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import java.util.Iterator;

public class RocksWrapperIterator extends CloseableIteratorIteration<SimpleNamespace, SailException> {
    RocksIterator innerRocksIterator;
    boolean seek = false;
    RocksWrapperIterator(ColumnFamilyHandle columnFamilyHandle, RocksDB rocksDB) {
        this.innerRocksIterator = rocksDB.newIterator(columnFamilyHandle);

    }

    @Override
    public boolean hasNext() {
        if(!seek) {
            this.innerRocksIterator.seekToFirst();
            seek = true;
        }
        return innerRocksIterator.isValid();
    }

    @Override
    public SimpleNamespace next() {
        String key = new String(innerRocksIterator.key());
        String value = new String(innerRocksIterator.value());
        SimpleNamespace ns = new SimpleNamespace(key, value);

        innerRocksIterator.next();
        return ns;
    }

    public void handleClose(){
        if (this.innerRocksIterator.isOwningHandle()) {
            System.out.println("isOwningHandle");
            this.innerRocksIterator.close();
        }
    }

//    @Override
//    public long count() {
//        long count = 0L;
//        while (this.hasNext()) {
//            this.iter.next();
//            this.matched = false;
//            count++;
//            BackendEntryIterator.checkInterrupted();
//        }
//        return count;
//    }

}
