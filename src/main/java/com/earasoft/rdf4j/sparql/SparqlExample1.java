package com.earasoft.rdf4j.sparql;

import com.earasoft.rdf4j.RdfExample1;
import com.earasoft.rdf4j.sail.nativerockrdf.NativeStore;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SparqlExample1 {
    private static String data_directory = "temp_data/family";
    static String thesaurus = "temp_data/Thesaurus.owl";

    public static void main(String[] args) throws IOException {
        File dataDir = new File(data_directory);
        Repository repo1 = new SailRepository(new NativeStore(dataDir));
        repo1.init();

        long preClear = System.currentTimeMillis();

        try (RepositoryConnection conn = repo1.getConnection()) {
            System.out.println("PreClear");
            conn.clear();
            System.out.println("PostClear");

            RdfExample1.loadFile(repo1, "example_data/family.ttl", "http://earasoft.com/family", RDFFormat.TURTLE);
            RdfExample1.loadFile(repo1, "example_data/family.ttl", "http://earasoft.com/family", RDFFormat.TURTLE);
//            RdfExample1.loadFile(repo1, thesaurus, "http://thesaurus.com", RDFFormat.RDFXML);
            System.out.println("LOAD");
//            conn.clearNamespaces();
            conn.setNamespace("helo", "http://k.com");
//            conn.removeNamespace("helo");
//            conn.removeNamespace("helo1");
            System.out.println("getNamespaces: " + conn.getNamespaces().asList());
            System.out.println("getContextIDs: " + conn.getContextIDs().asList());

            // posc ?p ?o ?s
            // spoc ?s ?p ?o
            // ops
            String queryString = "SELECT (COUNT(?s) AS ?triples) WHERE { ?s ?p ?o }  limit 10";
            queryString = "PREFIX family: <http://earasoft.com/family/0.1/> " +
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                    "SELECT\n" +
                    "  ?target_name ?siblings_name\n" +
                    "WHERE {\n" +
                    "    ?person a foaf:Person;\n" +
                    "        foaf:name \"Bayley Fletcher\".\n" +
                    "\n" +
                    "    ?person foaf:name ?target_name  .\n" +
                    "\n" +
                    "    { ?person family:isSon ?family . }\n" +
                    "    UNION\n" +
                    "    { ?person family:isDaugther ?family . }\n" +
                    "\n" +
                    "     {\n" +
                    "        ?person1 family:isSon ?family .\n" +
                    "        ?person1 foaf:name ?siblings_name .\n" +
                    "     }\n" +
                    "     UNION\n" +
                    "     {\n" +
                    "       ?person1 family:isDaugther ?family .\n" +
                    "       ?person1 foaf:name ?siblings_name .\n" +
                    "     }\n" +
                    "\n" +
                    "  FILTER ( ?person != ?person1  )\n" +
                    "}";


            tupleQuery(conn, queryString, (bindingSet)->{
                System.out.println(bindingSet); // [triples="239,596,440"^^<http://www.w3.org/2001/XMLSchema#integer>]
            });
        }
    }

    private static void tupleQuery(RepositoryConnection conn, String queryString, Consumer<BindingSet> f) {
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
            }
        }
        System.out.println("tupleQuery took " + (System.currentTimeMillis() - start));

        // tupleQuery show prepared query
    }
}
