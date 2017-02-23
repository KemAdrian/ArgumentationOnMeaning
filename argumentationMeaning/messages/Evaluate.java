package messages;

import enumerators.Agreement;
import enumerators.Performative;

public class Evaluate extends Message {
	
	public Evaluate(String sign, Agreement a) {
		this.type = Performative.Evaluate;
		this.sign = sign;
		this.element = a;
	}

}
