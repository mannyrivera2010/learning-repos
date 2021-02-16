package com.earasoft.rdf4j.mapdb;

import com.earasoft.rdf4j.sparql.RdfExample1;
import com.earasoft.rdf4j.utils.ByteUtils;
import com.earasoft.rdf4j.utils.HUtils;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.mapdb.BTreeMap;
import org.mapdb.DB;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

public class MapDbTesting {

    public static void mapDbTesting(long start, boolean load, boolean query, String mapDbFileName, String pathname, RDFFormat rdfxml) throws IOException {
        if(load){
            loadBtreeMap(mapDbFileName, pathname, rdfxml);
        }

        if(query){
            MapDbStore MapDbStore = new MapDbStore(mapDbFileName);
            DB db = MapDbStore.db;
            BTreeMap<Long, byte[]> map = MapDbStore.map;
            BTreeMap<byte[], byte[]> spo_map = MapDbStore.spo_map;

            System.out.println("createOrOpen: \t" + (System.currentTimeMillis() - start));

//        System.out.println("map.size(): " + map.size());

//        ConcurrentNavigableMap<byte[], byte[]> subMap = map.prefixSubMap(
//                ByteUtils.longToBytes(10), true
//        );

            ConcurrentNavigableMap<Long, byte[]> subMap = map.subMap(
                    1l, true,
                    12l, true
            );

            System.out.println("subMap.size(): " + subMap.size());

            for(Long l : subMap.keySet()){
//            System.out.println(Arrays.toString(k));
                System.out.println(l);
            }

            for (Iterator<Map.Entry<Long, byte[]>> it = map.entryIterator(); it.hasNext(); ) {
                Map.Entry<Long, byte[]> k = it.next();

//            System.out.println(new String(k.getValue(), "UTF-8"));

                Statement st = HUtils.parseStatement(k.getValue(), RdfExample1.vf);

                byte[] bytesIndex = spo_map.get(HUtils.toKeyValues(st));
                if(bytesIndex!=null){
                    ByteUtils.bytesToLong(bytesIndex);
                }else{
                    System.out.println("missing index spo for" + st);
                }

//            System.out.println(st);
            }

            System.out.println("finished scan: \t" + (System.currentTimeMillis()- start));
            db.close();
        }

    }

    public static void loadBtreeMap(String mapDbFileName, String pathname, RDFFormat rdfxml) throws IOException {
        long start = System.currentTimeMillis();

        MapDbStore MapDbStore = new MapDbStore(mapDbFileName);
        DB db = MapDbStore.db;
        BTreeMap<Long, byte[]> map = MapDbStore.map;
        BTreeMap<byte[], byte[]> spo_map = MapDbStore.spo_map;

        // spoc,posc,cosp


        Long counter = 1L;

        URL documentUrl = new File(pathname).toURI().toURL();
        InputStream inputStream = documentUrl.openStream();
//
        String baseURI = documentUrl.toString();
        RDFFormat format = rdfxml;
        try (GraphQueryResult res = QueryResults.parseGraphBackground(inputStream, baseURI, format)) {
            while (res.hasNext()) {
                Statement st = res.next();

//                System.out.println(st.toString());

//                byte[] key = ByteUtils.longToBytes(counter);

                byte[] value = HUtils.toKeyValues(st);


                spo_map.put(value, ByteUtils.longToBytes(counter)); //
//                System.out.println(Arrays.toString(value));

                map.put(counter, value);

                // ... do something with the resulting statement here.
                counter = counter+1L;

//                if(counter % 100000 == 0) db.commit();

//                System.out.println(counter);
            }
        }
        catch (RDF4JException e) {
            System.err.print(e);
        }
        finally {
            inputStream.close();
        }

        System.out.println("inputStream.close(): \t" + (System.currentTimeMillis()-start));
        db.close();
        System.out.println("finished: \t" + (System.currentTimeMillis()-start));
    }
}
