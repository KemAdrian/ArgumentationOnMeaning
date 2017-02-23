package semiotic_elements;

import java.util.HashSet;
import java.util.Set;

import interfaces.Container;
import interfaces.SemioticElement;

public class Sign implements SemioticElement{
	
	public String piece;
	public String cake;

	public Sign(String c, String p){
		this.piece =  new String(p);
		this.cake = new String(c);
	}
	
	public Sign(String pieceandcake){
		String[] parts = pieceandcake.split(":");
		if(parts.length >1){
			this.cake = parts[0];
			this.piece = parts[1];
		}
	}
	
	public Set<Example> getExtension(Container c){
		Set<Example> o = new HashSet<>();
		for(Concept cp : c.getAllConcepts()){
			if(cp.sign().equals(this.toString()))
				o.addAll(cp.extensional_definition());
		}
		return o;
	}
	
	public String toString(){
		return this.cake+':'+this.piece;
	}
	
	public Sign clone(){
		return new Sign(this.cake, this.piece);
	}

}
