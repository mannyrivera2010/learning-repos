package com.earasoft.rdf4j.sail.nativeStore;



import com.earasoft.rdf4j.sail.nativeStore.btree.BTree;
import com.earasoft.rdf4j.sail.nativeStore.btree.RecordIterator;

import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class BtreeBench
{
    public static void main( String[] args ) throws IOException {
        System.out.println( "Hello World!" );
        System.out.println( "--------");
        File tempDir = new File("temp_data");
        try(BTree a = new BTree(tempDir, "prefixtree", 4096, 10);){
            long timePoint1 = System.currentTimeMillis();
//            a.insert("hello".getBytes());
//            a.insert("hello1".getBytes());
//            a.insert("tommy".getBytes());
//            a.insert("tommy1".getBytes());
//            a.insert("applyNow".getBytes());

            int loopNum = 9999999;
//            for(int i = 1; i<=loopNum; i++){
//                String ada= getFormat(i);
//                a.insert(ada.getBytes());
//
//
//
//            }

            long timePoint2 = System.currentTimeMillis();

            System.out.println("InsertTime:" + (timePoint2- timePoint1));

            a.sync();

            long timePoint3 = System.currentTimeMillis();

            System.out.println("SyncTime:" + (timePoint3- timePoint2));

            int count = 0;
            try(RecordIterator aa = a.iterateAll()){
                byte[] value = null;
                while ((value = aa.next()) != null) {
//                addedBTree.insert(value);
//                    System.out.println(new String(value));
                    count++;

                }

            }
            long timePoint4 = System.currentTimeMillis();

            System.out.println("TimeIterateAll:" + (timePoint4- timePoint3));

            System.out.println("IterateAllCount:" + (count));


            try(RecordIterator aa = a.iterateRange(getFormat(1002).getBytes(), getFormat(5444).getBytes())){
                byte[] value = null;
                while ((value = aa.next()) != null) {
//                addedBTree.insert(value);
                    new String(value);
//                    System.out.println(new String(value));
//                    count++;

                }

            }

            a.sync();

            long timePoint5 = System.currentTimeMillis();

            System.out.println("iterateRange\t" + (timePoint5 - timePoint4));


            a.sync();
        }

        System.out.println( "--------");
        System.out.println( "END Hello World!" );
    }

    private static String getFormat(int i) {
        return String.format("%010d", i);
    }
}
