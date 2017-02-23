package csic.iiia.ftl.argumentation.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Agreement;
import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Performative;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Message;
import csic.iiia.ftl.argumentation.conceptconvergence.messages.Universal;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.Sign;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.Token;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;

public class Agent_v2 extends Thread implements Agent{
	
	private volatile Token token;
	
	
	// Multiagent System variables
	private String nickname;
	private ArrayList<Message> mailbox;
	private ArrayList<Message> sendbox;
	private Agent interlocutor;
	
	// Semiotic variables
	private HashMap<Sign, Collection<FeatureTerm>> sign_to_examples;
	private HashMap<Sign, Collection<Sign>> contrast_sets;
	
	// Multithreading variables
	private Thread thread;	
	private boolean kill;
	
	public Agent_v2(String nickname, Token t){
		// Agent initialization
		this.nickname = nickname;
		this.mailbox = new ArrayList<Message>();
		this.sendbox = new ArrayList<Message>();
		this.interlocutor = null;
		
		// Semiotic initialization
		sign_to_examples = new HashMap<Sign, Collection<FeatureTerm>>();
		contrast_sets = new HashMap<Sign, Collection<Sign>>();
		
		// Thread initialization
		this.kill = false;
		this.token = t;
	}
	
	// Function agree "Basic"
	public Agreement agree(FeatureTerm x, FeatureTerm y) throws FeatureTermException{
		if(x == null || y == null){
			return Agreement.Uncorrect;
		}
		else if(x.equivalents(y)){
			return Agreement.True;
		}
		else return Agreement.Uncorrect;
	}
	
	// Function agree's first overload - two sets comparhison
	public Agreement agree(Collection<FeatureTerm> x, Collection<FeatureTerm> y) throws FeatureTermException{
		
		if(!(this.isSetofElements(x) && this.isSetofElements(y))){
			System.out.println("THE SET IS NOT FROM A VALID CONTEXT");
			return Agreement.Uncorrect;
		}
		
		Collection <FeatureTerm> a = new HashSet<FeatureTerm>(), b = new HashSet<FeatureTerm>();
		a.addAll(y);
		b.addAll(x);
		for(FeatureTerm e : y){
			for(FeatureTerm f : x){
				if(this.agree(e, f) == Agreement.True){
					a.remove(e);
					b.remove(f);
				}
			}
		}
		
		if(a.size() == 0 || b.size() == 0){
			if(a.size() == 0 && b.size() == 0){
				return Agreement.True;
			}
			return Agreement.Correct;
		}
		return Agreement.Uncorrect;
	}
	
	// Function agree's second overload - first part : one sign one set
	public Agreement agree(Sign s, Collection<FeatureTerm> E) throws FeatureTermException{
		
		if(this.sign_to_examples.containsKey(s)){
			return agree(this.sign_to_examples.get(s),E);
		}
		return Agreement.Uncorrect;
	}
	
	// Function agree's second overload - second part : two signs
	public Agreement agree(Sign s1, Sign s2) throws FeatureTermException{
		if(this.sign_to_examples.containsKey(s1) && (this.sign_to_examples.containsKey(s2))){
			return agree(this.sign_to_examples.get(s1),this.sign_to_examples.get(s2));
		}
		return Agreement.Uncorrect;
	}
	
	// Making a simple set to sign association (the sign is a grounded sign)
	public boolean associate_set_to_sign(Collection<FeatureTerm> E, Sign s){
		if(!(sign_to_examples.containsKey(s))){
			sign_to_examples.put(s, E);
			return true;
		}
		return false;
	}
	
	// Test if a set of elements can be a contrast set
	public boolean isContrastSet(Sign s, Collection<FeatureTerm> context) throws FeatureTermException{
		for(Sign si : contrast_sets.get(s)){
			if(!si.getCake().equals(s)){
				System.out.println("the sign "+si.toString()+" has an invalid label for the contrast set "+s);
				return false;
			}
			for(FeatureTerm e : context){
				Collection<FeatureTerm> E = new ArrayList<FeatureTerm>();
				E.add(e);
				if(this.agree(si, E) == Agreement.Uncorrect){
					return false;
				}
				for(Sign sj : contrast_sets.get(s)){
					if((this.agree(si, E) != Agreement.Uncorrect) && !si.equals(sj)){
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	// To test if a set respects the conditions of a Context
	public boolean isSetofElements(Collection<FeatureTerm> c) throws FeatureTermException{
		int i = 0;
		int j = 0;
		Collection<FeatureTerm> c2 = c;
		for(FeatureTerm e : c){
			for(FeatureTerm f : c2){
				if(i == j){
					if(this.agree(e,f) == Agreement.Uncorrect){
						return false;
					}
				}
				else{
					if(this.agree(e, f) == Agreement.True){
						return false;
					}
				}
				j++;
			}
			j = 0;
			i++;
		}
		return true;
	}
	
	public String present(){
		return this.nickname;
	}
	
	public boolean meet(Agent a) {
		this.interlocutor = a;
		return false;
	}

	public boolean get(Message mail) {
		mailbox.add(mail);
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean read_message(Message mail) throws FeatureTermException {
		if(mail.read_type() == Performative.m_test){
			this.interlocutor = mail.read_sender();
			Universal m = (Universal) mail;
			
			if(m.read_object_1() instanceof FeatureTerm && m.read_object_2() instanceof FeatureTerm){
				FeatureTerm e1 = (FeatureTerm) m.read_object_1();
				FeatureTerm e2 = (FeatureTerm) m.read_object_2();
				System.out.println(this.agree(e1, e2));
			}
			
			else if(m.read_object_1() instanceof Sign || m.read_object_2() instanceof Sign){
				if(m.read_object_1() instanceof Sign && m.read_object_2() instanceof Sign){
					Sign s1 = (Sign) m.read_object_1();
					Sign s2 = (Sign) m.read_object_2();
					System.out.println(this.agree(s1, s2));
				}
				else if(m.read_object_1() instanceof Sign){
					Sign s1 = (Sign) m.read_object_1();
					if(m.read_object_2() instanceof FeatureTerm){
						Collection<FeatureTerm> E1 = new HashSet<FeatureTerm>();
						FeatureTerm o1 = (FeatureTerm) m.read_object_2();
						E1.add(o1);
						System.out.println(this.agree(s1, E1));
					}
					else if(m.read_object_2() instanceof Collection){
						Collection<FeatureTerm> E1 = (Collection<FeatureTerm>) m.read_object_2();
						System.out.println(this.agree(s1, E1));
					}
				}
				else if(m.read_object_2() instanceof Sign){
					Sign s1 = (Sign) m.read_object_2();
					if(m.read_object_1() instanceof FeatureTerm){
						Collection<FeatureTerm> E1 = new HashSet<FeatureTerm>();
						FeatureTerm o1 = (FeatureTerm) m.read_object_1();
						E1.add(o1);
						System.out.println(this.agree(s1, E1));
					}
					else if(m.read_object_1() instanceof Collection){
						Collection<FeatureTerm> E1 = (Collection<FeatureTerm>) m.read_object_1();
						System.out.println(this.agree(s1, E1));
					}
				}
				
			}
			
			else if(m.read_object_1() instanceof Collection || m.read_object_2() instanceof Collection){
				Collection<FeatureTerm> E1 = new HashSet<FeatureTerm>() ,E2 = new HashSet<FeatureTerm>();
				if((m.read_object_1() instanceof FeatureTerm)){
					FeatureTerm o1 = (FeatureTerm) m.read_object_1();
					E1.add(o1);
				}
				else{
					E1 = (Collection<FeatureTerm>) m.read_object_1();
				}
				if((m.read_object_2() instanceof FeatureTerm)){
					FeatureTerm o2 = (FeatureTerm) m.read_object_2();
					E2.add(o2);
				}
				else {
					E2 = (Collection<FeatureTerm>) m.read_object_2();
				}
				System.out.println(this.agree(E1,E2));
			}
			
		}
		return false;
	}

	public boolean send_message(Message mail) {
		this.sendbox.add(mail);
		return false;
	}

	public boolean give_token() {
		if(this.token != null || this.token.is_owned(this)){
			this.token.gives_to(interlocutor);
			return true;
		}
		return false;
	}

	public void run() {
		while(this.kill == false){
			if(this.token.is_owned(this)){
				for(Message m : this.mailbox){
					try {
						read_message(m);
					} catch (FeatureTermException e) {
						System.out.println("Reading messages failed");
					}
				}
				mailbox.clear();
				for(Message m : sendbox){
					m.send();
				}
				sendbox.clear();
			}
		}
		
	}

	public void start() {
	    if (this.thread == null)
	    {
	        this.thread = new Thread (this, this.nickname);
	        this.thread.start ();
	    }
		
	}

	public void end() {
		
	}

	public void kill() {
		this.kill = true;
	}

	@Override
	public LearningPackage getLearningPackage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Agreement agree(FeatureTerm featureValue, Collection<FeatureTerm> extensionalDefinition)
			throws FeatureTermException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isContext(Collection<FeatureTerm> c) throws FeatureTermException {
		// TODO Auto-generated method stub
		return false;
	}
	
	

}
