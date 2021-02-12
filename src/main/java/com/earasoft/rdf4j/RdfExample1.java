package com.earasoft.rdf4j;

import com.earasoft.rdf4j.utils.TimerSpanSingleton;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.LinkedHashModelFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

public class RdfExample1 {

    private static String g = "http://testgraph.com/";
    private static String g1 = "http://testgraph.com/1";
    private static String g2 = "http://testgraph.com/2";
    private static String indexes = "spoc,posc,cosp"; // system
    // spoc,posc,cspo,opsc // cache

    // spoc - subject predicate object context



    private static String data_directory = "data2";

    private static ValueFactory vf = SimpleValueFactory.getInstance();


    public static long[] loadFile(Repository repository, String file, String graphContext) throws IOException {
        long start = System.currentTimeMillis();
        long preParse = 0;
        long preCommit = 0;
        long postCommit = 0;

        try (RepositoryConnection conn = repository.getConnection();
             FileInputStream fis = new FileInputStream(file);
        ) {
            preParse = System.currentTimeMillis();
            conn.begin();
            IRI graphIri = vf.createIRI(graphContext);
            conn.add(fis, "", RDFFormat.RDFXML, graphIri);

            preCommit = System.currentTimeMillis();
            conn.commit();
            postCommit = System.currentTimeMillis();
        }
        return new long[]{
                preParse - start,
                preCommit - start,
                postCommit - start
        };
    }

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
                     FileInputStream fis = new FileInputStream("agro.owl");
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

    public static Difference getDiff(Model original, Model changed) {
        Model additions =  new LinkedHashModelFactory().createEmptyModel();
        Model deletions = new LinkedHashModelFactory().createEmptyModel();

        original.forEach(statement -> {
            if (!changed.contains(statement.getSubject(), statement.getPredicate(), statement.getObject())) {
                deletions.add(statement);
            }
        });

        changed.forEach(statement -> {
            if(!original.contains(statement.getSubject(), statement.getPredicate(), statement.getObject())) {
                additions.add(statement);
            }
        });

        return new Difference.Builder()
                .additions(additions)
                .deletions(deletions)
                .build();
    }



    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
//        loadbtreemap();

        mapdb(start);

//        File dataDir = new File(data_directory);
//        Repository repo1 = new SailRepository(new NativeStore(dataDir, indexes));
//        repo1.init();
//
//        long preClear = System.currentTimeMillis();
//        try (RepositoryConnection conn = repo1.getConnection()) {
//            conn.clear();
//        }
//
////        System.out.println(Arrays.toString(loadFile(repo1,"agro.owl", "http://argo.com")));
//
//        // [0, 45859, 330022]
//        System.out.println(Arrays.toString(loadFile(repo1,"Thesaurus.owl", "http://thesaurus.com")));


//
//        diff();


//        queries(repo1, preClear);


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

    private static void mapdb(long start) {
        DB db = DBMaker.fileDB("file.db")
                .fileMmapEnable()
                .make();

        BTreeMap<Long, byte[]> map = MapDbFactory.getLongBTreeMap(db);
        BTreeMap<byte[], byte[]> spo_map = MapDbFactory.getSpocMap(db);

        System.out.println("createOrOpen: \t" + (System.currentTimeMillis()- start));

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

            Statement st = HUtils.parseStatement(k.getValue(), vf);

            HUtils.ByteUtils.bytesToLong(spo_map.get(HUtils.toKeyValues(st)));
//            System.out.println(st);


        }


        System.out.println("finished scan: \t" + (System.currentTimeMillis()- start));

        db.close();
    }

    private static void loadbtreemap() throws IOException {
        long start = System.currentTimeMillis();

        DB db = DBMaker.fileDB("file.db")
                .fileMmapEnable()
                .make();
        BTreeMap<Long, byte[]> map = MapDbFactory.getLongBTreeMap(db);

        BTreeMap<byte[], byte[]> spo_map = MapDbFactory.getSpocMap(db);

//        spoc,posc,cosp


// kv[0] = new KeyValue(concat(SPO_PREFIX, false, sKey, pKey, oKey), CF_NAME, cq, timestamp, type, EMPTY);
//        kv[1] = new KeyValue(concat(POS_PREFIX, false, pKey, oKey, sKey), CF_NAME, cq, timestamp, type, EMPTY);
//        kv[2] = new KeyValue(concat(OSP_PREFIX, false, oKey, sKey, pKey), CF_NAME, cq, timestamp, type, EMPTY);
//        if (context != null) {
//            byte[] cKey = hashKey(cb);
//            kv[3] = new KeyValue(concat(CSPO_PREFIX, false, cKey, sKey, pKey, oKey), CF_NAME, cq, timestamp, type, EMPTY);
//            kv[4] = new KeyValue(concat(CPOS_PREFIX, false, cKey, pKey, oKey, sKey), CF_NAME, cq, timestamp, type, EMPTY);
//            kv[5] = new KeyValue(concat(COSP_PREFIX, false, cKey, oKey, sKey, pKey), CF_NAME, cq, timestamp, type, EMPTY);
//        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
//        out.write(someBytes);
        out.writeInt(5);
// ...
         out.toByteArray();

        Long counter = 1L;

        URL documentUrl = new File("Thesaurus.owl").toURI().toURL();
        InputStream inputStream = documentUrl.openStream();
//
        String baseURI = documentUrl.toString();
        RDFFormat format = RDFFormat.RDFXML;
        try (GraphQueryResult res = QueryResults.parseGraphBackground(inputStream, baseURI, format)) {
            while (res.hasNext()) {
                Statement st = res.next();

//                System.out.println(st.toString());

//                byte[] key = ByteUtils.longToBytes(counter);

                byte[] value = HUtils.toKeyValues(st);

                spo_map.put(value, HUtils.ByteUtils.longToBytes(counter)); //
//                System.out.println(Arrays.toString(value));

                map.put(counter, value);

                // ... do something with the resulting statement here.
                counter = counter+1L;
                System.out.println(counter);
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

    private static void diff() throws IOException {
        URL documentUrl = new File("Thesaurus.owl").toURI().toURL();
        InputStream inputStream = documentUrl.openStream();

        RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);

        Model model = new LinkedHashModel();
        rdfParser.setRDFHandler(new StatementCollector(model));

        try {
            rdfParser.parse(inputStream, documentUrl.toString());
        }
        catch (IOException e) {
            // handle IO problems (e.g. the file could not be read)
        }
        catch (RDFParseException e) {
            // handle unrecoverable parse error
        }
        catch (RDFHandlerException e) {
            // handle a problem encountered by the RDFHandler
        }
        finally {
            inputStream.close();
        }

        long preClear = System.currentTimeMillis();
        System.out.println("st - " + model.size());

        System.out.println(getDiff(model, model));

        long preClear1 = System.currentTimeMillis();

        System.out.println(preClear1-preClear);
    }

    private static void queries(Repository repo1, long preClear) throws IOException {
        long postClear = System.currentTimeMillis();
        System.out.println("reset time: " + (postClear - preClear));


        System.out.println(Arrays.toString(loadFile(repo1,"agro.owl", "http://argo.com")));

        System.out.println("init");

        List<BindingSet> results = Repositories.tupleQuery(repo1, "SELECT * WHERE { Graph ?g { ?s ?p ?o } } LIMIT 10", r -> QueryResults.asList(r));
        System.out.println("results");
        System.out.println(results);

        Model m = Repositories.graphQuery(repo1, "CONSTRUCT WHERE {?s ?p ?o} LIMIT 10", r -> QueryResults.asModel(r));

        System.out.print(m);
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


}