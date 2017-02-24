package semiotic_elements;

import java.util.HashSet;
import java.util.Set;

import containers.ContrastSet;
import interfaces.Container;
import interfaces.SemioticElement;

/**
 * {@link Sign}s are the {@link SemioticElement} that represent the symbols used in the communication.
 * 
 * @author kemoadrian
 *
 */
public class Sign implements SemioticElement{
	
	public String piece;
	public String cake;

	/**
	 * Creates a new {@link Sign} from two {@link String}s.
	 * @param c the {@link String} refering to the {@link ContrastSet} from which the {@link Concept} of this {@link Sign} belongs.
	 * @param p the individual name of this {@link Concept} as a {@link String}.
	 */
	public Sign(String c, String p){
		this.piece =  new String(p);
		this.cake = new String(c);
	}
	
	/**
	 * Creates a new {@link Sign} from a single {@link String}.
	 * @param pieceandcake the {@link String} refering to the {@link ContrastSet} from which the {@link Concept} of this {@link Sign} belongs followed by the individual name of this {@link Concept} as a {@link String}, separated by a colon.
	 */
	public Sign(String pieceandcake){
		String[] parts = pieceandcake.split(":");
		if(parts.length >1){
			this.cake = parts[0];
			this.piece = parts[1];
		}
	}
	
	/* (non-Javadoc)
	 * @see interfaces.SemioticElement#getExtension(interfaces.Container)
	 */
	public Set<Example> getExtension(Container c){
		Set<Example> o = new HashSet<>();
		for(Concept cp : c.getAllConcepts()){
			if(cp.sign().equals(this.toString()))
				o.addAll(cp.extensional_definition());
		}
		return o;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return this.cake+':'+this.piece;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Sign clone(){
		return new Sign(this.cake, this.piece);
	}

}
