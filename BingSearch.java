package edu.asu.irs13;
import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class BingSearch extends Applet implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public TextField tfield;
	public JTextArea area; 
	public Button b;
	
	Map<Integer,Double> doc;
	ArrayList<String> url;
	
	public void BingSearch(){
		url = new ArrayList<String>();
	}
	public void init(){
		setSize(500,500);
		tfield = new TextField(50); 
		area = new JTextArea(0,30);
		b = new Button("Search");
		add(tfield);
		add(b);
		add(area);

	    b.addActionListener(this);
	   
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		if(e.getSource()== b)
		{   
			
			String[] query = tfield.getText().split("\\s+");
			SearchTF t = new SearchTF();
			IndexReader r;
			try {
				r = IndexReader.open(FSDirectory.open(new File("index")));
				t.TFmethod(query, r);
				doc = t.TF_IDFmethod(query, r, 10);
				for(int docID: doc.keySet()){
					Document d = r.document(docID);
					String url = d.getFieldable("path").stringValue().replaceAll("%%","/");
					area.append(url+"\n");
					/*JTextPane pane = new JTextPane();
					pane.setContentType("text/html");
					pane.setEditable(false);

					pane.setPage("https://"+url+"l");
					add(pane);*/
				}
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println(e1.getMessage());
			}
			
		}
		
	}
	
}