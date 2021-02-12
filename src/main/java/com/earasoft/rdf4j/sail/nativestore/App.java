package com.earasoft.rdf4j.sail.nativestore;

import org.eclipse.rdf4j.common.io.NioFile;
import org.eclipse.rdf4j.sail.nativerdf.btree.BTree;

import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        try(BTree a = new BTree(new File("btree1"), "prefixtree", 2048, 4);){
            a.insert("hello".getBytes());
        }
        System.out.println( "Hello World!" );
    }
}
