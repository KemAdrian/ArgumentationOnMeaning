package csic.iiia.ftl.argumentation.conceptconvergence.semiotic;

import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;

public class Context {
	
	private Agent agent;
	
	public Context(Agent interlocutor){
		this.agent = interlocutor;
	}
	
	public Agent is(){
		return this.agent;
	}

}
