package messages;

import enumerators.Performative;

public class Agree extends Message {
	
	public Agree(String s1, String s2) {
		this.type = Performative.Evaluate;
		this.sign = s1;
		this.element = s2;
	}

}
