package com.earasoft.rdf4j;

import com.earasoft.rdf4j.sail.nativerockrdf.NativeStore;
import com.earasoft.rdf4j.utils.HUtils;
import com.earasoft.rdf4j.utils.ModelUtils;
import com.earasoft.rdf4j.utils.TimerSpanSingleton;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
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

import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.jetbrains.annotations.NotNull;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

import org.rocksdb.*;

public class RdfExample1 {

    static {
        RocksDB.loadLibrary();
    }

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


    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();

        int[] settings = {0, 0, 1};

        if(settings[0] == 1){
            mapDbTesting(start, true, false, "file.db", "temp_data/Thesaurus.owl", RDFFormat.RDFXML);
        }

        if(settings[1] == 1){
            // [1, 31301, 206416]
            // [0, 45859, 330022]
            // [0, 30292, 209857]
            // [29, 31511, 167648]

            rdfj4Testing(start, true, false, false, false, "temp_data/Thesaurus.owl");
        }

        if(settings[2] == 1){
            long rockdbTesting = System.currentTimeMillis();
            // rockdbTesting	end	67320
            // rockdbTesting	end	223412 , 4 writes
            // 148542
            rockdbTesting("temp_data/Thesaurus.owl", RDFFormat.RDFXML);

            System.out.println(String.format("rockdbTesting\tend\t%s",
                    (System.currentTimeMillis() - rockdbTesting)));

        }

    }

    private static final String cfdbPath = "./rocksdb-data-cf/";


    private static void rockdbTesting(String pathname, RDFFormat rdfxml){
        System.out.println("testDefaultColumnFamily begin...");
        //If the file does not exist, create the file first

        try (final ColumnFamilyOptions cfOpts = new ColumnFamilyOptions()
                .optimizeUniversalStyleCompaction()

//                .optimizeLevelStyleCompaction()
        ) {
            // list of column family descriptors, first entry must always be default column family

            // spoc,posc,cosp
            final List cfDescriptors = Arrays.asList(
                    new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts),
                    new ColumnFamilyDescriptor("spoc".getBytes(), cfOpts),
                    new ColumnFamilyDescriptor("posc".getBytes(), cfOpts),
                    new ColumnFamilyDescriptor("cosp".getBytes(), cfOpts)
            );


            try (final DBOptions dbOptions = new DBOptions()
                    .setCreateIfMissing(true)
                    .setCreateMissingColumnFamilies(true)
                    .setAllowConcurrentMemtableWrite(true)
                    .setMaxSubcompactions(6);
            ) {

                List<ColumnFamilyHandle> cfHandles = new ArrayList<>();
                try (final RocksDB rocksDB = RocksDB.open(dbOptions, cfdbPath, cfDescriptors, cfHandles) ) {


                    //Simple key value
//                byte[] key = "Hello".getBytes();
//                rocksDB.put(key, "World".getBytes());

                    System.out.println(cfHandles);

                    ///////////////////
                    URL documentUrl = new File(pathname).toURI().toURL();
                    InputStream inputStream = documentUrl.openStream();
//
                    String baseURI = documentUrl.toString();
                    RDFFormat format = rdfxml;
                    long counter = 0;
                    try (GraphQueryResult res = QueryResults.parseGraphBackground(inputStream, baseURI, format)) {
                        while (res.hasNext()) {
                            Statement st = res.next();

//                System.out.println(st.toString());

//


                            byte[] key = HUtils.ByteUtils.longToBytes(counter);
//                            byte[] key = UUID.randomUUID().toString().getBytes();
                            byte[] value = HUtils.toKeyValues(st);

                            try(WriteOptions writeOpt = new WriteOptions();
                                WriteBatch batch = new WriteBatch();){

                                // note having huge batch makes starting up longer

//                        for (int i = 0; i < keys.size(); ++i) {
//                            batch.put(keys.get(i), values.get(i));
//                        }

                                boolean first = true;
                                for(ColumnFamilyHandle h :cfHandles){

                                    if(first){
                                        batch.put(h, key, value);
                                    }else{
                                        batch.put(h, value, HUtils.EMPTY );
                                    }
                                    first = false;
                                }



                                rocksDB.write(writeOpt, batch);
                            }

                            // ... do something with the resulting statement here.
                            counter = counter+1L;

                            if(counter % 100000 == 0) System.out.println(counter);


                        }
                    }



                    /////////////////

                    //Print all [key - value]
//                RocksIterator iter = rocksDB.newIterator();
//                for (iter.seekToFirst(); iter.isValid(); iter.next()) {
//                    System.out.println("iterator key:" + new String(iter.key()) + ", iter value:" + new String(iter.value()));
//                }


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
            //
            //        System.out.println(Arrays.toString(loadFile(repo1,"agro.owl", "http://argo.com")));

            System.out.println(Arrays.toString(loadFile(repo1, thesaurus, "http://thesaurus.com")));
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


    public static class MapDbStore{
        final DB db;
        final BTreeMap<Long, byte[]> map;
        final BTreeMap<byte[], byte[]> spo_map;

        public MapDbStore(String mapDbFileName){
            this.db = DBMaker.fileDB(mapDbFileName)
                    .fileMmapEnable()
                    .make();
            this.map = getLongBTreeMap(db);
            this.spo_map = getSpocMap(db);
        }

        @NotNull
        private static BTreeMap<byte[], byte[]> getSpocMap(DB db) {
            return db
                    .treeMap("spoc_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY)
                    .createOrOpen();
        }

        @NotNull
        private static BTreeMap<Long, byte[]> getLongBTreeMap(DB db) {
            return db
                    .treeMap("map", Serializer.LONG, (Serializer.BYTE_ARRAY))
                    .valuesOutsideNodesEnable()
                    .createOrOpen();
        }
    }


    private static void mapDbTesting(long start, boolean load, boolean query, String mapDbFileName, String pathname, RDFFormat rdfxml) throws IOException {
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

                Statement st = HUtils.parseStatement(k.getValue(), vf);

                byte[] bytesIndex = spo_map.get(HUtils.toKeyValues(st));
                if(bytesIndex!=null){
                    HUtils.ByteUtils.bytesToLong(bytesIndex);
                }else{
                    System.out.println("missing index spo for" + st);
                }

//            System.out.println(st);
            }

            System.out.println("finished scan: \t" + (System.currentTimeMillis()- start));
            db.close();
        }

    }

    private static void loadBtreeMap(String mapDbFileName, String pathname, RDFFormat rdfxml) throws IOException {
        long start = System.currentTimeMillis();

        MapDbStore MapDbStore = new MapDbStore(mapDbFileName);
        DB db = MapDbStore.db;
        BTreeMap<Long, byte[]> map = MapDbStore.map;
        BTreeMap<byte[], byte[]> spo_map = MapDbStore.spo_map;

        // spoc,posc,cosp

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
//        out.write(someBytes);
        out.writeInt(5);
        out.toByteArray();

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

                spo_map.put(value, HUtils.ByteUtils.longToBytes(counter)); //
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


        System.out.println(Arrays.toString(loadFile(repo1,"agro.owl", "http://argo.com")));

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