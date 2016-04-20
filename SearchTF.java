package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class SearchTF {
 
    public HashMap<String,Double> qw;     	// contains query weight for each term in the query
    public HashMap<Integer,Double> sim ;     // cosine similarity for each document
    public HashMap<Integer, Double> tdfsim;
    public ArrayList<Double> idf;
    public double qlen;
    public HashMap<Integer, Double> dlen ; 
    public HashMap<Integer, Double> tdfdlen;
    public SearchTF(){
     	this.qlen = 0.0;
    	this.dlen =new HashMap<Integer, Double>();
     	this.qw = new HashMap<String,Double>();
    	this.sim = new HashMap<Integer,Double>();
    	this.tdfsim = new HashMap<Integer, Double>();
    	this.idf = new ArrayList<Double>();
    	this.tdfdlen = new HashMap<Integer, Double>();
    }
    
    
    public void queryWeight(String[] q){
    
    	/* To find tf for each term in the query */
    	for(String tr : q){
    		if(qw.containsKey(tr.toLowerCase()))
    			qw.put(tr.toLowerCase(),qw.get(tr)+1);
    		else
    			qw.put(tr.toLowerCase(),new Double(1.0));
    	}
    	
    	/* To find 'w' for each term in the query w = 0.5+(0.5 * tf) */
    	/* To find |q| = sqrt(w1^2 + w2^2 + w3^2)*/
    	for(Map.Entry<String,Double> entry : qw.entrySet() ){
    		qw.put(entry.getKey(),0.5+0.5*((double)entry.getValue()));  
    		qlen = qlen + Math.pow(qw.get(entry.getKey()),2);   // |Q|
    	}

    }
	
    
	public void TFmethod(String[] q, IndexReader r) throws IOException{
		
		long end, start, tfend;
		HashMap<Integer,String> url = new HashMap<Integer, String>();
		long tfstart = System.nanoTime();
		int noDoc = 0;
		int j=0;
		
		   
		queryWeight(q);
		for(int i=0; i< q.length; i++){
			Term term = new Term("contents", q[i]);
			TermDocs tdocs = r.termDocs(term);
			while(tdocs.next())	
			{
				//System.out.println("Doc ID : "+tdocs.doc());
			   if( sim.containsKey(q[i].toLowerCase())){
				   sim.put(tdocs.doc(),sim.get(tdocs.doc())+((double)qw.get(q[i])*tdocs.freq()));// q1 * w1 +q2 * w2
				   dlen.put(tdocs.doc(),dlen.get(tdocs.doc())+Math.pow(tdocs.freq(),2)); // w1^2 +w2^2+...+wi^2
			   
			   }
			   else{
				   sim.put(tdocs.doc(),(double)qw.get(q[i])*tdocs.freq()); // qi * wi
				   dlen.put(tdocs.doc(),Math.pow(tdocs.freq(),2));
				   url.put(tdocs.doc(),r.document(tdocs.doc()).getFieldable("path").stringValue().replace("%%", "/"));
			   }
				noDoc++;
			}		
	     }
		 
		 /* divide sim[i] by the product of document length and query length*/
		 
		 for(Map.Entry<Integer,Double> entry : sim.entrySet()) 
			 sim.put(entry.getKey(), entry.getValue()/ (Math.sqrt(dlen.get(entry.getKey())*Math.sqrt(qlen))) );
		 
		 /* Sort sim[i] in ascending order */
		 //Map<Integer,Double> sorted = sortbyValues(sim);
		 		 		 
		 //for(Map.Entry<Integer,Double> entry  : sorted.entrySet())
			// System.out.println(entry.getKey()+"		"+url.get(entry.getKey()));
	
			 
		 
		 //tfend = System.nanoTime();
		 //System.out.println("No of documents retrieved : "+noDoc);
		 //System.out.println("Retrieved pages in : "+(tfend-tfstart)+" ns");
	
	
	}
	public Map<Integer,Double> TF_IDFmethod(String[] q,IndexReader r, int n) throws IOException{
			long idfend;
			long idfstart= System.nanoTime();
			HashMap<Integer, String> url = new HashMap<Integer, String>();
			Map<Integer,Double> sorted = new HashMap<Integer, Double>();
			int docMax = r.numDocs();
			int noDoc = 0, j=0;
			int k=0;
			qlen = 0;
			
		
			for(int i=0; i< q.length; i++){
				Term term = new Term("contents", q[i]);
				
				idf.add(Math.log(docMax/r.docFreq(term)));
			}
			
			for(Map.Entry<String,Double> entry : qw.entrySet() ){
	    		qw.put(entry.getKey(),((entry.getValue()-0.5)*idf.get(j))+0.5);
	    		j++;
	    		qlen = qlen + Math.pow(qw.get(entry.getKey()),2);   // |Q|
	    	}
			
			
			
			for(int i=0; i< q.length; i++){
				Term term = new Term("contents", q[i]);
				TermDocs tdocs = r.termDocs(term);
				
				while(tdocs.next())	
				{  	// q1 * w1 +q2 * w2 // w1^2 +w2^2+...+wi^2
				   if( tdfsim.containsKey(q[i].toLowerCase())){
					   tdfsim.put(tdocs.doc(),tdfsim.get(tdocs.doc())+(double)qw.get(q[i])*tdocs.freq()*idf.get(idf.size()-1));
				       tdfdlen.put(tdocs.doc(), tdfdlen.get(tdocs.doc())+Math.pow(tdocs.freq()*idf.get(idf.size()-1),2));
				   }
				   else{
					    // qi * wi = (tdf(q) * tf(q))* (tdf(d) *tf(d))
					   tdfsim.put(tdocs.doc(),(double)qw.get(q[i])*tdocs.freq()*idf.get(idf.size()-1));
					   tdfdlen.put(tdocs.doc(),Math.pow(tdocs.freq()*idf.get(idf.size()-1),2));
					   url.put(tdocs.doc(),r.document(tdocs.doc()).getFieldable("path").stringValue().replace("%%", "/"));
				   }
					noDoc++;
					
				}
				
		     }
		
			 /* divide sim[i] by the product of document length and query length*/
			 for(Map.Entry<Integer,Double> entry : tdfsim.entrySet())
				 tdfsim.put(entry.getKey(), entry.getValue()/ (Math.sqrt(tdfdlen.get(entry.getKey())*Math.sqrt(qlen))) );
			
			 
			 /* Sort sim[i] in ascending order */
			 
			 
			 
			for(Map.Entry<Integer,Double> entry  : sortbyValues(tdfsim).entrySet()){
				 if( n < 1)
					 break;
				// System.out.println(entry.getKey());
				 sorted.put(entry.getKey(), entry.getValue());
				 Document d = r.document(entry.getKey());
				  // System.out.println(d.getFieldable("path").stringValue());
				 n--;
			 }
				 
			 idfend = System.nanoTime();
			 System.out.println("Retrieved pages in : "+(idfend-idfstart)+" ns");
		      
		   		return sorted;
		}
		
		public HashMap<Integer, Double> sortbyValues(HashMap<Integer, Double> hm){
			List<Object> l = new LinkedList<Object>(hm.entrySet());
			Collections.sort(l,new Comparator<Object>(){
				public int compare(Object a, Object b){
					return ((Comparable) ((Map.Entry) (a)).getValue())
			                  .compareTo(((Map.Entry) (b)).getValue());
				}
			});
			
			HashMap<Integer, Double> sorted = new LinkedHashMap<Integer, Double>();
			for(Iterator<Object> it = l.iterator(); it.hasNext();){
				Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>)it.next();
				sorted.put(entry.getKey(),entry.getValue());
			}
			return sorted;
		}

    public static void main(String[] args) throws CorruptIndexException, IOException {
		// TODO Auto-generated method stub
    	IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
    	Scanner s = new Scanner(System.in);
		String line="";
		System.out.println("<Query> ");
		while(!(line=s.nextLine()).equals("quit")){
			
			SearchTF search = new SearchTF();
			System.out.println("Performing search using TF method");
			String[] query = line.split("\\s+");
			search.TFmethod(query, r);
			System.out.println("Performing search using TF-IDF method");
			search.TF_IDFmethod(query,r,10);
			System.out.println("Completed");
			System.out.println("<Query> ");
		}
		s.close();
		r.close();
     }
	
	
	
}
/*
*/