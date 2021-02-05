package com.earasoft.rdf4j;

import com.earasoft.rdf4j.utils.TimerSpanSingleton;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class RdfExample1 {
    private static String g = "http://testgraph.com/";
    private static String g1 = "http://testgraph.com/1";
    private static String g2 = "http://testgraph.com/2";
    private static String indexes = "spoc,posc,cosp"; // system
    // spoc,posc,cspo,opsc // cache


    private static String data_directory = "data2";

    private static ValueFactory vf = SimpleValueFactory.getInstance();


    public static  void write30Times(){
        try(TimerSpanSingleton.TimerSpan timerSpan = new TimerSpanSingleton.TimerSpan("RDF4J learning");){
            File dataDir = new File(data_directory);
            Repository repo1 = new SailRepository(new NativeStore(dataDir, indexes));
            repo1.init();

            timerSpan.event("repo1.init()");

            long preClear = System.currentTimeMillis();
            try (RepositoryConnection conn = repo1.getConnection()) {
                conn.clear();
            }
            long postClear = System.currentTimeMillis();
            System.out.println("reset time: " + (postClear - preClear));

            timerSpan.event("repo1.getConnection().clear()");

            for(int i = 1; i <= 30; i++){
                timerSpan.event("start loop: " + i);

                long preConnection = System.currentTimeMillis();
                long preCommit = 0;
                long postCommit = 0;

                try (RepositoryConnection conn = repo1.getConnection();
                     FileInputStream fis = new FileInputStream("Thesaurus.owl");
                ) {
                    conn.begin();
                    timerSpan.event("repo1.getConnection()");
                    conn.add(fis, "", RDFFormat.RDFXML, vf.createIRI(g+i));
                    timerSpan.event("conn.add(FileInputStream)");

                    preCommit = System.currentTimeMillis();
                    conn.commit();
                    postCommit = System.currentTimeMillis();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long postConnection = System.currentTimeMillis();

                System.out.println("adding to nativeStore\t" + i + "\t" + (postConnection - preConnection) + "\t" + (postConnection - postCommit) + "\t" + (postCommit - preCommit)) ;

                timerSpan.event("end loop (time for close): " + i);
            }

            // end try
        }
    }

    public static void main(String[] args) throws IOException {
        File dataDir = new File(data_directory);
        Repository repo1 = new SailRepository(new NativeStore(dataDir, indexes));
        repo1.init();

        System.out.println("init");

        List<BindingSet> results = Repositories.tupleQuery(repo1, "SELECT * WHERE { Graph ?g { ?s ?p ?o } } LIMIT 10", r -> QueryResults.asList(r));
        System.out.println("results");
        System.out.println(results);

        Model m = Repositories.graphQuery(repo1, "CONSTRUCT WHERE {?s ?p ?o} LIMIT 10", r -> QueryResults.asModel(r));

        System.out.print(m);


//        Map<String, Repository> repos = repoManager.getAllRepositories();


//        try (RepositoryConnection conn = repo1.getConnection()) {
//            RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, new FileOutputStream(new File("all.ttl")));
//            conn.prepareGraphQuery(QueryLanguage.SPARQL,
//                    "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } limit 100").evaluate(writer);
//        }

//        try (RepositoryConnection conn = repo1.getConnection()) {
////            String queryString = "SELECT (COUNT(?s) AS ?triples) WHERE { GRAPH <http://testgraph.com/1> {?s ?p ?o} } limit 10";
//
//            String queryString = "SELECT (COUNT(?s) AS ?triples) WHERE { ?s ?p ?o }  limit 10";
//            TupleQuery tupleQuery = conn.prepareTupleQuery(queryString);
//            System.out.println("prepareTupleQuery");
//            try (TupleQueryResult result = tupleQuery.evaluate()) {
//                System.out.println("evaluate");
//                while (result.hasNext()) {  // iterate over the result
//                    System.out.println("hasNext");
//                    BindingSet bindingSet = result.next();
//                    Value valueOfX = bindingSet.getValue("x");
//                    Value valueOfY = bindingSet.getValue("y");
//                    // do something interesting with the values here...
//
//                    System.out.println(bindingSet); // [triples="239,596,440"^^<http://www.w3.org/2001/XMLSchema#integer>]
//
//                }
//            }
//
//
//        } // end conn







    }

//    public void diffFed() throws IOException {
//        TimerSpanSingleton.TimerSpan timerSpan = new TimerSpanSingleton.TimerSpan("RDF4J learning");
//        StopWatch watch = new StopWatch();
//
//        System.out.println("START");
//
//        //
//
//        File dataDir = new File(data_directory);
//        Repository repo1 = new SailRepository(new NativeStore(dataDir, indexes));
//        repo1.init();
//
//        Repository repo2 = new SailRepository(new MemoryStore());
//        repo2.init();
//
//
//        try (RepositoryConnection conn = repo1.getConnection()) {
//            conn.clear();
//        }
//        System.out.println("initialized");
//
//        watch.start();
//
//        try (RepositoryConnection conn = repo1.getConnection();
//             FileInputStream fis = new FileInputStream("NCI_Thesaurus.owl");
//        ) {
//
//            System.out.println("stream:" + fis);
//            conn.add(fis, "", RDFFormat.RDFXML, vf.createIRI(g1));
////            InputStream stream = Main.class.getResourceAsStream("/test.ttl");
////            conn.add(stream, "", RDFFormat.TURTLE, vf.createIRI(g1));
//            System.out.println(conn.getContextIDs().next().stringValue());
//        }
//        watch.stop();
//        System.out.println("Loaded NCI_Thesaurus.owl in " + watch.getTime());
//        watch.reset();
//
//        watch.start();
//        try (RepositoryConnection conn = repo2.getConnection();
//             FileInputStream fis = new FileInputStream("NCI_Thesaurus.owl");) {
//            conn.add(fis, "", RDFFormat.RDFXML, vf.createIRI(g2));
////            InputStream stream = Main.class.getResourceAsStream("/test2.ttl");
////            conn.add(stream, "", RDFFormat.TURTLE, vf.createIRI(g2));
//            System.out.println(conn.getContextIDs().next().stringValue());
//        }
//        watch.stop();
//        System.out.println("Loaded NCI_Thesaurus in memory in " + watch.getTime());
//        watch.reset();
//        Federation federation = new Federation();
//        federation.addMember(repo1);
//        federation.addMember(repo2);
//        federation.init();
//        try (SailConnection conn = federation.getConnection()) {
//            System.out.println("Set up SailConnection");
//            String query = getQuery(g1, g2);
//            System.out.println(query);
//            ParsedTupleQuery tupleQueryOp = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, query, null);
//            TupleQuery tupleQuery = new SailConnectionTupleQuery(tupleQueryOp, conn);
//            watch.start();
//            TupleQueryResult result = tupleQuery.evaluate();
//            watch.stop();
//            System.out.println("Results in " + watch.getTime());
//            watch.reset();
//            Model model = new LinkedHashModel();
//            while (result.hasNext()) {
//                BindingSet bindingSet = result.next();
//                model.add((Resource) bindingSet.getBinding("s").getValue(),
//                        (IRI) bindingSet.getBinding("p").getValue(),
//                        bindingSet.getBinding("o").getValue());
//            }
//            System.out.println("Total statements in g1 not in g2 " + model.size());
//            String query2 = getQuery(g2, g1);
//            System.out.println(query);
//            ParsedTupleQuery tupleQueryOp2 = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, query2, null);
//            TupleQuery tupleQuery2 = new SailConnectionTupleQuery(tupleQueryOp2, conn);
//            watch.start();
//            TupleQueryResult result2 = tupleQuery2.evaluate();
//            watch.stop();
//            System.out.println("Results in " + watch.getTime());
//            watch.reset();
//            Model model2 = new LinkedHashModel();
//            while (result2.hasNext()) {
//                BindingSet bindingSet = result2.next();
//                model2.add((Resource) bindingSet.getBinding("s").getValue(),
//                        (IRI) bindingSet.getBinding("p").getValue(),
//                        bindingSet.getBinding("o").getValue());
//            }
//            System.out.println("Total statements in g2 not in g1 " + model2.size());
//        }
//        repo1.shutDown();
//        repo2.shutDown();
//    }


    private static String getQuery(String g1, String g2) {
        StringBuilder sb = new StringBuilder();
        sb.append("select ?s ?p ?o WHERE { VALUES ?g1 {<");
        sb.append(g1);
        sb.append(">} ");
        sb.append(" VALUES ?g2 {<");
        sb.append(g2);
        sb.append(">} GRAPH ?g1 { ?s ?p ?o .}");
        sb.append(" FILTER NOT EXISTS { GRAPH ?g2");
        sb.append(" { ?s ?p ?o } } }");
        return sb.toString();
    }
}