import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;


public class Lucene {
	
	public static Map<String, List<String>> retrieveCrawledPages(String fileName) {		
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		List<String> list = null;
		
    	List<String> pages = new ArrayList<>();
		int counter =0;
    	
        try {
        	pages = Files.readAllLines(new File(fileName).toPath(), Charset.defaultCharset());
        	Iterator<String> it = pages.iterator(); 
        	while(it.hasNext()){
				counter++;
        		String[] elements = it.next().split("@@@@@");
        		if(elements.length == 2) {
        			list = new ArrayList<>();
        			list.add(elements[1]);
        			map.put(elements[0], list);
        		}
        		else if(elements.length == 3) {
        			list = new ArrayList<>();
        			list.add(elements[1]);
        			list.add(elements[2]);
        			map.put(elements[0], list);
        		}
        	}
        	return map;
        }
        catch(IOException ex) {
            System.out.println(ex);
        }
        return map;
    }
	
	public static void main(String[] args) {
		if(args.length <=0) {
			System.out.println("Usage: java Lucene <data file>");
			System.exit(-1);
		}
        Map<String, List<String>> crawledPagesMap = retrieveCrawledPages(args[0]);
        
        try {
        	Analyzer analyzer = new StandardAnalyzer();
            
            // Store the index in memory:
            Directory directory = new ByteBuffersDirectory(); // RAMDirectory() is depricated.
            
            // To store an index on disk, use this instead:       
            IndexWriterConfig config = new IndexWriterConfig(analyzer);        
            IndexWriter indexWriter = new IndexWriter(directory, config);
            	    
    	    for (Map.Entry<String, List<String>> eachMap : crawledPagesMap.entrySet()){
    	    	String content = "";
    	    	String subHeader = "";
    	    	List<String> valueList = eachMap.getValue();
    	    	
    	    	if(eachMap.getValue().size() == 1){
    	    		// System.out.println(eachMap.getValue().toString());
    	    		content = eachMap.getValue().toString();
    	    	}
    	    	else if(eachMap.getValue().size() == 2){
    	    		subHeader = valueList.get(0).toString();
    				content = valueList.get(1).toString();
    	    	}
    	    	
    	    	org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
    	    	
    	    	doc.add(new TextField("title", eachMap.getKey(), org.apache.lucene.document.Field.Store.YES));
    	    	doc.add(new TextField("subheading", subHeader, org.apache.lucene.document.Field.Store.YES));
    	    	doc.add(new TextField("content", content, org.apache.lucene.document.Field.Store.YES));            				
    	        
    	    	indexWriter.addDocument(doc);
    	    }
    	    indexWriter.close();
    	    
    	    // Now search the index:        
    	    // DirectoryReader indexReader = DirectoryReader.open(directory);        
    	    // IndexSearcher indexSearcher = new IndexSearcher(indexReader);        
    	    // QueryParser parser = new QueryParser("content", analyzer);        
    	    // Query query = parser.parse("UCR Admissions");        
    	    
    	    // // System.out.println(query.toString());    
    	    
    	    // int topHitCount = 100;        
    	    // ScoreDoc[] hits = indexSearcher.search(query, topHitCount).scoreDocs;   
    	    
    	    // // Iterate through the results:        
    	    // for (int rank = 0; rank < hits.length; ++rank) {            
    	    // 	org.apache.lucene.document.Document hitDoc = indexSearcher.doc(hits[rank].doc);       
    	    	
    	    //     System.out.println((rank + 1) + " (score:" + hits[rank].score + ")");
    	    //     System.out.println("Title - "+ hitDoc.get("title"));
    	    //     // System.out.println("Sub Heading - " + hitDoc.get("subheading"));
    	    //     // System.out.println("Content - " +hitDoc.get("content"));
    	        
   	      	// 	// System.out.println(indexSearcher.explain(query, hits[rank].doc));
    	    // }        
    	    // indexReader.close();        
    	    // directory.close();
        }
        catch(Exception ex) {
        	System.out.println(ex);
        }
	}
}
