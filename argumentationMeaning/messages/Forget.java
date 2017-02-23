package messages;

import enumerators.Performative;
import semiotic_elements.Sign;

public class Forget extends Message{

	public Forget(Sign sign) {
		this.type = Performative.Forget;
		this.sign = sign.toString();
	}

}
