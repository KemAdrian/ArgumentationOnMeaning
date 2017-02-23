package csic.iiia.ftl.argumentation.conceptconvergence.messages;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Performative;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Message;

public class Universal extends Message{
	
	private Object object_1;
	private Object object_2;
	
	public Universal(Agent from, Agent to, Object o1, Object o2){
		this.type = Performative.m_test;
		this.from = from;
		this.to = to;
		this.object_1 = o1;
		this.object_2 = o2;
	}
	
	public Object read_object_1(){
		return object_1;
	}
	
	public Object read_object_2(){
		return object_2;
	}

}
