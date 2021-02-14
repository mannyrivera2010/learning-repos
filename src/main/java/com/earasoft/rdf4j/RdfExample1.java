package com.earasoft.rdf4j;

import com.earasoft.rdf4j.mapdb.MapDbTesting;
import com.earasoft.rdf4j.sail.nativerockrdf.NativeStore;
import com.earasoft.rdf4j.utils.HUtils;
import com.earasoft.rdf4j.utils.ModelUtils;
import com.earasoft.rdf4j.utils.TimerSpanSingleton;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import org.eclipse.rdf4j.rio.ntriples.NTriplesUtil;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import java.util.zip.CRC32;

import org.rocksdb.*;

public class RdfExample1 {

    static {
        RocksDB.loadLibrary();
    }

    /**
     * The checksum to use for calculating data hashes.
     */
    private static final CRC32 crc32 = new CRC32();

    public enum BackendStore{
        NATIVE_STORE,
        MAPDB_STORE
    }

    public static final String FILE_DB = "file.db";
    private static String g = "http://testgraph.com/";
    private static String g1 = "http://testgraph.com/1";
    private static String g2 = "http://testgraph.com/2";
    private static String indexes = "spoc,posc,cosp"; // system
    // spoc,posc,cspo,opsc // cache

    // spoc - subject predicate object context

    private static String data_directory = "data2";

    public static ValueFactory vf = SimpleValueFactory.getInstance();

    public static long[] loadFile(Repository repository, String file, String graphContext, RDFFormat rdfxml) throws IOException {
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
            conn.add(fis, "", rdfxml, graphIri);

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


    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();

        int[] settings = {0, 0, 1};

        int times = 1;

        for(int i = 1; i<=times; i++) {
            System.out.println(i);

            String pathname = "temp_data/Thesaurus.owl";
            pathname = "example_data/agro.owl";

            if(settings[0] == 1){
                MapDbTesting.mapDbTesting(start, true, false, "file.db", pathname, RDFFormat.RDFXML);
            }

            if(settings[1] == 1){
                // [1, 31301, 206416]
                // [0, 45859, 330022]
                // [0, 30292, 209857]
                // [29, 31511, 167648]
                // [40, 45257, 312164] 1
                // [36, 42486, 266519] 2
                // [34, 43440, 249619] 3

                rdfj4Testing(start, true, false, false, false, pathname);
            }

            if(settings[2] == 1){
                long rockdbTesting = System.currentTimeMillis();
                // rockdbTesting	end	67320
                // rockdbTesting	end	223412 , 4 writes
                // 148542

                // 202408 Laptop 1st
                // 190614 Laptop 2st
                rockdbTesting(start,true, true, pathname, RDFFormat.RDFXML);

                System.out.println(String.format("rockdbTesting\tend\t%s",
                        (System.currentTimeMillis() - rockdbTesting)));

            }


        }

    }

    private static final String cfdbPath = "./rocksdb-data-cf/";


    private static int getDataHash(byte[] data) {
        synchronized (crc32) {
            crc32.update(data);
            int crc = (int) crc32.getValue();
            crc32.reset();
            return crc;
        }
    }

    private static void rockdbTesting(long start, boolean load, boolean query, String pathname, RDFFormat rdfxml){
        try (final ColumnFamilyOptions cfOpts = new ColumnFamilyOptions()
                .optimizeUniversalStyleCompaction()
             .setCompressionType(CompressionType.LZ4_COMPRESSION)

             .setCompressionOptions(new CompressionOptions().setEnabled(true))
        ) {
            // spoc,posc,cosp
            final List cfDescriptors = Arrays.asList(
                    new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts)
                    ,new ColumnFamilyDescriptor("spoc".getBytes(), cfOpts)
                    ,new ColumnFamilyDescriptor("posc".getBytes(), cfOpts)
                    ,new ColumnFamilyDescriptor("cosp".getBytes(), cfOpts)
            );

            try (final DBOptions dbOptions = new DBOptions()
                    .setCreateIfMissing(true)
                    .setCreateMissingColumnFamilies(true)
                    .setAllowConcurrentMemtableWrite(true)
                    .setIncreaseParallelism(4)
//                    .setAllowMmapWrites(true)
                    .setMaxSubcompactions(4)
            ) {

                List<ColumnFamilyHandle> cfHandles = new ArrayList<>();
                try (final RocksDB rocksDB = RocksDB.open(dbOptions, cfdbPath, cfDescriptors, cfHandles) ) {
                    ///////////////////
                    System.out.println("Connected");
                    if(load){
                        URL documentUrl = new File(pathname).toURI().toURL();
                        InputStream inputStream = documentUrl.openStream();
//
                        String baseURI = documentUrl.toString();
                        RDFFormat format = rdfxml;
                        long counter = 0;
                        try (GraphQueryResult res = QueryResults.parseGraphBackground(inputStream, baseURI, format)) {
                            while (res.hasNext()) {
                                Statement st = res.next();
                                st = vf.createStatement(st.getSubject(), st.getPredicate(), st.getObject(), vf.createIRI("http://graph1.com"));
                                // byte[] key = HUtils.ByteUtils.longToBytes(counter);
//                                byte[] key = UUID.randomUUID().toString().getBytes();


//                            byte[] value = HUtils.toKeyValues(st);

                                byte[] value = statementToByteArray(st);

                                ByteArrayDataOutput keyBuffer = ByteStreams.newDataOutput();
                                keyBuffer.writeInt(getDataHash(value));


                                try(WriteOptions writeOpt = new WriteOptions();
                                    WriteBatch batch = new WriteBatch();){

                                    boolean first = true;
                                    for(ColumnFamilyHandle h :cfHandles){

                                        if(first){
                                            batch.put(h, keyBuffer.toByteArray(), value);
                                        }else{
                                            batch.put(h, value, HUtils.EMPTY);
                                        }
                                        first = false;
                                    }
                                    rocksDB.write(writeOpt, batch);
                                }
                                // ... do something with the resulting statement here.
                                counter = counter+1L;
                                if(counter % 1000000 == 0) System.out.println(counter);
                            }
                        }
                    } // end load
                    /////////////////
                    if(query){
                        System.out.println("query");
                        //Print all [key - value]
                        RocksIterator iter = rocksDB.newIterator(cfHandles.get(0));

                        long counterRock = 1;
                        for (iter.seekToFirst(); iter.isValid(); iter.next()) {
                            System.out.println("iterator key:" + new String(iter.key()) + "\n, iter value:" + new String(iter.value()));

                            Statement st = parseStatement(iter.value(), vf);

                            System.out.println(st);
                            counterRock = counterRock + 1;
                        }
                        System.out.println("counterRock: " + counterRock);
                    }
                } catch (RocksDBException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    // NOTE frees the column family handles before freeing the db
                    for (final ColumnFamilyHandle cfHandle : cfHandles) {
                        cfHandle.close();
                    }
                }
            }

        }
    }

    private static void rdfj4Testing(long start, boolean load, boolean query, boolean clear, boolean writeTttl, final String thesaurus) throws IOException {
        File dataDir = new File(data_directory);
        Repository repo1 = new SailRepository(new NativeStore(dataDir, indexes));
        repo1.init();

        long preClear = System.currentTimeMillis();
        if(clear){
            try (RepositoryConnection conn = repo1.getConnection()) {
                conn.clear();
            }
        }

        if(load){
            // System.out.println(Arrays.toString(loadFile(repo1,"agro.owl", "http://argo.com")));
            System.out.println(Arrays.toString(loadFile(repo1, thesaurus, "http://thesaurus.com", RDFFormat.RDFXML)));
        }

        if(writeTttl){
            try (RepositoryConnection conn = repo1.getConnection()) {
                RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, new FileOutputStream(new File("all.ttl")));
                conn.prepareGraphQuery(QueryLanguage.SPARQL,
                        "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } limit 100").evaluate(writer);
            }
        }

        if(query){
            long startQuery = System.currentTimeMillis();

            try (RepositoryConnection conn = repo1.getConnection()) {
//            String queryString = "SELECT (COUNT(?s) AS ?triples) WHERE { GRAPH <http://testgraph.com/1> {?s ?p ?o} } limit 10";

                String queryString = "SELECT (COUNT(?s) AS ?triples) WHERE { ?s ?p ?o }  limit 10";



                TupleQuery tupleQuery = conn.prepareTupleQuery(queryString);
                System.out.println("prepareTupleQuery");
                try (TupleQueryResult result = tupleQuery.evaluate()) {
                    System.out.println("evaluate");
                    while (result.hasNext()) {  // iterate over the result
                        System.out.println("hasNext");
                        BindingSet bindingSet = result.next();
                        Value valueOfX = bindingSet.getValue("x");
                        Value valueOfY = bindingSet.getValue("y");
                        // do something interesting with the values here...

                        System.out.println(bindingSet); // [triples="239,596,440"^^<http://www.w3.org/2001/XMLSchema#integer>]

                    }
                }
            } // end conn

            System.out.println(String.format("rdfj4Testing\tqueryTime\t%s",
                    (System.currentTimeMillis() - startQuery)));
        }

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


    private static byte[] statementToByteArray(Statement st) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(NTriplesUtil.toNTriplesString(st.getSubject()));
        out.writeUTF(NTriplesUtil.toNTriplesString(st.getPredicate()));
        out.writeUTF(NTriplesUtil.toNTriplesString(st.getObject()));

        Resource context = st.getContext();
        out.writeUTF(context != null ? NTriplesUtil.toNTriplesString(context) : "");
        return out.toByteArray();
    }

    public static Statement parseStatement(byte[] b, ValueFactory vf) {
        ByteArrayDataInput in = ByteStreams.newDataInput(b);

        Resource subj = HUtils.readResource(in.readUTF(), vf);
        IRI pred = HUtils.readIRI(in.readUTF(), vf);
        Value value = HUtils.readValue(in.readUTF(), vf);
        Statement stmt;

        String cS = in.readUTF();
        if (cS.length() == 0) {
            stmt = vf.createStatement(subj, pred, value);
        } else {
            Resource context = HUtils.readResource(cS, vf);
            stmt = vf.createStatement(subj, pred, value, context);
        }
        return stmt;
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

        System.out.println(ModelUtils.getDiff(model, model));

        long preClear1 = System.currentTimeMillis();

        System.out.println(preClear1-preClear);
    }

    private static void queries(Repository repo1, long preClear) throws IOException {
        long postClear = System.currentTimeMillis();
        System.out.println("reset time: " + (postClear - preClear));


        System.out.println(Arrays.toString(loadFile(repo1,"agro.owl", "http://argo.com", RDFFormat.RDFXML)));

        System.out.println("init");

        List<BindingSet> results = Repositories.tupleQuery(repo1, "SELECT * WHERE { Graph ?g { ?s ?p ?o } } LIMIT 10", r -> QueryResults.asList(r));
        System.out.println("results");
        System.out.println(results);

        Model m = Repositories.graphQuery(repo1, "CONSTRUCT WHERE {?s ?p ?o} LIMIT 10", r -> QueryResults.asModel(r));

        System.out.print(m);
    }

    public void diffFed(String file1, final String file2) throws IOException {
        TimerSpanSingleton.TimerSpan timerSpan = new TimerSpanSingleton.TimerSpan("RDF4J learning");

        System.out.println("START");

        File dataDir = new File(data_directory);
        Repository repo1 = new SailRepository(new NativeStore(dataDir, indexes));
        repo1.init();

        Repository repo2 = new SailRepository(new MemoryStore());
        repo2.init();


        try (RepositoryConnection conn = repo1.getConnection()) {
            conn.clear();
        }
        System.out.println("initialized");


        try (RepositoryConnection conn = repo1.getConnection();
             FileInputStream fis = new FileInputStream(file1);
        ) {

            System.out.println("stream:" + fis);
            conn.add(fis, "", RDFFormat.RDFXML, vf.createIRI(g1));
//            InputStream stream = Main.class.getResourceAsStream("/test.ttl");
//            conn.add(stream, "", RDFFormat.TURTLE, vf.createIRI(g1));
            System.out.println(conn.getContextIDs().next().stringValue());
        }

        System.out.println("Loaded NCI_Thesaurus.owl in ");

        try (RepositoryConnection conn = repo2.getConnection();
             FileInputStream fis = new FileInputStream(file2 + ".owl");) {
            conn.add(fis, "", RDFFormat.RDFXML, vf.createIRI(g2));
//            InputStream stream = Main.class.getResourceAsStream("/test2.ttl");
//            conn.add(stream, "", RDFFormat.TURTLE, vf.createIRI(g2));
            System.out.println(conn.getContextIDs().next().stringValue());
        }

        System.out.println("Loaded NCI_Thesaurus in memory in "  );

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
        repo1.shutDown();
        repo2.shutDown();
    }


}