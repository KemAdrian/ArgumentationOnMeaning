package csic.iiia.ftl.argumentation.conceptconvergence.tools;

import java.util.HashSet;
import java.util.Random;

public class HashSetRadomizable<E> extends HashSet<E>{
	
	private static final long serialVersionUID = 1L;

	public HashSetRadomizable(){
		super();
	}
	
	public E getRandomElement(){
		int size = this.size();
		int item = new Random().nextInt(size);
		int i = 0;
		for(E obj : this)
		{
		    if (i == item){
		    	return obj;
		    }
		    i ++;
		}
		return null;
	}
	
	public E removeRandomElement(){
		E out = null;
		int size = this.size();
		int item = new Random().nextInt(size);
		int i = 0;
		for(E obj : this)
		{
		    if (i == item){
		    	out = obj;
		    	break;
		    }
		    i ++;
		}
		this.remove(out);
		return out;
	}

}
