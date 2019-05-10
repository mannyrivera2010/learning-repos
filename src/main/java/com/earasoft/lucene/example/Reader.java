package com.earasoft.lucene.example;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.DFISimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

/**
 * Read Index
 * 
 * @author erivera
 *
 */
public class Reader {
    
    /**
     * Main
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        IndexReader reader = createReader();
        printStats(reader);
        
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity());
//      searcher.setSimilarity(new ClassicSimilarity());
//        searcher.setSimilarity(new LMDirichletSimilarity());
        
        // searchBooleanQuery
        searchBooleanQuery(searcher, "last_name", "Ridwood", false);
        searchBooleanQuery(searcher, "first_name", "Thomasina", false);
        
        // searchQueryParser
        searchQueryParser(searcher, "id", "10", false);
        searchQueryParser(searcher, "first_name", "Thomasina", false);
        
    }
    
    /**
     * Create Map From Doc
     * 
     * @param d
     * @return
     */
    private static Map<String, Object> createMapFromDoc(Document d) {
        Map<String, Object> current = new LinkedHashMap<>();
        
        for (IndexableField field : d.getFields()) {
            current.put(field.name(), field.stringValue());
        }
        
        return current;
    }

    /**
     * createSearcher
     * 
     * @return
     * @throws IOException
     */
    private static IndexReader createReader() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(Constants.INDEX_DIR));
        IndexReader reader = DirectoryReader.open(dir);
        return reader;
    }
    
    /**
     * Print Stats
     * 
     * @param reader
     */
    private static void printStats(IndexReader reader) {
        System.out.println("Stats:");
        System.out.println("MaxDoc(): " + reader.maxDoc());
        System.out.println("RefCount(): " + reader.getRefCount());
        System.out.println("hasDeletions(): " + reader.hasDeletions());
        System.out.println("");
    }
    
    /**
     * searchQueryParser
     * 
     * @param searcher
     * @throws Exception
     * @throws IOException
     */
    private static void searchQueryParser(IndexSearcher searcher, String fieldName, String fieldValue, boolean explain) throws Exception, IOException {
        System.out.println(String.format("--- searchQueryParser(fieldName:%s, fieldValue:%s) ---", fieldName, fieldValue));
        
        Analyzer searchTimeAnalyzer = new StandardAnalyzer();
        QueryParser qp = new QueryParser(fieldName, searchTimeAnalyzer);
        
        Query idQuery = qp.parse(fieldValue);
        TopDocs foundDocs = searcher.search(idQuery, 10);

        printDocs(searcher, idQuery, foundDocs, explain);
    }
    
    /**
     * searchBooleanQuery
     *  
     * @param searcher
     * @throws IOException
     */
    private static void searchBooleanQuery(IndexSearcher searcher, String fieldName, String fieldValue, boolean explain) throws IOException {
        System.out.println(String.format("--- searchBooleanQuery(fieldName:%s, fieldValue:%s) ---", fieldName, fieldValue));
        
        Analyzer searchTimeAnalyzer = new StandardAnalyzer();
        QueryBuilder queryBuilder = new QueryBuilder(searchTimeAnalyzer);
        Query query = queryBuilder.createBooleanQuery(fieldName, fieldValue);
        
        TopDocs hits = searcher.search(query, 10);
        printDocs(searcher, query, hits, explain);
    }

    /**
     * Print Docs
     * 
     * @param searcher
     * @param a
     * @param hits
     * @throws IOException
     */
    private static void printDocs(IndexSearcher searcher, Query a, TopDocs hits, boolean explain) throws IOException {
        System.out.println("Total Results :: " + hits.totalHits);
        
        for (ScoreDoc sd : hits.scoreDocs) {
            Document d = searcher.doc(sd.doc);
            
            Map<String, Object> current = createMapFromDoc(d);
            System.out.println(current);
            
            if(explain) {
                System.out.println("");
                Explanation expl = searcher.explain(a, sd.doc);
                System.out.println(expl);
            }
            
            System.out.println("--");
            
        }
        
        System.out.println("");
    }

}
