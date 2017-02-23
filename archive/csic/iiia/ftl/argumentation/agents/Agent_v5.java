package csic.iiia.ftl.argumentation.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Agreement;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Message;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.Concept;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.ContrastSet;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.Sign;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.RuleHypothesis;

public class Agent_v5 implements Agent{
	
	// Class Variables
	private static Map<AtomicInteger, Agent> AGENT_DIRECTORY = new ConcurrentHashMap<AtomicInteger, Agent>();
	private static AtomicInteger AGENT_COUNTER = new AtomicInteger(0);
	private static AtomicInteger TOKEN = new AtomicInteger(0);
	
	// Multiagent System variables
	private String nickname;
	private AtomicInteger ID;
	private AtomicInteger Interlocutor;
	private ArrayList<Message> mailbox;
	//private ArrayList<Message> sendbox;
	
	// Semiotic Variables
	private LearningPackage ln;
	private HashMap<String, ContrastSet> contrastSets;
	
	
	// Agent instanciation
	public Agent_v5(String nick){
		this.nickname = nick;
		this.ID = new AtomicInteger(Agent_v5.AGENT_COUNTER.incrementAndGet());
		this.Interlocutor = this.ID;
		this.mailbox = new ArrayList<Message>();
		//this.sendbox = new ArrayList<Message>();
		this.contrastSets = new HashMap<String, ContrastSet>();
		
		Agent_v5.AGENT_DIRECTORY.put(this.ID, this);
	}
	
	// Agent Global Methods
	public String uselesselyScreamHisName(){
		return this.nickname;
	}

	//  Make one agent the interlocutor of this
	public boolean meet(AtomicInteger agent){
		this.Interlocutor = agent;
		return true;
	}
	
	// Put an email in the mailbox
	public boolean get(Message mail) {
		return this.mailbox.add(mail);
	}
	
	
	// Read a message
	public boolean read_message(Message mail) throws FeatureTermException {
		return false;
	}
	
	// Send a message
	public boolean send_message(Message mail) {
		mail.read_sender().get(mail);
		return false;
	}
	
	// Pass the token to an other agent
	public boolean give_token() {
		if(Agent_v5.TOKEN == this.ID){
			Agent_v5.TOKEN = Interlocutor;
			return true;
		}
		return false;
	}
	
	
	// Agent Semiotic Methods
	
	public Collection<Concept> getInitialConcepts(){
		return this.contrastSets.get("initial").getAllConcepts();
	}
	
	public Collection<Concept> getBufferConcepts(){
		return this.contrastSets.get("buffer").getAllConcepts();
	}
	
	// Associate a feature term with the sign of one contrast set 'cake'
	public Sign nameObject(FeatureTerm e, String cake) throws Exception{
		ContrastSet cset = this.contrastSets.get(cake);
		if(cset.getConcepts(e) == null){
			return new Sign("Problem", "UnknownSign");
		}
		else if(cset.getConcepts(e).size() != 1){
			return new Sign("Problem", "MultipleOptions");
		}
		for(Concept c : cset.getConcepts(e)){
			return c.Sign();
		}
		return null;
	}
	
	// Learn
	public void learnFromHypothesis(RuleHypothesis h, Collection<FeatureTerm> E, LearningPackage ln) throws FeatureTermException{
		this.ln = ln;
		ContrastSet cset = new ContrastSet(this, "initial");
		cset.fromRuleHypothesis(h, E);
		this.contrastSets.put("initial", cset);
	}
	
	// Create a custom contrast set
	public void createContrastSet(String cake){
		ContrastSet new_cset = new ContrastSet(this, cake);
		this.contrastSets.put(cake, new_cset);
	}
	
	// Get the buffer contrast set
	public ContrastSet getBuffer() throws Exception{
		if(!this.contrastSets.containsKey("buffer")){
			this.createContrastSet("buffer");
			this.contrastSets.get("buffer").putContext(this.getInitialContext());
		}
		return this.contrastSets.get("buffer");
	}
		
	// Return the initial context of the agent
	public Collection<FeatureTerm> getInitialContext(){
		if(this.contrastSets.containsKey("initial")){
			return this.contrastSets.get("initial").getContext();
		}
		return null;
	}
	
	// Put a semantic proposal in the buffer
	public boolean addAssociation(Sign s, FeatureTerm e, boolean canModify) throws Exception{
		HashSet<FeatureTerm> isAnElement = new HashSet<FeatureTerm>();
		isAnElement.addAll(this.getInitialContext());
		isAnElement.add(e);
		if(this.isContext(isAnElement)){
			return this.getBuffer().putElementandSignAssociation(new Sign("buffer", s.getPiece()), e);
		}
		else{
			return this.getBuffer().putRuleandSignAssociation(new Sign("buffer", s.getPiece()), e);
		}
	}
	
	public boolean removeAssociation(Sign s, FeatureTerm e) throws FeatureTermException, Exception{
		HashSet<FeatureTerm> isAnElement = new HashSet<FeatureTerm>();
		isAnElement.addAll(this.getInitialContext());
		isAnElement.add(e);
		if(this.isContext(isAnElement)){
			// Get the rule(s)
			for(Concept c : this.getBuffer().getConcepts(e)){
				//Delet the rule(s)-sign association
				for(FeatureTerm r : c.IntensionalDefinition()){
					if(r.subsumes(e)){
						this.getBuffer().removeRuleandSignAssociation(s, r);
					}
				}
			}
			// Delet the element-sign association
			return this.getBuffer().removeElementandSignAssociation(s, e);
		}
		else{
			// Get the element(s)
			for(FeatureTerm f : this.getBuffer().getConcept(s.getPiece()).ExtensionalDefinition()){
				// Delet the elemen(s)-sign association
				this.getBuffer().removeElementandSignAssociation(s, f);
			}
			// Delet the rule-sign association
			return this.getBuffer().removeRuleandSignAssociation(s, e);
		}
	}
	
	// Add a new Argument to the debate
	
	// Check 
	
	// Print the rules for all the concepts of all the contrast sets
	public void showLearning(){
		for(String cs : this.contrastSets.keySet()){
			System.out.println(">>>>> CONTRAST SET : "+cs);
			this.contrastSets.get(cs).showLearning(this.ln);
		}
	}
	
	// Test the context of all the contrast sets to see if they are all valids
	public void testLearning() throws FeatureTermException{
		for(ContrastSet cset : this.contrastSets.values()){
			if(this.isContext(cset.getContext())){
				System.out.println("Global context is valid");
			}
			else{
				System.out.println("Global context invalid!");
			}
		}
	}
	
	// Get Learning Package
	public LearningPackage getLearningPackage(){
		return this.ln;
	}
	
	// Get a concept from the contrast sets
	public Concept getConcept(Sign s){
		if(this.contrastSets.containsKey(s.getCake())){
			return this.contrastSets.get(s.getCake()).getConcept(s.getPiece());
		}
		return null;
	}
	
	// Put a concept in a given contrast set
	public void putConcept(Concept c) throws FeatureTermException{
		ContrastSet cset = this.contrastSets.get(c.Sign().getCake());
		if(cset == null)
			System.out.println("Error, contrast set of the concept doesn't exist");
		else{
			cset.putConcept(c);
		}
	}
	
	// Function agree "Basic"
	public Agreement agree(FeatureTerm x, FeatureTerm y) throws FeatureTermException {
		if (x == null || y == null) {
			return Agreement.Uncorrect;
		} else if (x.equivalents(y)) {
			return Agreement.True;
		} else if(x.subsumes(y) || y.subsumes(x)) {
			return Agreement.Correct;
		} else
			return Agreement.False;
	}

	// Function agree's first overload - two sets comparhison
	public Agreement agree(Collection<FeatureTerm> x, Collection<FeatureTerm> y) throws FeatureTermException {

		// Not allow the possibility of having something else than a set of element as an input
		if (!(this.isContext(x) && this.isContext(y))) {
			System.out.println("NOT A SET OF ELEMENT");
			return null;
		}

		Collection<FeatureTerm> a = new HashSet<FeatureTerm>(), b = new HashSet<FeatureTerm>();
		a.addAll(y);
		b.addAll(x);
		for (FeatureTerm e : y) {
			for (FeatureTerm f : x) {
				if (this.agree(e, f) == Agreement.True) {
					a.remove(e);
					b.remove(f);
				}
			}
		}

		if(!a.containsAll(y) && !b.containsAll(x)){
			if (a.size() == 0   ||   b.size() == 0){
				if (a.size() == 0 && b.size() == 0) {
					return Agreement.True;
				}
				return Agreement.Correct;
			}
			return Agreement.Uncorrect;
		}
		return Agreement.False;
	}
	
	// Function aggre's overload - one element one set
	public Agreement agree(FeatureTerm e , Collection<FeatureTerm> E) throws FeatureTermException{
		HashSet<FeatureTerm> e1 = new HashSet<FeatureTerm>();
		e1.add(e);
		return this.agree(E, e1);
	}
	
	// Function aggre's overload - one set one element
	public Agreement agree(Collection<FeatureTerm> E , FeatureTerm e) throws FeatureTermException{
		HashSet<FeatureTerm> e1 = new HashSet<FeatureTerm>();
		e1.add(e);
		return this.agree(E, e1);
	}

	// Function agree's second overload - first part : one sign one set
	public Agreement agree(Sign s, Collection<FeatureTerm> E) throws FeatureTermException {

		if (this.getConcept(s) != null) {
			for(FeatureTerm r : this.getConcept(s).IntensionalDefinition()){
				if(agree(r, E)!= Agreement.False)
					return agree(r, E);
			}
		}
		return Agreement.False;
	}
	
	// Function aggre's overload - one set one sign
	public Agreement agree(Collection<FeatureTerm> E, Sign s) throws FeatureTermException {

		if (this.getConcept(s) != null) {
			for(FeatureTerm r : this.getConcept(s).IntensionalDefinition()){
				if(agree(r, E)!= Agreement.False)
					return agree(r, E);
			}
		}
		return Agreement.False;
	}
	
	// Function aggre's overload - one element one sign
	public Agreement agree(FeatureTerm e, Sign s) throws FeatureTermException {

		if (this.getConcept(s) != null) {
			for(FeatureTerm r : this.getConcept(s).IntensionalDefinition()){
				if(agree(r, e)!= Agreement.False)
					return agree(r, e);
			}
		}
		return Agreement.False;
	}
	
	// Function aggre's overload - one sign one element
	public Agreement agree(Sign s, FeatureTerm e) throws FeatureTermException {

		if (this.getConcept(s) != null) {
			for(FeatureTerm r : this.getConcept(s).IntensionalDefinition()){
				if(agree(r, e)!= Agreement.False)
					return agree(r, e);
			}
		}
		return Agreement.False;
	}
	
	// Function agree's second overload - second part : two signs
	public Agreement agree(Sign s1, Sign s2) throws FeatureTermException {
		if (this.getConcept(s1) != null && this.getConcept(s2) != null) {
			return agree(this.getConcept(s1).ExtensionalDefinition(), this.getConcept(s2).ExtensionalDefinition());
		}
		return Agreement.False;
	}
	
	// To test if a set respects the conditions of a Context
	public boolean isContext(Collection<FeatureTerm> c) throws FeatureTermException{
		Collection<FeatureTerm> c2 = c;
		for(FeatureTerm e : c){
			for(FeatureTerm f : c2){
				if(this.agree(e, f) == Agreement.Uncorrect || this.agree(e, f) == Agreement.Correct)
					return false;
			}
		}
		return true;
	}

}
