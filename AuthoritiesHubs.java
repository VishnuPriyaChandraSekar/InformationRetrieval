package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
/* new */
public class AuthoritiesHubs extends AdjacencyMatrix{
	
    public void AuthoritiesHub(Map<Integer,Double> doc,int k ,int n,AdjacencyMatrix m) throws CloneNotSupportedException, CorruptIndexException, IOException{
      long baseend;
	  long basestart= System.nanoTime();
	  
      ArrayList<ArrayList<Integer>> A = m.buildMatrix(doc);
      baseend = System.nanoTime();
      System.out.println("Time taken to build the base set : "+(baseend - basestart));
      
      ArrayList<ArrayList<Integer>> T = new ArrayList<ArrayList<Integer>>();
      
      ArrayList<Node> authority = new ArrayList<Node>();
      ArrayList<Node> hub = new ArrayList<Node>();		  
      
      
	   /* Constructing the transpose matrix*/
	  for(int j = 0; j<A.size(); j++){
		  ArrayList<Integer> tmp = new ArrayList<Integer>();
		  for(int z=0; z<m.node.size(); z++)
			  tmp.add(A.get(z).get(j));
		  T.add(tmp);
		  
	  }
	   /* Initialize the hub and authority vector*/
	  Iterator<Integer> iterator = m.node.iterator();
	  System.out.println(" The size of the base set"+m.node.size());
	   while(iterator.hasNext()){
		   int value = iterator.next();
		   Node page = new Node(value,1);
		   Node p1 = new Node(value,1);
		   authority.add(page);
		   hub.add(p1);
	   }
	   //System.out.println("Node : "+node.size()+" authority : "+authority.size()+" hub : "+hub.size()+" A "+A.size());
	   /*    T(hub)          node.size X node.size   node.size X 1  */
	   /*    A(authority)    node.size X node.size   node.size X 1    */
	   int iteration = 0;
	   basestart= System.nanoTime();
	   while(true){
		   iteration ++;
	//	   System.out.println("Running");
		   ArrayList<Node> Atmp = copyNode(authority);
		   ArrayList<Node> Htmp = copyNode(hub);
		   double atotal = 0, htotal = 0;
		   /* authority vector*/
		   for(int i=0; i<m.node.size(); i++){
			   double sum = 0;
			   for(int j=0; j<m.node.size(); j++){
				   sum += T.get(i).get(j) * hub.get(j).weight;
			  }
			   Node t = authority.get(i);
			   t.weight = sum; 
			   atotal += Math.pow(sum, 2);
			   authority.set(i,t);
		   }
		   /* Normalize the authority vector*/
		   if(atotal > 0){
			   atotal = Math.sqrt(atotal);
		   
			   for(int i=0; i<authority.size(); i++){
				   Node v = authority.get(i);
				   //System.out.println("Authority value "+v.weight);
				   v.weight = v.weight / atotal;
				   authority.set(i,v);
			   }
		   	}
		   /* hub vector*/
		   for(int i=0; i<m.node.size(); i++){
			   double sum = 0;
			   for(int j=0; j<m.node.size(); j++){
				   sum += A.get(i).get(j) * authority.get(j).weight;
			   }
			   Node t = hub.get(i);
			   t.weight = sum; 
			   htotal += Math.pow(sum, 2);
			   hub.set(i,t);
		   }
		   /* Normalize the hub vector using L2 norm*/
		   if(htotal >0){
		   htotal = Math.sqrt(htotal);
		   	for(int i=0; i<hub.size()&&htotal > 0; i++){
		   		Node v = hub.get(i);
		   		//System.out.println("Hub value "+v.weight);
		   		v.weight = v.weight / htotal;
		   		hub.set(i,v);
		   		}
		   }
		   /*System.out.println("Before Checking");
		   for(int i=0; i<node.size();i++){
			   System.out.println(hub.get(i).weight+"                                     "+authority.get(i).weight);
			   
		   }*/
		   if(check(Atmp,authority) && check(Htmp,hub))
			     break;
		   
	   }
	   baseend= System.nanoTime();
	   /* Finding top n authorities and hubs*/
	   authority=FilterNode(authority);
	   hub=FilterNode(hub);
	   IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
	   System.out.println("Top "+n+" authorities");
	   for(int i=0; i<authority.size();i++){
		   System.out.println(authority.get(i).value+" "+authority.get(i).weight);	
		   Document d = r.document(authority.get(i).value);
		   System.out.println(d.getFieldable("path").stringValue());
	   }
	   System.out.println("Top "+n+" hub");
	   for(int i=0; i<hub.size();i++){
		   System.out.println(hub.get(i).value+" "+hub.get(i).weight);	
		   Document d = r.document(hub.get(i).value);
		   System.out.println(d.getFieldable("path").stringValue());
	   }   
	  
	   System.out.println("Time taken to compute the authority and hub value : "+(baseend - basestart));
	   System.out.println("No of iteration taken to converge : "+iteration);
    }
    public ArrayList<Node> copyNode(ArrayList<Node> tmp) throws CloneNotSupportedException{
    	ArrayList<Node> d = new ArrayList<Node>();
    	for(int i=0; i<tmp.size(); i++){
    		Node t = (Node)tmp.get(i).clone();
    		d.add(t);
    	}
    	return d;
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
    public boolean check(ArrayList<Node> tmp, ArrayList<Node> score){
    	for(int i=0; i<tmp.size(); i++){
    		//System.out.println(tmp.get(i).weight+"   "+score.get(i).weight);
    		if(tmp.get(i).weight - score.get(i).weight > 0.00001)
    			  return false;
    	}
    	return true;
    }
    public void PageRank(){
    	
    }
	public static void main(String[] args) throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		int k,n;
		String q = ""; 
		System.out.println("Authority and Hub");
		System.out.println("Value of K :     ");
		Scanner s = new Scanner(System.in);
		SearchTF tf = new SearchTF();
		AuthoritiesHubs link = new AuthoritiesHubs();
		AdjacencyMatrix m = new AdjacencyMatrix();
		try {
			k = s.nextInt();
			System.out.println("Value of N :     ");
			n = s.nextInt();
			System.out.println("<Query>");
			q = s.next();
			IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
			tf.TFmethod(q.split("//s"), r);
			Map<Integer,Double> top =tf.TF_IDFmethod(q.split("//s"), r,k);
			link.AuthoritiesHub(top,k,n,m);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(" Error : "+e.getMessage());
		}finally{
			s.close();
		}

	}

}
