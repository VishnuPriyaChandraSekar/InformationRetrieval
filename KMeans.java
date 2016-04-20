package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class KMeans {

	public void Clustering(Map<Integer,Double> doc,IndexReader r) throws IOException{
		int count = 1;
	    long start,end;
	    double vs1=0.0, vs2=0.0, vs3=0.0  ;
	   	HashMap<Integer,Integer> cluster = new HashMap<Integer,Integer>();
	   	HashMap<Integer,ArrayList<Double>> old_centroid = new HashMap<Integer,ArrayList<Double>>();
	    ArrayList<Integer> old_cluster = new ArrayList<Integer>();
	    HashMap<Integer,Double> group1 = new HashMap<Integer,Double>();
	    HashMap<Integer,Double> group2 = new HashMap<Integer,Double>();
	    HashMap<Integer,Double> group3 = new HashMap<Integer,Double>();
	    HashMap<String,Double> centroid1 = new HashMap<String,Double>(); 
	    HashMap<String,Double> centroid2 = new HashMap<String,Double>();
	    HashMap<String,Double> centroid3 = new HashMap<String,Double>();
	    
	    ArrayList<Double> dumb = new ArrayList<Double>();
	    dumb.add(0.0);
	    dumb.add(0.0);
	    
	    
	    HashMap<Integer,HashMap<String, Double>> matrix = new HashMap<Integer, HashMap<String,Double>>();
	    HashSet<String> terms = new HashSet<String>();
	    TermEnum te = r.terms();
	    int max = r.maxDoc();
	    start = System.nanoTime();
	    
	    /** Find the TF-IDF value for each word in the set of documents**/
	    while(te.next()){
	    	String term = te.term().text();
	    	Term t = new Term("contents",term);
	    	if(r.docFreq(t) > 0){
	    		double idf = (double)Math.log(max/r.docFreq(t));
		    	TermDocs td = r.termDocs(t);
		    	while(td.next()){
		    		if(doc.containsKey(td.doc())){
		    			if(!terms.contains(term))
		    				terms.add(term);
		    			HashMap<String,Double> index;
		    			if(matrix.containsKey(td.doc()))
			    			index = matrix.get(td.doc());
			    		else
			    			index = new HashMap<String,Double>();
			    		index.put(term,td.freq() * idf);
		    		    matrix.put(td.doc(),index);
		    		}	    		
		    	}
	    	}
	    	
	    }
	    
	   
	    /** Randomly assign the document to the clusters**/
	    for(Map.Entry<Integer,Double> entry: doc.entrySet()){
	       if( count >=1 && count <=19){
	    	   group1.put(entry.getKey(),1.0);
	    	   cluster.put(entry.getKey(), 1);
	    	   old_cluster.add(1);
	    	   old_centroid.put(1,dumb);
	       }	   
	      else if( count > 19 && count <= 38){
	    	  group2.put(entry.getKey(),1.0);
	    	  cluster.put(entry.getKey(), 2);
	    	  old_cluster.add(2);
	    	  old_centroid.put(2,dumb);
	      }
	      else {
	    	  group3.put(entry.getKey(),1.0);
	    	  cluster.put(entry.getKey(), 3);
	    	  old_cluster.add(3);
	    	  old_centroid.put(3,dumb);
	      }
	    
	      
	    	count++;   	   
	    }
	     /** Unique ord**/
	   
	    /** Calculate the centroid for each clusters **/
	    
	    while(true){
	    	for(Map.Entry<Integer, Integer> entry : cluster.entrySet()){
	        	int docNum = entry.getKey();
	        	int group = entry.getValue();
	        	//System.out.println("Group  : "+group);
	        	Iterator<String> it = terms.iterator();
	        	while(it.hasNext()){
	        		String word = it.next();
	        		
	        		if(group == 1){
	            		if(matrix.get(docNum).containsKey(word)){
	            			if(centroid1.containsKey(word))
	            				centroid1.put(word,centroid1.get(word)+(matrix.get(docNum).get(word))/group1.size());
	            			else
	            				centroid1.put(word,matrix.get(docNum).get(word)/group1.size());
	            		}
	            		
	            	}
	            	else if(group == 2){
	            		if(matrix.get(docNum).containsKey(word)){
	            			if(centroid2.containsKey(word))
	            				centroid2.put(word,centroid2.get(word)+(matrix.get(docNum).get(word))/group2.size());
	            			else
	            				centroid2.put(word, matrix.get(docNum).get(word)/group2.size());
	            		}	
	            		
	            	}
	            	else if(group == 3){
	            		if(matrix.get(docNum).containsKey(word)){
	            			if(centroid3.containsKey(word))
	            				centroid3.put(word,centroid3.get(word)+(matrix.get(docNum).get(word))/group3.size());
	            			else
	            				centroid3.put(word, matrix.get(docNum).get(word)/group3.size());
	            		}	
	            		
	            	}
	            }
	        }
		  
		    group1.clear();
		    group2.clear();
		    group3.clear();
		    
		    /** Calculate the vector similarity between the centroids and documents **/
		  
		    for(Map.Entry<Integer, HashMap<String,Double>> entry: matrix.entrySet()){
		    	int docNum = entry.getKey();
		    	double doclen = 0.0, clen = 0.0;
		    	for(Map.Entry<String,Double> t: centroid1.entrySet()){
		    		String word = t.getKey();
		    		if(matrix.get(docNum).containsKey(word)){
		    			doclen += Math.pow(matrix.get(docNum).get(word), 2);
			    		clen += Math.pow(centroid1.get(word), 2);
			    		vs1 += matrix.get(docNum).get(word) * centroid1.get(word);
		    		}
		    	}
		    	vs1 = vs1 /(Math.sqrt(doclen)*Math.sqrt(clen));
		    	//System.out.println("V1 : "+vs1);
		    	doclen = 0.0;
		    	clen = 0.0;
		    	for(Map.Entry<String, Double> t: centroid2.entrySet()){
		    		String word = t.getKey();
		    		if(matrix.get(docNum).containsKey(word)){
		    			doclen += Math.pow(matrix.get(docNum).get(word), 2);
			    		clen += Math.pow(centroid2.get(word), 2);
			    		vs2 += matrix.get(docNum).get(word) * centroid2.get(word);
		    		}
		    		
		    	}
		    	vs2 = vs2 /(Math.sqrt(doclen)*Math.sqrt(clen));
		    	//System.out.println("V2 : "+vs2);
		    	doclen = 0.0;
		    	clen = 0.0;
		    	
		    	for(Map.Entry<String, Double> t: centroid3.entrySet()){
		    		String word = t.getKey();
		    		if(matrix.get(docNum).containsKey(word)){
		    			doclen += Math.pow(matrix.get(docNum).get(word), 2);
			    		clen += Math.pow(centroid3.get(word), 2);
			    		vs3 += matrix.get(docNum).get(word) * centroid3.get(word);
		    		}
		    		
		    	}
		    	vs3 = vs3 /(Math.sqrt(doclen)* Math.sqrt(clen));
		    	//System.out.println("V3 : "+vs3);
		    		   	//System.out.println("V10 : "+vs10);
		    	if(vs1 > vs2 && vs1 > vs3 ){
		    		cluster.put(docNum, 1);
		    		group1.put(docNum, vs1);
		    	}
		    	else if(vs2 > vs1 && vs2 > vs3 ){
		    		cluster.put(docNum, 2);
		    		group2.put(docNum, vs2);
		    	}
		    	else if(vs3 > vs1 && vs3 > vs2 ){
		    		cluster.put(docNum, 3);
		    		group3.put(docNum, vs3);
		    	}
		    	vs1 = 0.0;
		    	vs2 = 0.0;
		    	vs3 = 0.0;
		    		    	
		    }
		    System.out.println("The ize of the cluter"+cluster.size());
		    List<Integer> tmp = new ArrayList<Integer>(cluster.values());
		    boolean com = compareCentroid(new ArrayList<Double>(centroid1.values()),old_centroid.get(1)) ;
		    com = com && compareCentroid(new ArrayList<Double>(centroid2.values()),old_centroid.get(2));
		    com = com && compareCentroid(new ArrayList<Double>(centroid3.values()),old_centroid.get(3));
		    		    		    
	    	if( com ){
	    	    
	    	    old_centroid.put(1, new ArrayList<Double>(centroid1.values()));
	    	    old_centroid.put(1, new ArrayList<Double>(centroid2.values()));
	    	    old_centroid.put(3, new ArrayList<Double>(centroid3.values()));
	    	
	    		centroid1.clear();
	    	    centroid2.clear();
	    	    centroid3.clear();
	    	
	    	}
	    	else{
	    	 	group1 = sortbyValues(group1);
	    	 	group2 = sortbyValues(group2);
	    	 	group3 = sortbyValues(group3);
	    		int top = 3;
	    	 	System.out.println("Top 3 documents in cluster 1");
	    	    for(Map.Entry<Integer, Double> e : group1.entrySet()){
	    	    	if( top == 0)
	    	    		break;
	    	    	System.out.print(e.getKey());
	    	    	Document d = r.document(e.getKey());
					System.out.print(" "+d.getFieldable("path").stringValue());
					System.out.println();
	    	    	top--;
	    	    }
	    	    top = 3;
	    	    System.out.println("Top 3 documents in cluster 2");
	    	    for(Map.Entry<Integer, Double> e : group2.entrySet()){
	    	    	if( top == 0)
	    	    		break;
	    	    	System.out.print(e.getKey());
	    	    	Document d = r.document(e.getKey());
					System.out.print(" "+d.getFieldable("path").stringValue());
					System.out.println();
	    	    	top--;
	    	    }
	    	    top = 3;
	    	    System.out.println("Top 3 documents in cluster 3");
	    	    for(Map.Entry<Integer, Double> e : group3.entrySet()){
	    	    	if( top == 0)
	    	    		break;
	    	    	System.out.print(e.getKey());
	    	    	Document d = r.document(e.getKey());
					System.out.print(" "+d.getFieldable("path").stringValue());
					System.out.println();
	    	    	top--;
	    	    }
	    	    
	    	     end = System.nanoTime();
				 System.out.println("Retrieved pages in : "+(end-start)+" ns");
			      
		    	break;
	    	}
	    		
	    }

	    
	}
	
	public HashMap<Integer, Double> sortbyValues(HashMap<Integer, Double> hm){
		List<Object> l = new LinkedList<Object>(hm.entrySet());
		Collections.sort(l,new Comparator<Object>(){
			public int compare(Object a, Object b){
				return ((Comparable) ((Map.Entry) (a)).getValue())
		                  .compareTo(((Map.Entry) (b)).getValue());
			}
		});
		
		HashMap<Integer, Double> sorted = new HashMap<Integer, Double>();
		for(Iterator<Object> it = l.iterator(); it.hasNext();){
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>)it.next();
			sorted.put(entry.getKey(),entry.getValue());
		}
		return sorted;
	}
	public boolean compareCentroid(List<Double> centroid, ArrayList<Double> old){
		
		for(int i=0; i<Math.max(centroid.size(),old.size()); i++){
			//System.out.println("Centroid : "+centroid.get(i)+"  Old : "+old.get(i));
			int ret = Double.compare(centroid.get(i), old.get(i));
			if(ret !=0 || Math.abs(centroid.get(i)-old.get(i)) > 0.0000001){
				//System.out.println("They are not equal");
				return true;
			}
				
		}
		return false;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner s = new Scanner(System.in);
		String query = "";
		try {
			IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
			System.out.println("<Query>");
			query = s.nextLine();
			SearchTF tf = new SearchTF();
			tf.TFmethod(query.split("\\s+"), r);
			Map<Integer,Double> doc = tf.TF_IDFmethod(query.split("\\s+"), r, 50);
			KMeans km = new KMeans();
			km.Clustering(doc,r);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		finally{
			s.close();
		}

	}

}
