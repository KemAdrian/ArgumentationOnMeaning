package messages;

import enumerators.Performative;
import semiotic_elements.Sign;
import tools.Pair;

public class Elect extends Message{
		public Sign s2;
		public Integer i;

	public Elect(Sign sign, Pair<Sign,Integer> p) {
		this.type = Performative.Elect;
		this.sign = sign.toString();
		this.element = p;
		this.s2 = p.getLeft();
		this.i = p.getRight();
	}
	
	public String getOldSign(){
		return s2.toString();
	}
	
	public Integer getVote(){
		return i;
	}

}
