package messages;

import enumerators.Performative;

public abstract class Message {
	
	protected String sign;
	protected Object element;
	protected Performative type;
	
	public String getSign(){
		return this.sign;
	}
	
	public Performative readPerformative(){
		return this.type;
	}
	
	public Object getElement(){
		return this.element;
	}
}
