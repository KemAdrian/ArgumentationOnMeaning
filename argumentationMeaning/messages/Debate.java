package messages;

import enumerators.Agreement;
import enumerators.Performative;

public class Debate extends Message {
	
	public Debate(String sign, Agreement a) {
		this.type = Performative.Debate;
		this.sign = sign;
		this.element = a;
	}

}
