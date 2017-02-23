package csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2;

public class Sign {
	
	// A sign is pointing to a set of Signs (Intensional definition) and/or a set of Elements (Extensional definition) in a Context
	// A sign is composed of its contrast set (the cake it's from) and its symbol (the one piece of that cake it's referring to).
	
	private static int NEW_SIGN_NUMEROTATION = 0;
	
	private String piece;
	private String cake;
	
	public Sign(String c, String p){
		this.piece = p;
		this.cake = c;
	}
	
	public Sign(){
		this.piece = "custom_sign"+String.valueOf(NEW_SIGN_NUMEROTATION);
		this.cake = "buffer";
		Sign.NEW_SIGN_NUMEROTATION ++;
	}
	
	public String getPiece(){
		return this.piece;
	}
	
	public String getCake(){
		return cake;
	}
	
	public String toString(){
		return this.cake + "." + this.piece;
	}

}
