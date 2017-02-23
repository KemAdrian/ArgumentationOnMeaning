package csic.iiia.ftl.argumentation.conceptconvergence.tools;

import csic.iiia.ftl.argumentation.agents.Agent_v1;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;

public class Token {
	
	private Agent owner;
	
	public Token(){
		this.owner = null;
	}
	
	public Token(Agent a){
		this.owner = a;
	}
	
	public boolean is_owned(Agent agent_v2){
		if(this.owner == null || agent_v2 == null)
			return false;
		return(agent_v2.equals(owner));
	}
	
	public int gives_to(Agent interlocutor){
		if(Agent_v1.DEBUG > 0){
			System.out.println("Token given to "+ interlocutor.toString()+"\n");
		}
		this.owner = interlocutor;
		return 0;
	}
	
	public int remove_from(Agent a){
		if(this.owner == null || a == null)
			return 1;
		if(a.equals(this.owner))
			this.owner = null;
		return 0;
	}

}
