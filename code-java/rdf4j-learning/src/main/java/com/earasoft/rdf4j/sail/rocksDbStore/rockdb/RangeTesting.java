package com.earasoft.rdf4j.sail.rocksDbStore.rockdb;

import org.rocksdb.AbstractComparator;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ComparatorOptions;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksIterator;
import org.rocksdb.Slice;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class RangeTesting {




    public static void main(String[] args) {

        try {
            try(RockDbHolding conn = new RockDbHolding("range_testing")){

                ByteBuffer a = ByteBuffer.allocate(4);


                long start = System.currentTimeMillis();
//                RockDbState.recreateColumnFamily(conn, "default");
                for(Integer i = 0; i<=10000000; i++){

                    a.putInt(i);
                    a.flip();
                    final byte[] key = a.array();



                    byte[] v = (i.toString() + "v").getBytes();



//                    RockDbState.setKeyRockDbBytes(conn, a, (i.toString() + "v").getBytes(), "default");


                    ColumnFamilyHandle columnFamilyHandle = conn.cfHandlesMap.get("default");

                    WriteBatch writeBatch = new WriteBatch();


                    writeBatch.put(columnFamilyHandle, key, "hello".getBytes());


                    WriteOptions writeOptions = new WriteOptions();
                    conn.rocksDB.write(writeOptions, writeBatch);
                    writeBatch.close();
                    writeOptions.close();


                    if(i % 100000 == 0){
                        System.out.println(i);
                    }
//                    a.clear();
                }

                System.out.println(System.currentTimeMillis() - start);


                Iterator aa = RockDbState.createRocksRegIteratorForCFColumn(
                        conn,
                        "default",
                        iteratorEntry -> {
                            byte[] keyBytes = iteratorEntry.keyBytes;
//                    Long count = RockByteUtils.decodeCounter(iteratorEntry.valueBytes); // for debugging

                            return new String(keyBytes);
                        }
                );


//                while(a.hasNext()){
//                    System.out.println(a.next());
//                }

//                conn.rocksDB.get("984".getBytes(), "988".getBytes());

//
//                System.out.println("---Prefix---");
//
//                ByteBuffer aa = ByteBuffer.allocate(4);
//                aa.putInt(98);
//
//
//                start = System.currentTimeMillis();
//
//                ReadOptions ro = new ReadOptions();
//                ro.setPrefixSameAsStart(true);// setPrefixSameAsStart
//                ro.setTotalOrderSeek(true);
//
//
//                byte[] array = aa.array();
//                Slice slice = new Slice(array); // TODO figure out how to do inclusive
//                ro.setIterateUpperBound(slice);
//                ro.setIterateLowerBound(new Slice(array));
//
//
//                RocksIterator iterator = conn.rocksDB.newIterator(ro); // iterator.seek(prefix.getBytes())
//
//                for (iterator.seekToFirst(); iterator.isValid() ; iterator.next()) {
//                    String key = new String(iterator.key());
//
//
////                    if (!key.startsWith(prefix))
////                        break;
//                    System.out.println(String.format("%s", key));
//                }

//                https://github.com/cockroachdb/pebble/blob/master/docs/rocksdb.md#internal-keys

//                void scanRange(RocksDbKey start, RocksDbKey end, RocksDbScanCallback fn) {
//                    try (ReadOptions ro = new ReadOptions()) {
//                        ro.setTotalOrderSeek(true);
//                        RocksIterator iterator = db.newIterator(ro);
//                        for (iterator.seek(start.getRaw()); iterator.isValid(); iterator.next()) {
//                            RocksDbKey key = new RocksDbKey(iterator.key());
//                            if (key.compareTo(end) >= 0) { // past limit, quit
//                                return;
//                            }
//                            RocksDbValue val = new RocksDbValue(iterator.value());
//                            if (!fn.cb(key, val)) {
//                                // if cb returns false, we are done with this section of rows
//                                return;
//                            }
//                        }
//                    }
//                }
                System.out.println(System.currentTimeMillis() - start);


            }
            System.out.println("closing");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
