package com.earasoft.rdf4j.sparql;

import com.earasoft.rdf4j.sail.rocksDbStore.RocksDbStore;
import com.earasoft.rdf4j.utils.FileQueryReader;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SparqlFamilyExample1 {
    private static String data_directory = "temp_data/family";
    static String thesaurus = "temp_data/Thesaurus.owl";
    private static String indexes = "cosp, spoc, posc";
    // spoc,posc,cosp

    public static void main(String[] args) throws IOException {
        File dataDir = new File(data_directory);
//        Repository repo1 = new SailRepository(new RocksDbStore(dataDir));
        Repository repo1 = new SailRepository(new NativeStore(dataDir, indexes));
        repo1.init();

        long preClear = System.currentTimeMillis();

        try (RepositoryConnection conn = repo1.getConnection()) {
            System.out.println("getConnection");
//            RDFWriter writer = Rio.createWriter(RDFFormat.TRIG, new FileOutputStream(new File("all.trig")));
//            conn.prepareTupleQuery(QueryLanguage.SPARQL,
//                    "SELECT  { GRAPH ?g { ?s ?p ?o } } WHERE { GRAPH ?g { ?s ?p ?o } } limit 100").evaluate().;

//            System.out.println("PreClear");
//            conn.clear();
//            System.out.println("PostClear");
            int familyLoopLimit = 1000;
            double[] familyTimings = new double[familyLoopLimit];
            for(int i = 0; i< familyLoopLimit; i++){

                long familyTimeStart = System.currentTimeMillis();
//               RdfExample1.loadFile(repo1, "example_data/family.ttl","http://earasoft.com/family/" + familyTimeStart+i, RDFFormat.TURTLE);

                RdfExample1.loadFile(repo1, thesaurus, "http://thesaurus.com/" + familyTimeStart+i, RDFFormat.RDFXML);

                long familyTimeEnd = System.currentTimeMillis() - familyTimeStart;
                System.out.print(familyTimeEnd);
                familyTimings[i] = familyTimeEnd;

            }
            // 320753, 289907, 274167, 287753, 336660, 310981,320232

            System.out.println("timings: " + Arrays.toString(familyTimings));
            System.out.println("timings sd: "  + calculateSD(familyTimings));
//
            System.out.println("LOAD");
//            conn.clearNamespaces();
            conn.setNamespace("helo", "http://k.com");
//            conn.removeNamespace("helo");
//            conn.removeNamespace("helo1");
//            System.out.println("getNamespaces: " + conn.getNamespaces().asList());
//            System.out.println("getContextIDs: " + conn.getContextIDs().asList());

            // posc ?p ?o ?s
            // spoc ?s ?p ?o
            // ops

            int upper = 0;
            Map<String, String> queries = FileQueryReader.loadQueries();
            Map<String, double[]> queriesTimings = new LinkedHashMap<>();

            for(int ia = 0; ia < upper; ia++){
                for(Map.Entry<String, String> queryEntry : queries.entrySet()){
                    String s = queryEntry.getKey().toString();
                    System.out.println("-----" + s + "-----");

                    final int[] counterA = {0};
                    long queryTiming = tupleQuery(conn, queryEntry.getValue().toString(), (bindingSet)->{
                        // System.out.println(bindingSet); // [triples="239,596,440"^^<http://www.w3.org/2001/XMLSchema#integer>]
                        counterA[0] = counterA[0] + 1;

                    });
                    // get timing array, allocate if not initized
                    double[] timingArray = queriesTimings.get(s);
                    if(timingArray == null){
                        timingArray = new double[upper*2]; // double array size to store timings and the iteration number
                    }
                    timingArray[ia] = queryTiming;
                    timingArray[ia + upper]=counterA[0];
                    queriesTimings.put(s, timingArray);
                }
            }
            // print out timings for queries
            for(Map.Entry<String, double[]> queryEntry : queriesTimings.entrySet()) {
                String s = queryEntry.getKey();
                double[] value = queryEntry.getValue();
                double[] slide = Arrays.copyOfRange(value, 0, upper);

                System.out.println(s + "\t" + calculateSD(slide) + "\n\t" + Arrays.toString(value));
            }

//            conn.clear();
        }
    }

    public static double calculateSD(double numArray[])
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;

        for(double num : numArray) {
            sum += num;
        }

        double mean = sum/length;

        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/length);
    }

    private static long tupleQuery(RepositoryConnection conn, String queryString, Consumer<BindingSet> f) {
        long start = System.currentTimeMillis();
        TupleQuery tupleQuery = conn.prepareTupleQuery(queryString);
        System.out.println("prepareTupleQuery");
        try (TupleQueryResult result = tupleQuery.evaluate()) {

            System.out.println("evaluate");
            while (result.hasNext()) {  // iterate over the result
//                    System.out.println("hasNext");
                BindingSet bindingSet = result.next();
//                Value valueOfX = bindingSet.getValue("x");
//                Value valueOfY = bindingSet.getValue("y");
                // do something interesting with the values here...
                f.accept(bindingSet);
            } // end hasNext()

        }

//        System.out.println(tupleQuery);
        return System.currentTimeMillis() - start;
    }
}
