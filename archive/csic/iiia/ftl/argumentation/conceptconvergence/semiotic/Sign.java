package csic.iiia.ftl.argumentation.conceptconvergence.semiotic;

import csic.iiia.ftl.base.core.FeatureTerm;

public class Sign {
	
	private String sign;
	private FeatureTerm symbol;
	private Concept concept;
	
	public Sign(String s, FeatureTerm symbol){
		this.sign = s;
		this.symbol = symbol;
	}

	
	public String sign(){
		return sign;
	}
	
	
	public FeatureTerm symbol(){
		return this.symbol;
	}
	
	public int associate(Concept c){
		if(c == null){
			System.out.println("WARNING: concept not initialized");
		}
		if(concept != null){
			System.out.println("WARNING: concept already associated");
		}
		else{
			this.concept = c;
		}
		return 0;
	}
	
	public boolean changeSign(String s, FeatureTerm symbol){
		this.sign = s;
		this.symbol = symbol;
		return true;
		
	}public boolean changeSign(Sign s){
		this.sign = s.sign();
		this.symbol = s.symbol();
		return true;
	}
}
