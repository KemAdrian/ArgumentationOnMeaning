package csic.iiia.ftl.argumentation.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Agreement;
import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Performative;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Message;
import csic.iiia.ftl.argumentation.conceptconvergence.messages.Accept;
import csic.iiia.ftl.argumentation.conceptconvergence.messages.Answer;
import csic.iiia.ftl.argumentation.conceptconvergence.messages.Ask;
import csic.iiia.ftl.argumentation.conceptconvergence.messages.Assert;
import csic.iiia.ftl.argumentation.conceptconvergence.messages.Refuse;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Concept;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Context;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.ContrastSet;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Sign;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.Token;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.RuleHypothesis;


public class Agent_v1 extends Thread implements Agent{
	
	public static int DEBUG = 0;
	
	private volatile Token token;
	
	private boolean satisfied;
	private boolean in_game;
	private boolean can_ask;
	private boolean kill;
	
	private List<FeatureTerm> new_problems;
	private Set<FeatureTerm> pending_problems;
	
	private LinkedList<Message> mailbox;
	private LinkedList<Message> sendbox;
	
	private HashMap<Context, ContrastSet> contrast_sets;
	
	private Thread thread;
	private String nickname;
	private Agent interlocutor;
	private Context context;
	
	public Agent_v1(String nickname, Set<FeatureTerm> initial_training, LearningPackage infos, Token t) throws Exception{
		
		this.new_problems = new ArrayList<FeatureTerm>();
		this.pending_problems = new HashSet<FeatureTerm>();
		this.mailbox = new LinkedList<Message>();
		this.sendbox= new LinkedList<Message>();
		
		this.satisfied = true;
		this.in_game = false;
		this.can_ask = true;
		this.kill = false;
		
		this.interlocutor = null;
		this.context = new Context(this);
		
		this.contrast_sets = new HashMap<Context, ContrastSet>();
		this.contrast_sets.put(this.context, new ContrastSet());
		
		this.nickname = nickname;
		this.token = t;
		
		if(Agent_v1.DEBUG > 0){
			System.out.println("Agent "+this.nickname+" is created");
		}
		
		// Initialization of the different concepts
		for(FeatureTerm s : infos.different_solutions()){
			Concept c = new Concept(new Sign(s.toStringNOOS(infos.dm()), s), infos);
			if(c.intensionalDefinition().learn(initial_training) < 1){
				c.extensionalDefinition().creates_from_intensional(initial_training);
				
				if(Agent_v1.DEBUG > 1 && Concept.DEBUG == true){
					System.out.println("with the following rules:");
					c.intensionalDefinition().tell_rules();
					System.out.println("and "+c.extensionalDefinition().get_examples().size()+" examples");
				}
				
				this.contrast_sets.get(this.context).add(c);
			}
		}
		
	}
	
	public String toString(){
		return this.nickname;
	}
	
	public boolean meet(Agent a){
		
		this.interlocutor = a;
		
		if(Agent_v1.DEBUG > 0){
			System.out.println(this.toString() + " met "+a.toString());
		}
		
		return true;
	}
	
	public boolean get(Message mail){
		mailbox.add(mail);
		
		if(Agent_v1.DEBUG > 0){
			System.out.println(this.toString() + " received a message");
		}
		
		return true;
	}
	
	public int isPresented(FeatureTerm e){
		if(Agent_v1.DEBUG > 0){
			System.out.println(this.toString() + " is presented a new exemple");
		}
		this.satisfied = false;
		this.in_game = true;
		this.new_problems.add(e);
		if(!this.isAlive()){
			this.start();
		}
		return 0;
		
	}
	
	public void run(){
		
		//Infinite Loop of the Agent
		while(kill == false){
			
			if(!this.satisfied || this.in_game){
			
				// When we have the Token
				if(this.token.is_owned(this)){
					
					// Send assert for all new problems
					while(!new_problems.isEmpty()){
						try {
							Assert message =new Assert(this, interlocutor, this.give_current_cset().categorize(new_problems.get(0)), new_problems.get(0));
							pending_problems.add(new_problems.remove(0));
							message.send();
							
							if(Agent_v1.DEBUG > 0){
								System.out.println("there is a new problem, "+this.toString()+" sends an Assert to "+this.interlocutor.toString());
							}
							
						} catch (FeatureTermException e) {
							e.printStackTrace();
						}
					}
					
					// Check mailbox
					while(!mailbox.isEmpty()){
						
						try {
							read_message(mailbox.removeFirst());
						} catch (FeatureTermException e) {
							e.printStackTrace();
						}
						
					}
			
					// Check messages to send
					for(Message m : sendbox){
						send_message(m);
					}
				
					// Check if satisfied
					if(new_problems.isEmpty() && pending_problems.isEmpty()){
						this.end();
						System.out.println(this.toString()+" can terminate");
					}
					
					System.out.print(this.toString()+" knows: ");
					for(String c : this.give_current_cset().list_known_concepts()){
						System.out.print(" "+c);
					}
					System.out.println("\n");
					
					this.give_token();
				}
			}
			
			else{
				//System.out.println(this.toString()+" is sleeping");
				if(this.token.is_owned(this)){
					this.token.remove_from(this);
					System.out.println(this.toString()+" remove the token");
				}
				try {
					sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean read_message(Message mail) throws FeatureTermException{
		
		if(Agent_v1.DEBUG > 0){
			System.out.println(this.toString() + " is reading a message, it's a "+mail.read_type());
		}
		
		if(mail.read_type() == Performative.m_assert){
			Assert message = (Assert) mail;
			
			if(message.read_sign().sign().equals(this.give_current_cset().categorize(message.read_example()).sign())){
				if(Agent_v1.DEBUG  > 0){
					System.out.println(this.toString() + " Its sign is "+message.read_sign().sign()+" and its matching");
				}
				Accept accept = new Accept(this, interlocutor, message.read_sign(), message.read_example());
				sendbox.add(accept);
				pending_problems.remove(message.read_example());
			}
			
			else{
				if(Agent_v1.DEBUG  > 0){
					System.out.println(this.toString() + " Its sign is "+message.read_sign().sign()+" but it should have been "+this.give_current_cset().categorize(message.read_example()).sign());
				}
				this.satisfied = false;
				Ask ask = new Ask(this, interlocutor, message.read_sign(), message.read_example());
				sendbox.add(ask);
			}
			
			return true;
			
		}
		
		if(mail.read_type() == Performative.m_accept){
			Accept message = (Accept) mail;
			
			if(new_problems.remove(message.read_example()) || pending_problems.remove(message.read_example())){
				Accept accept = new Accept(this, interlocutor, message.read_sign(), message.read_example());
				sendbox.add(accept);
			}
			System.out.println(this.toString()+" is satisfied");
			this.satisfied = true;
			
			return true;
		}
		
		if(mail.read_type() == Performative.m_refuse){
			Refuse message = (Refuse) mail;
			
			if(new_problems.contains(message.read_example()) || pending_problems.contains(message.read_example())){
				this.can_ask = true;
			}
			
			return true;
			
		}
		
		if(mail.read_type() == Performative.m_ask){
			Ask message = (Ask) mail;
			
			this.can_ask = false;
			Answer answer = new Answer(this, interlocutor, message.read_sign(), this.give_current_cset().get_generalization(message.read_sign(), message.read_example()), message.read_example());
			sendbox.add(answer);
			
			if(Agent_v1.DEBUG  > 0){
				System.out.println(this.toString() + " is asked the meaning of "+message.read_sign().sign());
				System.out.println(this.toString() + " we send him an answer "+answer.read_generalization().solution.toStringNOOS());
			}
			
			return true;
		}
		
		if(mail.read_type() == Performative.m_answer){
			Answer message = (Answer) mail;
			
			if(Agent_v1.DEBUG  > 0){
				System.out.println(this.toString() + " received the meaning of "+message.read_sign().sign());
			}
			
			if(update_csets(message)){
				Accept accept = new Accept(this, interlocutor, message.read_sign(), message.read_example());
				
				if(Agent_v1.DEBUG  > 0){
					System.out.println(this.toString() + " accept it");
				}
				
				sendbox.add(accept);
				pending_problems.remove(message.read_example());
				
			}
			
			else{
				Refuse refuse = new Refuse(this, interlocutor, message.read_sign(), message.read_example());
				sendbox.add(refuse);
				if(Agent_v1.DEBUG  > 0){
					System.out.println(this.toString() + " refuse it");
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean send_message(Message mail){
		
		if(mail.read_type() != Performative.m_ask || this.can_ask == true){
		
			if(Agent_v1.DEBUG > 0){
				System.out.println(this.toString() + " is sending a message ("+mail.read_type()+")");
			}
		
			mail.send();
			sendbox.remove(mail);
			return true;
		}
		
		return false;
	}
	
	private boolean update_csets(Answer a) throws FeatureTermException{
		
		Set<Concept> concepts_attacked = new HashSet<Concept>();
		Set<Concept> concepts_to_add = new HashSet<Concept>();
		Set<Concept> concepts_to_del = new HashSet<Concept>();
		Set<Concept> concepts_to_chg = new HashSet<Concept>();
		RuleHypothesis new_intensional = new RuleHypothesis();
		new_intensional.addRule(a.read_generalization());
		boolean new_contrastset = false;

		
		Context ctx = null;
		for(Context c : this.contrast_sets.keySet()){
			if(c.is().equals(interlocutor)){
				ctx = c;
			}
		}
		
		if(ctx == null){
			new_contrastset = true;
			ctx = new Context(interlocutor);
			contrast_sets.put(ctx, new ContrastSet(ctx));
			System.out.println(this.toString()+" is creating a new contrast set");
		}
		
		
		concepts_attacked = this.contrast_sets.get(context).get_attacked(new_intensional);
		if(concepts_attacked.size() != 1){
			if(Agent_v1.DEBUG  > 0){
				System.out.println(this.toString() + " There is "+concepts_attacked.size()+" to modify");
			}
			return false;
		}
		
		Concept c = null;
		for(Concept i : concepts_attacked){
			c = i;
		}
		
		System.out.println(c.sign().sign()+" needs to be modified");
		
		FeatureTerm solution = a.read_sign().symbol();
		c.extend_solutions(solution);
		
		Concept c_1 = new Concept(new Sign(a.read_sign().sign(), a.read_sign().symbol()),c.extensionalDefinition().get_informations());
		
		for(FeatureTerm e : c.extensionalDefinition().get_examples()){
			if(new_intensional.coveredByAnyRule(e.featureValue(c.getInfos().description_path().features.get(0))) != null){
				c_1.extensionalDefinition().add_example(e,solution);
			}
		}
		
		System.out.println("extensional definition size of the old concept "+c.extensionalDefinition().get_examples().size());
		System.out.println("extensional definition size of the new concept "+c_1.extensionalDefinition().get_examples().size());
		//System.out.println(c.intensionalDefinition().tell_rules());
		
		if(c.extensionalDefinition().get_examples().size() == c_1.extensionalDefinition().get_examples().size()){
			c_1.intensionalDefinition().set(c.intensionalDefinition().getAllGeneralizations());
			concepts_to_add.add(c_1);
			System.out.println("It's a synonym!");
		}
		
		else{
			System.out.println("It's a hyponym");
			c_1.intensionalDefinition().set(new_intensional);
			
			Concept c_2 = new Concept(new Sign(c.sign().sign(), c.sign().symbol()), c.extensionalDefinition().get_informations());
			c_2.extend_solutions(solution);
			for(FeatureTerm e : c.extensionalDefinition().get_examples()){
				if(new_intensional.coveredByAnyRule(e.featureValue(c.getInfos().description_path().features.get(0))) == null){
					c_2.extensionalDefinition().add_example(e);
				}
			}
			
			concepts_to_add.add(c_1);
			concepts_to_add.add(c_2);
			concepts_to_chg.add(c_2);
			
		}
		
		if(!new_contrastset){
			concepts_to_del.add(c);
		}
		
		else{
			for(Concept i : this.contrast_sets.get(context).get_others(c)){
				i.extend_solutions(solution);
				concepts_to_add.add(i);
			}
		}
		
		
		this.context = ctx;
		for(Concept i : concepts_to_del){
			this.contrast_sets.get(context).forget(i);
		}
		for(Concept i : concepts_to_add){
			this.contrast_sets.get(context).add(i);
		}
		
		for(Concept i : concepts_to_chg){
			try {
				i.intensionalDefinition().learn(this.give_current_cset().all_examples());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		return true;
	}
	
	private ContrastSet give_current_cset(){
		return this.contrast_sets.get(this.context);
	}
	
	public boolean give_token(){
		if(this.token != null || this.token.is_owned(this)){
			this.token.gives_to(interlocutor);
		}
		return true;
	}
	
	public boolean is_satisfied(){
		return this.satisfied;
	}
	
	public void start(){
	    if (this.thread == null)
	    {
	        this.thread = new Thread (this, this.nickname);
	        this.thread.start ();
	    }
		this.in_game = true;
		this.can_ask = true;
	}
	
	public void end(){
		this.in_game = false;
		for(Message i : sendbox){
			if(i.read_type() == Performative.m_ask){
				sendbox.remove(i);
			}
		}
	}
	
	public void kill(){
		this.kill = true;
	}

	@Override
	public LearningPackage getLearningPackage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Agreement agree(FeatureTerm e, FeatureTerm e1) throws FeatureTermException {
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
	public Agreement agree(Collection<FeatureTerm> extensionalDefinition,
			Collection<FeatureTerm> extensionalDefinition2) throws FeatureTermException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isContext(Collection<FeatureTerm> c) throws FeatureTermException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Agreement agree(csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.Sign s1,
			csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.Sign s2) throws FeatureTermException {
		// TODO Auto-generated method stub
		return null;
	}


}
