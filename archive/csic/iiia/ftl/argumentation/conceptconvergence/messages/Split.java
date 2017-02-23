package csic.iiia.ftl.argumentation.conceptconvergence.messages;

import java.util.HashMap;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Performative;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Message;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Sign;
import csic.iiia.ftl.learning.core.RuleHypothesis;

public class Split extends Message{
	
	private HashMap<Sign, RuleHypothesis> map;
	
	public Split(Agent from, Agent interlocutor, HashMap<Sign, RuleHypothesis> map){
		this.type = Performative.m_split;
		this.from = from;
		this.to = interlocutor;
		this.map = map;
	}
	
	public HashMap<Sign, RuleHypothesis> getMap(){
		return this.map;
	}

}
