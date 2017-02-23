package csic.iiia.ftl.argumentation.conceptconvergence.messages;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Performative;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Message;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Sign;
import csic.iiia.ftl.learning.core.RuleHypothesis;

public class Agree extends Message{
	
	private Sign sign;
	private RuleHypothesis intensional;
	
	public Agree(Agent from, Agent interlocutor, Sign s, RuleHypothesis i){
		this.type = Performative.m_agree;
		this.from = from;
		this.to = interlocutor;
		this.sign = s;
		this.intensional = i;
	}
	
	public Sign read_sign(){
		return this.sign;
	}
	
	public RuleHypothesis read_example(){
		return this.intensional;
	}

}
