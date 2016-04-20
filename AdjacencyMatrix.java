package edu.asu.irs13;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AdjacencyMatrix {
	public HashMap<Integer,TreeSet<Integer>> link;
	public Set<Integer> node;
	public AdjacencyMatrix(){
		this.link = new HashMap<Integer,TreeSet<Integer>>();
		this.node = new TreeSet<Integer>();
	}
	public ArrayList<ArrayList<Integer>> buildMatrix(Map<Integer,Double> doc){
		  ArrayList<ArrayList<Integer>> A = new ArrayList<ArrayList<Integer>>();
		  LinkAnalysis.numDocs = 25054;
		  LinkAnalysis l = new LinkAnalysis();
		  
		  for(Map.Entry<Integer, Double> entry : doc.entrySet()){
			  Integer key = entry.getKey();
		
			  int[] out = l.getLinks(key);
			  int[] in = l.getCitations(key);
			  TreeSet<Integer> matrix;  
			  if(link.containsKey(key))
				  matrix = link.get(key);
			  else
			      matrix=new TreeSet<Integer>();
			  for(int j=0; j< out.length ; j++){
		
				  matrix.add(out[j]);
				  node.add(out[j]);
			  }
			  link.put(entry.getKey(), matrix);
			  node.add(key);
			  TreeSet<Integer> outbound=new TreeSet<Integer>();
			  outbound.add(key);
			  for(int i=0; i< in.length; i++){
		
				  node.add(in[i]);
				  if(link.containsKey(in[i]))
					  outbound.addAll(link.get(in[i]));
				   link.put(in[i],outbound);
			  }
		 }
		  
		
		  /* Constructing the adjacency matrix*/
		  Iterator<Integer> it = node.iterator();
		  
		  while(it.hasNext()){
			  Integer key = it.next();
		
			  ArrayList<Integer> tmp = new ArrayList<Integer>();
			  if(link.containsKey(key)){
					  Iterator<Integer> iter = node.iterator();
				  TreeSet<Integer> t = link.get(key);
				  while(iter.hasNext()){
					  Integer column = iter.next();
		
					  if(t.contains(column))
						  tmp.add(1);
					  else
					      tmp.add(0);
				  }
			  }
			  else{
		
				   for(int i=0; i<node.size(); i++)
					   tmp.add(0);
			  }
			  A.add(tmp);
		  }

		return A;
	}
}
