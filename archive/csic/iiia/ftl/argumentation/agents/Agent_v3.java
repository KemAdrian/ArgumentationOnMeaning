package csic.iiia.ftl.argumentation.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Agreement;
import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Performative;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Message;
import csic.iiia.ftl.argumentation.conceptconvergence.messages.Agree;
import csic.iiia.ftl.argumentation.conceptconvergence.messages.Intensional;
import csic.iiia.ftl.argumentation.conceptconvergence.messages.Split;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Concept;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Sign;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.HashSetRadomizable;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.SetCast;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Sort;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.RuleHypothesis;

public class Agent_v3 extends Thread implements Agent{
	
	// Class Variables
	private static Map<AtomicInteger, Agent> AGENT_DIRECTORY = new HashMap<AtomicInteger, Agent>();
	private static AtomicInteger AGENT_COUNTER = new AtomicInteger(1);
	private static AtomicInteger TOKEN = new AtomicInteger(0);
	
	// Argumentations Phases
	private final static int PHASE_AVAILABLE = 0;
	private final static int PHASE_EDEF_AGREEMENT = 1;
	private final static int PHASE_VALID_EXCLUSION = 2;
	
	// Agent Variables
	private int phase;
	private String nickname;
	private AtomicInteger id;
	private AtomicInteger speaker;
	private boolean terminate;
	
	// Semantic Variables
	int newSignCounter;
	LearningPackage ln;
	HashMap<String,Concept> contrast_set;
	HashMap<String,Concept> new_proposal;
	HashSetRadomizable<FeatureTerm> Context;
	HashSetRadomizable<FeatureTerm> ToDebate;
	
	
	// Mailbox
	ArrayList<Message> ToRead; 
	ArrayList<Message> ToSend;
	
	
	public Agent_v3(){
		
		// Initialization of Agent Variables
		this.phase = PHASE_AVAILABLE;
		this.nickname = "agent-"+Agent_v3.AGENT_COUNTER;
		this.id = new AtomicInteger(Agent_v3.AGENT_COUNTER.intValue());
		this.terminate = false;
		
		// Doing additional stuff
		Agent_v3.AGENT_COUNTER.incrementAndGet();
		Agent_v3.AGENT_DIRECTORY.put(this.id, this);
		
		// Initialization of Semantic Variables
		this.contrast_set = new HashMap<String, Concept>();
	}
	
	public Agent_v3(String nickname){
		this.nickname = nickname;
		this.phase = PHASE_AVAILABLE;
		this.id = new AtomicInteger(Agent_v3.AGENT_COUNTER.intValue());
		this.terminate = false;
		
		// Doing additional stuff
		Agent_v3.AGENT_COUNTER.incrementAndGet();
		Agent_v3.AGENT_DIRECTORY.put(this.id, this);;
		
		// Initialization of Semantic Variables
		this.contrast_set = new HashMap<String, Concept>();
	}
	
	public static Agent getAgent(AtomicInteger ID){
		return Agent_v3.AGENT_DIRECTORY.get(ID);
		
	}
	
	public void run(){
		try{
			while(this.terminate == false){
				if(this.hasToken()){
					if(this.phase == Agent_v3.PHASE_AVAILABLE){
						this.available();
					}
					else if(this.phase == Agent_v3.PHASE_EDEF_AGREEMENT){
						this.E_agreement();
					}
					else if(this.phase == Agent_v3.PHASE_VALID_EXCLUSION){
						//this.V_exlusion();
					}
					else{
						System.out.println("Nothing to do, "+this.Call()+" terminates");
						this.terminate = true;
					}
					for(Message m : this.ToSend){
						this.send_message(m);
					}
				}
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}

	// Get Agent informations	
	
	// Get Agent's name
	public String Call(){
		return this.nickname;
	}
	
	// Get Agent's ID number
	public AtomicInteger Identify(){
		return this.id;
	}
	
	// Check if the Agent has the token
	public synchronized boolean hasToken(){
		return (this.id == Agent_v3.TOKEN);
	}
	
	// The Agent gives the Token to its speaker if it has the Token
	public synchronized boolean give_token() {
		if(this.hasToken()){
			Agent_v3.TOKEN = this.speaker;
			return true;
		}
		return false;
	}
	
	// The Agent gives the Token to the speaker with the ID passed as a parameter
	public synchronized boolean giveToken(AtomicInteger speaker){
		if(this.hasToken()){
			Agent_v3.TOKEN = speaker;
			return true;
		}
		return false;
	}
	
	// The Agent receives the Token if nobobody has the Token already
	public synchronized boolean getToken(){
		if(Agent_v3.TOKEN.intValue() == 0){
			Agent_v3.TOKEN = this.id;
			return true;
		}
		return false;
	}
	
	// The Agent's speakers becomes the one with the ID passed as a parameter
	public boolean talkTo(AtomicInteger ID){
		if(ID.intValue() != 0){
			this.speaker = ID;
			return true;
		}
		return false;
	}
	
	// The Agent is learning a new contrast set from a set of feature terms
	public boolean Learn(Set<FeatureTerm> training_set, LearningPackage infos) throws Exception{
		this.ln = infos;
		for(FeatureTerm f : infos.different_solutions()){
			Sign s = new Sign(f.toStringNOOS(infos.dm()), f);
			Concept c = new Concept(s, infos);
			Set<FeatureTerm> training = SetCast.duplicate(training_set, infos.ontology());
			c.intensionalDefinition().learn(training);
			c.extensionalDefinition().creates_from_intensional(SetCast.duplicate(training_set, infos.ontology()));
			Context.addAll(c.extensionalDefinition().get_examples());
			contrast_set.put(s.sign(),c);
		}
		return true;
	}
	
	// The Agent is resolving its available phase
	private boolean available(){
		this.newSignCounter = 0;
		this.new_proposal = new HashMap<String, Concept>();
		this.ToDebate = new HashSetRadomizable<FeatureTerm>();
		for(Concept c : contrast_set.values()){
			ToDebate.addAll(c.extensionalDefinition().get_examples());
		}
		this.phase = Agent_v3.PHASE_EDEF_AGREEMENT;
		return true;
	}
	
	// The Agent is resolving its E_agreement phase
	private boolean E_agreement() throws FeatureTermException{
		
		// Adding the agreed definitions and deleting the agree messages
		ArrayList<Message> to_keep = new ArrayList<Message>();
		for(Message m : this.ToRead){
			if(m.read_type() == Performative.m_agree){
				Agree a = (Agree) m;
				Concept c = new Concept(a.read_sign(), ln);
				c.intensionalDefinition().set(a.read_example());
				c.extensionalDefinition().creates_from_intensional(ToDebate,a.read_sign().symbol());
				ToDebate.remove(c.extensionalDefinition().get_examples());
				this.new_proposal.put(a.read_sign().sign(),c);
			}
			else{
				to_keep.add(m);
			}
		}
		this.ToRead = to_keep;
		
		// If the message box didn't contain other things than agreements, let's start an argumentation
		if(ToRead.isEmpty() && !ToDebate.isEmpty()){
			FeatureTerm f = ToDebate.getRandomElement();
			Concept concept = null;
			for(Concept c : contrast_set.values()){
				if(c.intensionalDefinition().is_covering(f)){
					concept = c;
				}
			}
			if(concept == null){
				System.out.println("No concept is coverin the example from the extensional definition");
				return false;
			}
			Sort s = new Sort(concept.getInfos().solution_path().features.get(0).get(), null, concept.getInfos().ontology());
			FeatureTerm nf = new TermFeatureTerm("solution-custom-"+newSignCounter, s);
			Intensional m = new Intensional(this, Agent_v3.getAgent(speaker), new Sign("solution-custom-"+newSignCounter, nf), concept.intensionalDefinition().getAllGeneralizations());
			this.ToSend.add(m);
		}
		
		// If it contained any other messages, read them and do the proper thing
		else if(!ToRead.isEmpty()){
			this.newSignCounter += ToRead.size();
			for(Message m : ToRead){
				// If it's a Intensional
				if(m.read_type() == Performative.m_intensional){
					Intensional i = (Intensional) m;
					Set<Message> answer = checkProposal(i.read_sign(), i.read_rule());
					ToSend.addAll(answer);
				}
				// If it's a Split
				if(m.read_type() == Performative.m_split){
					Split sp = (Split) m;
					for(Sign s : sp.getMap().keySet()){
						Set<Message> answer = checkProposal(s,  sp.getMap().get(s));
						ToSend.addAll(answer);
					}
					
				}
			}
		}
		
		// If there is nothing more to debate about
		if(ToDebate.isEmpty()){
			this.phase = Agent_v3.PHASE_VALID_EXCLUSION;
		}
		
		return true;
	}
	
	// Agent's speaker is now the Agent passed as a parameter
	public boolean meet(Agent A){
		this.speaker = ((Agent_v3) A).Identify();
		return true;
	}
	
	// Add the message mail to this Agent's mailbox
	public boolean get(Message mail) {
		this.ToRead.add(mail);
		return true;
	}
	
	// Deprecated
	public boolean read_message(Message mail) throws FeatureTermException {
		System.out.println("On Agent_v3, the read_message method is not used. Please do not invoke it");
		return false;
	}

	// Send a message mail to this Agent's speaker
	public boolean send_message(Message mail) {
		Agent_v3.getAgent(speaker).get(mail);
		return false;
	}
	
	// Take a sign and an intensional definition and check if it should agree or change it by spliting. Returns the proper message to send back at the speaker.
	private Set<Message> checkProposal(Sign sign, RuleHypothesis definition) throws FeatureTermException{
		Set<Message> output = new HashSet<Message>();
		Set<FeatureTerm> solutions = new HashSet<FeatureTerm>();
		Set<FeatureTerm> examples = new HashSet<FeatureTerm>();
		// Find how many concepts have examples subsumed by this extensional definition
		for(FeatureTerm e : ToDebate){
			if(definition.coveredByAnyRule(e) != null){
				if(solutions.add(e.featureValue(ln.solution_path().features.get(0)))){
					examples.add(e);
				}
			}
		}
		
		// For 0 or one, we are in the case of a new sign (0), a synonym or a  hyponym (1) - we accept the definition without saying anything about the sign
		if(solutions.size() < 2){
			output.add(new Agree(this, Agent_v3.getAgent(speaker), sign, definition)); 
			Concept c = new Concept(sign, ln);
			c.intensionalDefinition().set(definition);
			c.extensionalDefinition().creates_from_intensional(SetCast.duplicate(ToDebate,ln.ontology()),sign.symbol());
			ToDebate.remove(c.extensionalDefinition().get_examples());
			this.new_proposal.put(sign.sign(),c);
			// In the case of an hyponym, we create a new concept for the remaining elements of the extentional definition
			for(FeatureTerm solution : solutions){
				Set<FeatureTerm> leftovers = SetCast.duplicate(contrast_set.get(solution).extensionalDefinition().get_examples(), ln.ontology());
				leftovers.removeAll(c.extensionalDefinition().get_examples());
			}
		}
		
		// For more than 1, we are in the case of a hypernym - we propose a new partition of this extensional definition with new hypothesis
		if(solutions.size() > 1){
			HashMap<Sign, RuleHypothesis> map = new HashMap<Sign, RuleHypothesis>();
			Set<FeatureTerm> already_covered = new HashSet<FeatureTerm>();
			for(Concept c : new_proposal.values()){
				already_covered.addAll(c.extensionalDefinition().get_examples());
			}
			for(FeatureTerm e : examples){
				for(Concept c : contrast_set.values()){
					if(c.intensionalDefinition().is_covering(e)){
						
					}
				}
			}
			output.add(new Split(this, Agent_v3.getAgent(speaker), map));
		}
		return output;
	}

	public void end() {
		this.terminate = true;
		
	}

	public void kill() {
		this.terminate = true;	
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
