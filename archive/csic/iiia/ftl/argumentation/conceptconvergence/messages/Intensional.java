package csic.iiia.ftl.argumentation.conceptconvergence.messages;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Performative;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Message;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Sign;
import csic.iiia.ftl.learning.core.RuleHypothesis;

public class Intensional extends Message{
	
	private Sign sign;
	private RuleHypothesis intensional;
	
	public Intensional(Agent from, Agent interlocutor, Sign s, RuleHypothesis i){
		this.type = Performative.m_intensional;
		this.from = from;
		this.to = interlocutor;
		this.sign = s;
		this.intensional = i;
	}
	
	public Sign read_sign(){
		return this.sign;
	}
	
	public RuleHypothesis read_rule(){
		return this.intensional;
	}

}
