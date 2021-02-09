package wdc.productcorpus.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.valuesource.ConstValueSource;
import org.apache.lucene.queries.function.valuesource.FloatFieldSource;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafFieldComparator;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import scala.collection.immutable.NumericRange;

public class LuceneIndexerResturantsMagellan {

	private static final String INDEX_DIR = "C:\\Users\\User\\Desktop\\multisourceER_data\\restaurants_magellan\\index_multiplefields\\yelp_3";
	private static final String indexFile = "C:\\Users\\User\\Desktop\\multisourceER_data\\restaurants_magellan\\yelp_3.csv";
	
	private static final String searchFile = "C:\\Users\\User\\Desktop\\multisourceER_data\\restaurants_magellan\\zomato_2.csv";
	private static final String outputFile = "C:\\Users\\User\\Desktop\\multisourceER_data\\restaurants_magellan\\pairsafterblocking\\yelp_3__zomato_2.csv";
	
	
	public static void main (String args[]) throws Exception{
		System.out.println("Start");
		LuceneIndexerResturantsMagellan indexer = new LuceneIndexerResturantsMagellan();
		//indexer.index();
		
		// now search
		indexer.search();
		/*
		//IndexSearcher searcher = createSearcher();

		
		TopDocs foundDocs = searchByMultipleFields("Urge for Going (Night Flyer)",  "Nan", "Night Flyer", 002,  2008, searcher, 20);
		for (ScoreDoc sd : foundDocs.scoreDocs) 
        {
            Document d = searcher.doc(sd.doc);
            System.out.println(d.get("label"));
        }
        */
	}
	
	public void search() throws IOException {
		IndexSearcher searcher = createSearcher();
				
		BufferedReader reader = new BufferedReader(new FileReader(new File(searchFile)));
		String line = "";
		int lineCounter = 0;
		ArrayList<String> similar = new ArrayList<String>();
		while ((line=reader.readLine())!=null) {
			try {
				lineCounter++;
				if (lineCounter==1) continue;
				if (lineCounter%10000==0) System.out.println(lineCounter);
				String[] fields = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				String id = fields[6];
				String label = fields[0];
				label = Normalizer.normalize(label, Normalizer.Form.NFD)	;
				label = label.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
				
				String address = fields[1];
				address = Normalizer.normalize(address, Normalizer.Form.NFD)	;
				address = address.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
					
				label = label.replaceAll("[^\\s\\p{L}\\p{N}]+", " ").toLowerCase();
				address = address.replaceAll("[^\\s\\p{L}\\p{N}]+", " ").toLowerCase();
				
				//TopDocs foundDocs = searchByTitle(title, searcher, 20);

				TopDocs foundDocs = searchByMultipleFields(label, address, searcher, 5);
				
				for (ScoreDoc sd : foundDocs.scoreDocs) 
		        {
		            Document d = searcher.doc(sd.doc);
		            System.out.println(label+"####"+d.get("label"));
		            similar.add(d.get("id")+";"+id);
		        }
				
				//write the pairs
				if (similar.size()>100000){
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile),true));
					for (String pair:similar){
						writer.write(pair+"\n");
					}
					writer.flush();
					writer.close();
					similar.clear();
				}
				
			}
			catch(Exception e){
				System.out.println(e.getMessage());
				System.out.println(line);
			} 
			
		}
		reader.close();
		
		//write the pairs
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile),true));
		for (String pair:similar){
			writer.write(pair+"\n");
		}
		writer.flush();
		writer.close();
		
        
	}
	public void index() throws IOException{
		IndexWriter writer = createWriter();
        List<Document> documents = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(new File(indexFile)));
		String line = "";
		int lineCounter = 0;
		while ((line=reader.readLine())!=null) {

			lineCounter++;
			if (lineCounter==1) continue;
			if (lineCounter%10000==0) System.out.println(lineCounter);
			//which field do you want to use for indexing?
			String[] fields = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			String id = fields[6];
			String label = fields[0];
			label = Normalizer.normalize(label, Normalizer.Form.NFD)	;
			label = label.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			
			String address = fields[1];
			address = Normalizer.normalize(address, Normalizer.Form.NFD)	;
			address = address.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			
			
			// replace all non-alphanumeric characters considerng the different languages	
			label = label.replaceAll("[^\\s\\p{L}\\p{N}]+", " ").toLowerCase();
			address = address.replaceAll("[^\\s\\p{L}\\p{N}]+", " ").toLowerCase();
			
			
			Document doc = createDocument(id,label,address);
			documents.add(doc);
		}
		reader.close();
		writer.deleteAll();
        
        writer.addDocuments(documents);
        writer.commit();
        writer.close();
	}
	
	private static Document createDocument(String id, String label, String address) 
	{
	    Document document = new Document();
	    document.add(new StringField("id", id.toString() , Field.Store.YES));
	    
	    document.add(new TextField("label", label , Field.Store.YES));	    
	    document.add(new TextField("address", address , Field.Store.YES));
	    
	    return document;
	}
	
	private static IndexWriter createWriter() throws IOException 
	{
	    FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
	    IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
	    IndexWriter writer = new IndexWriter(dir, config);
	    return writer;
	}
	
	private static IndexSearcher createSearcher() throws IOException 
	{
	    Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
	    IndexReader reader = DirectoryReader.open(dir);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    return searcher;
	}
	
	private static TopDocs searchByMultipleFields(String label, String address, IndexSearcher searcher, int hitscount) throws Exception
	{
		ArrayList<Query> queries= new ArrayList<Query>();

		
		//label
	    //fuzzy query for title which supports few edits on the character level
		if (label != null && !label.isEmpty() && label!=""){
			queries.add(new FuzzyQuery(new Term("label", label)));
			//text field query with standard analyzer
			QueryParser qp = new QueryParser("label",  new StandardAnalyzer());
			queries.add(qp.parse(label));
		}
		if (address != null && !address.isEmpty() &&address!=""){
			//album
			queries.add(new FuzzyQuery(new Term("address", address)));	
			QueryParser qp_album = new QueryParser("address",  new StandardAnalyzer());
			queries.add(qp_album.parse(address));	    
		}
		
		
		BooleanQuery.Builder multipleFieldsQuery = new BooleanQuery.Builder();
		for (Query query:queries) {
			multipleFieldsQuery.add(query, Occur.SHOULD);
		}

		BooleanQuery query = multipleFieldsQuery.build();
		TopDocs hits = searcher.search(query, hitscount);
		
		return hits;
	}
	
	private static TopDocs searchByTitle(String title, IndexSearcher searcher, int hitscount) throws Exception
	{ 
		
		Term term = new Term("label", title);
		Query query = new FuzzyQuery(term, 1);
		TopDocs hits = searcher.search(query, hitscount);
	    return hits;
	}
	
	



	
}