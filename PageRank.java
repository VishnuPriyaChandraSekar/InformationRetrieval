/**
 * 
 */
package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

/**
 * @author vishnupriya
 *
 */
public class PageRank extends AdjacencyMatrix{
	public int d ;
	public float c ;
	public HashMap<Integer,ArrayList<Node>> M;
	ArrayList<Double> R ;
	public int in;
	public PageRank(){
		this.d = 10;
		this.c = 0.85f;
		this.M = new HashMap<Integer,ArrayList<Node>>();
		this.R= new ArrayList<Double>();
	}
    public void Rank(Map<Integer,Double> doc,double m,IndexReader reader ) throws CloneNotSupportedException, CorruptIndexException, IOException{
    	HashMap<Integer,Double> result = new HashMap<Integer,Double>();
    	for(Map.Entry<Integer, Double> entry : doc.entrySet()){
    		double rank = m * R.get(entry.getKey()) + (1- m) * entry.getValue();
    		result.put(entry.getKey(), rank);
    	}
    	
    	for(Map.Entry<Integer,Double> entry  : sortbyValues(result).entrySet()){
			 System.out.print(entry.getKey()+"   ");
			 Document d = reader.document(entry.getKey());
			   System.out.println(d.getFieldable("path").stringValue());
			
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
		
		HashMap<Integer, Double> sorted = new LinkedHashMap<Integer, Double>();
		for(Iterator<Object> it = l.iterator(); it.hasNext();){
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>)it.next();
			sorted.put(entry.getKey(),entry.getValue());
		}
		return sorted;
	}
    
    public void buildPageRank() throws CorruptIndexException, IOException{
    	IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
    	LinkAnalysis.numDocs = 25054;
    	LinkAnalysis l = new LinkAnalysis();
    	int N = r.maxDoc();
    	double z = (double)1/N;
    	double K = (double)(1-c)*z;
    	boolean sink = false;
    	ArrayList<Double> R_old = new ArrayList<Double>();
    	HashMap<Integer,Double> colSum = new HashMap<Integer,Double>();
    	Set<Integer> v = new HashSet<Integer>(); 
        
        for(int i=0; i<N ;i++){
        	v.add(i);
        	R_old.add(z);
        	R.add(1.0);
        	if(l.getLinks(i).length == 0)
        		sink = true;
        }
        /** Constructing the adjacency matrix**/
        Iterator<Integer> n = v.iterator();
        while(n.hasNext()){
        	Integer key = n.next();
        	ArrayList<Node> outnode = new ArrayList<Node>();
        	for(int e:l.getCitations(key)){
        		Node t = new Node(e, 1);
        		outnode.add(t);
        		if(colSum.containsKey(e))
        			colSum.put(e, colSum.get(e)+1);
        		else
        			colSum.put(e, t.weight);
        	}
        	M.put(key,outnode);
        }
        
        if(!sink)
        	z = 0;
        
        /** Constructing the M" matrix = c (M+Z) +(1-c) * K  **/
        for(Map.Entry<Integer, ArrayList<Node>> entry : M.entrySet()){
        	ArrayList<Node> tmpList = entry.getValue();
        	for(int j =0 ; j< tmpList.size(); j++){
        		Node t = tmpList.get(j);
        		t.weight = (t.weight/colSum.get(t.value)) * c + z + K;
        	}
        /*	for(int d=0; d<sink_node.size();d++){
        		Node sink = new Node(sink_node.get(d),c * z + K);
        		tmpList.add(sink);
        	}*/
        	entry.setValue(tmpList);
        }
         
        /** Power iteration R = M" * R_old **/
        
        int r_ind  = 0;
        while(true || r_ind < 30){
        	
        	double sum = 0.0;
        	   	for(Map.Entry<Integer, ArrayList<Node>> entry : M.entrySet()){
        		ArrayList<Node> t = entry.getValue();
        		double total = 0.0;
        		 
        		for(int j =0; j< t.size(); j++){
        			total += t.get(j).weight * R_old.get(t.get(j).value);
        			sum += total;
        		}
        			
          		
        		R.set(r_ind,total);
        		r_ind ++;
        	}
      
        	/**Normalize the probability matrix**/
        	/*for(int i=0; i<R.size(); i++)
        		R.set(i, R.get(i)/sum);*/
        		
        	if(check(R,R_old))
        		break;
        	else
        	{
        		R_old.clear();
        		R_old = new ArrayList<Double>(R);
        	}
        
        }
      
    }
    public ArrayList<Node> copyNode(ArrayList<Node> tmp) throws CloneNotSupportedException{
    	ArrayList<Node> d = new ArrayList<Node>();
    	for(int i=0; i<tmp.size(); i++){
    		Node t = (Node)tmp.get(i).clone();
    		d.add(t);
    	}
    	
    	return d;
    }
    public boolean check(ArrayList<Double> Rtmp, ArrayList<Double> Otmp){
    	double high = Math.abs(Rtmp.get(0)- Otmp.get(0));
    	double doc = Double.MIN_VALUE;
    	
    	//System.out.println("size of R : "+R.size());
    	for(int i=1; i<Rtmp.size(); i++){
    	   // System.out.println("R : "+Rtmp.get(i)+"  Old : "+Otmp.get(i));
    		high = Math.max(high, Math.abs(Rtmp.get(i)- Otmp.get(i)));
    		if(doc < Rtmp.get(i)){
    			doc = Rtmp.get(i);
    			in = i;
    		}
    		
    	}
    	//System.out.println("Maximum doc "+doc+" no "+in);
    	if( high > 0.000000001)
    	       return false;
    	else 
    		return true;
    }
    public ArrayList<Node> FilterNode(ArrayList<Node> tmp){
    	Collections.sort(tmp,new Comparator<Node>(){
    		public int compare(Node n, Node m){
    			if(n.weight == m.weight)
    				return 0;
    			else 
    				return m.weight > n.weight ? 1:-1;
    		}
    	});
    	return tmp;
    }
	/**
	 * @param args
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 * @throws CloneNotSupportedException 
	 */
	public static void main(String[] args) throws CorruptIndexException, IOException, CloneNotSupportedException {
		// TODO Auto-generated method stub
		String q = ""; 
		System.out.println("PageRank");
		Scanner s = new Scanner(System.in);
		SearchTF tf = new SearchTF();
		PageRank pr= new PageRank();
		long begin = System.nanoTime();
		pr.buildPageRank();
		long end = System.nanoTime();
		System.out.println(" Time taken to calculate page rank : "+(end-begin));
		try {
			
			System.out.println("Value of W :     ");
			double n = s.nextDouble();
			System.out.println("<Query>");
			q = s.next();  
			IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
			tf.TFmethod(q.split("//s+"), r);
			Map<Integer,Double> top =tf.TF_IDFmethod(q.split("//s+"), r,10);
		    pr.Rank(top,n,r);	
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(" Error : "+e.getMessage());
		} 		finally{
			s.close();
		}
	}

}
