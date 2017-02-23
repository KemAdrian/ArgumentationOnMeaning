package csic.iiia.ftl.argumentation.conceptconvergence.messages;

import csic.iiia.ftl.argumentation.agents.Agent_v1;
import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Performative;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Message;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Sign;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.learning.core.Rule;

public class Answer extends Message{
	
	private Sign sign;
	private Rule generalization;
	private FeatureTerm example;
	
	public Answer(Agent_v1 from, Agent interlocutor, Sign s, Rule g, FeatureTerm e){
		this.type = Performative.m_answer;
		this.from = from;
		this.to = interlocutor;
		this.sign = s;
		this.generalization = g;
		this.example = e;
	}
	
	public Sign read_sign(){
		return this.sign;
	}
	
	public Rule read_generalization(){
		return this.generalization;
	}
	
	public FeatureTerm read_example(){
		return this.example;
	}

}
