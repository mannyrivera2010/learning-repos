package com.earasoft.rdf4j.sail.nativerockrdf.rockdb;

import com.earasoft.rdf4j.sail.nativerockrdf.rockdb.RockDbIteratorEntry;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.sail.SailException;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import java.util.function.Function;

public class RockDbWrapperIterator<S> extends CloseableIteratorIteration<S, SailException> {
    final RocksIterator innerRocksIterator;
    boolean seek = false;
    final Function<RockDbIteratorEntry, S> func;

    RockDbWrapperIterator(ColumnFamilyHandle columnFamilyHandle, RocksDB rocksDB, Function<RockDbIteratorEntry, S> func) {
        this.innerRocksIterator = rocksDB.newIterator(columnFamilyHandle);
        this.func = func;
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
    public S next() {
        byte[] keyBytes = innerRocksIterator.key();
        byte[] valueBytes = innerRocksIterator.value();

        S ns = this.func.apply(new RockDbIteratorEntry(keyBytes, valueBytes));

        innerRocksIterator.next();
        return ns;
    }

    public void handleClose(){
        if (this.innerRocksIterator.isOwningHandle()) {
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
