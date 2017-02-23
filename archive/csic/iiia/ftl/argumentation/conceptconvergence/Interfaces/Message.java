package csic.iiia.ftl.argumentation.conceptconvergence.Interfaces;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Performative;

public abstract class Message {
	
	protected Performative type;
	protected Agent from, to;
	
	public int send(){
		to.get(this);
		return 0;
	}
	
	public Performative read_type(){
		return this.type;
	}
	
	public Agent read_sender(){
		return this.from;
	}
	
}
