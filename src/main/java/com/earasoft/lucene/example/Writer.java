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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
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
				Document document1 = createDocument(csvRecord);
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
	private static Document createDocument(CSVRecord csvRecord) {
		Document document = new Document();
		document.add(new StringField("id", csvRecord.get("id"), Store.YES));
		document.add(new TextField("first_name", csvRecord.get("first_name"), Store.YES));
		document.add(new TextField("last_name", csvRecord.get("last_name"), Store.YES));
		// document.add(new StringField("url", csvRecord.get("url"), Store.YES));
		
		FieldType fieldType = new FieldType();
		fieldType.setStored(true);
		// fieldType.setIndexOptions(IndexOptions.DOCS);
		
		document.add(new Field("is_cool_field", csvRecord.get("is_cool").getBytes(), fieldType));
		document.add(new StringField("is_cool", csvRecord.get("is_cool"), Store.YES));
		
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
