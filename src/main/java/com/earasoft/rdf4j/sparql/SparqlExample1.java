package com.earasoft.rdf4j.sparql;

import com.earasoft.rdf4j.RdfExample1;
import com.earasoft.rdf4j.sail.nativerockrdf.NativeStore;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.IOException;

public class SparqlExample1 {
    private static String data_directory = "temp_data/family";


    public static void main(String[] args) throws IOException {
        File dataDir = new File(data_directory);
        Repository repo1 = new SailRepository(new NativeStore(dataDir));
        repo1.init();

        long preClear = System.currentTimeMillis();

        try (RepositoryConnection conn = repo1.getConnection()) {
            conn.clear();


            RdfExample1.loadFile(repo1, "example_data/family.ttl", "http://earasoft.com/family", RDFFormat.TURTLE);


            System.out.println("getNamespaces: " + conn.getNamespaces());
            System.out.println("getContextIDs: " + conn.getContextIDs());

            String queryString = "SELECT (COUNT(?s) AS ?triples) WHERE { ?s ?p ?o }  limit 10";
            queryString = "PREFIX family: <http://earasoft.com/family/0.1/> " +
                    "SELECT ?aname WHERE { ?aname a family:Family . }";


            TupleQuery tupleQuery = conn.prepareTupleQuery(queryString);
            System.out.println("prepareTupleQuery");
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                System.out.println("evaluate");
                while (result.hasNext()) {  // iterate over the result
//                    System.out.println("hasNext");
                    BindingSet bindingSet = result.next();
                    Value valueOfX = bindingSet.getValue("x");
                    Value valueOfY = bindingSet.getValue("y");
                    // do something interesting with the values here...

                    System.out.println(bindingSet); // [triples="239,596,440"^^<http://www.w3.org/2001/XMLSchema#integer>]

                }
            }
        }

    }
}
