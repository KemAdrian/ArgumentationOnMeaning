package messages;

import java.util.Set;

import enumerators.Performative;
import semiotic_elements.Generalization;
import semiotic_elements.Sign;

public class Assert extends Message{

	public Assert(Sign sign, Set<Generalization> e) {
		this.type = Performative.Assert;
		this.sign = sign.toString();
		this.element = e;
	}

}
