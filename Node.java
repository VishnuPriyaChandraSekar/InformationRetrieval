package edu.asu.irs13;



public class Node implements Cloneable{
   public int value;
   public double weight;
   
   public Node(int value, double weight){
	   this.value = value;
	   this.weight = weight; 
   }
   @Override
   protected Object clone() throws CloneNotSupportedException {
       return super.clone();
   }
}
