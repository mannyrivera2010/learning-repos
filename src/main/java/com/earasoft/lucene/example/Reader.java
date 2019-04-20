package com.earasoft.lucene.example;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
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
		IndexSearcher searcher = createSearcher();

		System.out.println("--- Search by createBooleanQuery(ID) ---");
		QueryBuilder builder = new QueryBuilder(new StandardAnalyzer());
		Query a = builder.createBooleanQuery("is_cool", "true");

		TopDocs hits = searcher.search(a, 10);

		for (ScoreDoc sd : hits.scoreDocs) {
			Document d = searcher.doc(sd.doc);
			
			Map<String, Object> current = createMapFromDoc(d);
			System.out.println(current);
			System.out.println("");
	        Explanation expl = searcher.explain(a, sd.doc);
	        System.out.println(expl);
	        System.out.println("--");
	        
		}


	   
		// Search by ID
		System.out.println("--- Search by ID ---");
		TopDocs foundDocs = searchById(1, searcher);

		System.out.println("Total Results :: " + foundDocs.totalHits);

		for (ScoreDoc sd : foundDocs.scoreDocs) {
			Document d = searcher.doc(sd.doc);
			
			Map<String, Object> current = createMapFromDoc(d);
			System.out.println(current);
		}

		// Search by firstName
		System.out.println("--- Search by firstName ---");
		TopDocs foundDocs2 = searchByFirstName("Brian", searcher);

		System.out.println("Total Results :: " + foundDocs2.totalHits);

		for (ScoreDoc sd : foundDocs2.scoreDocs) {
			Document d = searcher.doc(sd.doc);
			
			Map<String, Object> current = createMapFromDoc(d);
			System.out.println(current);
		}

	}

	private static Map<String, Object> createMapFromDoc(Document d) {
		Map<String, Object> current = new LinkedHashMap<>();
		
		for (IndexableField field : d.getFields()) {
			current.put(field.name(), field.stringValue());

		}
		return current;
	}

	/**
	 * searchByFirstName
	 * 
	 * @param firstName
	 * @param searcher
	 * @return
	 * @throws Exception
	 */
	private static TopDocs searchByFirstName(String firstName, IndexSearcher searcher) throws Exception {
		QueryParser qp = new QueryParser("firstName", new StandardAnalyzer());
		Query firstNameQuery = qp.parse(firstName);
		TopDocs hits = searcher.search(firstNameQuery, 10);
		return hits;
	}

	/**
	 * searchById
	 * 
	 * @param id
	 * @param searcher
	 * @return
	 * @throws Exception
	 */
	private static TopDocs searchById(Integer id, IndexSearcher searcher) throws Exception {
		QueryParser qp = new QueryParser("id", new StandardAnalyzer());
		Query idQuery = qp.parse(id.toString());
		TopDocs hits = searcher.search(idQuery, 10);
		return hits;
	}

	/**
	 * createSearcher
	 * 
	 * @return
	 * @throws IOException
	 */
	private static IndexSearcher createSearcher() throws IOException {
		Directory dir = FSDirectory.open(Paths.get(Constants.INDEX_DIR));
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		return searcher;
	}

}
