package csic.iiia.ftl.argumentation.conceptconvergence.messages;

import csic.iiia.ftl.argumentation.agents.Agent_v1;
import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Performative;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Message;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Sign;
import csic.iiia.ftl.base.core.FeatureTerm;

public class Assert extends Message{
	
	private Sign sign;
	private FeatureTerm example;
	
	public Assert(Agent_v1 from, Agent interlocutor, Sign s, FeatureTerm e){
		this.type = Performative.m_assert;
		this.from = from;
		this.to = interlocutor;
		this.sign = s;
		this.example = e;
	}
	
	public Sign read_sign(){
		return this.sign;
	}
	
	public FeatureTerm read_example(){
		return this.example;
	}

}
