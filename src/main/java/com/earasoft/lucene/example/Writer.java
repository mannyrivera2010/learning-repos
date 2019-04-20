package com.earasoft.lucene.example;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

/**
 * Writer
 * 
 * @author erivera
 *
 */
public class Writer {
	/**
	 * main
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		IndexWriter writer = createWriter();
		List<Document> documents = new ArrayList<>();
		
		// Read file
		try (CSVParser csvParser = CSVParser.parse(Paths.get("mock_data.csv"), Charset.defaultCharset(), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
			for (CSVRecord csvRecord : csvParser) {
//				System.out.println("Record No - " + csvRecord.get(0));
				int id = Integer.parseInt(csvRecord.get(0));
				
				Document document1 = createDocument(id, csvRecord.get(1), csvRecord.get(2), csvRecord.get(3));
				documents.add(document1);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

		

		// Let's clean everything first
		writer.deleteAll();

		writer.addDocuments(documents);
		writer.commit();
		writer.close();

		System.out.print("finish");
	}

	/**
	 * createDocument
	 * 
	 * @param id
	 * @param firstName
	 * @param lastName
	 * @param website
	 * @return
	 */
	private static Document createDocument(Integer id, String firstName, String lastName, String website) {
		Document document = new Document();
		document.add(new StringField("id", id.toString(), Store.YES));
		document.add(new TextField("firstName", firstName, Store.YES));
		document.add(new TextField("lastName", lastName, Store.YES));
		document.add(new TextField("website", website, Store.YES));
		return document;
	}

	/**
	 * createWriter
	 * 
	 * @return
	 * @throws IOException
	 */
	private static IndexWriter createWriter() throws IOException {
		FSDirectory dir = FSDirectory.open(Paths.get(Constants.INDEX_DIR));
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		config.setCodec(new SimpleTextCodec());

		IndexWriter writer = new IndexWriter(dir, config);
		return writer;
	}
}
