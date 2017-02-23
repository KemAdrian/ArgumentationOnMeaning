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

public class Agent_v4 implements Agent{
	
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
	public Agent_v4(String nick){
		this.nickname = nick;
		this.ID = new AtomicInteger(Agent_v4.AGENT_COUNTER.incrementAndGet());
		this.Interlocutor = this.ID;
		this.mailbox = new ArrayList<Message>();
		//this.sendbox = new ArrayList<Message>();
		this.contrastSets = new HashMap<String, ContrastSet>();
		
		Agent_v4.AGENT_DIRECTORY.put(this.ID, this);
	}
	
	// Agent Global Methods
	public String uselesselyScreamHisName(){
		return this.nickname;
	}
	
	public Sign nameObject(FeatureTerm e, String cake) throws Exception {
		ContrastSet cset = this.contrastSets.get(cake);
		if (cset.getConcepts(e) == null) {
			return new Sign("Problem", "UnknownSign");
		} else if (cset.getConcepts(e).size() != 1) {
			return new Sign("Problem", "MultipleOptions");
		}
		for (Concept c : cset.getConcepts(e)) {
			return c.Sign();
		}
		return null;
	}

	public boolean meet(AtomicInteger agent){
		this.Interlocutor = agent;
		return true;
	}

	public boolean get(Message mail) {
		return this.mailbox.add(mail);
	}
	
	public boolean read_message(Message mail) throws FeatureTermException {
		return false;
	}
	
	public boolean send_message(Message mail) {
		mail.read_sender().get(mail);
		return false;
	}
	
	public boolean give_token() {
		if(Agent_v4.TOKEN == this.ID){
			Agent_v4.TOKEN = Interlocutor;
			return true;
		}
		return false;
	}
	
	
	// Agent Semiotic Methods
	
	// Learn
	public void learnFromHypothesis(String cake, RuleHypothesis h, Collection<FeatureTerm> E, LearningPackage ln) throws FeatureTermException{
		this.ln = ln;
		ContrastSet cset = new ContrastSet(this, cake);
		cset.fromRuleHypothesis(h, E);
		this.contrastSets.put(cake, cset);
	}
	
	public void createBufferContrastSet(String cake){
		ContrastSet new_cset = new ContrastSet(this, cake);
		this.contrastSets.put(cake, new_cset);
	}
	
	public void showLearning(){
		for(String cs : this.contrastSets.keySet()){
			System.out.println(">>>>> CONTRAST SET : "+cs);
			this.contrastSets.get(cs).showLearning(this.ln);
		}
	}
	
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
		} else
			return Agreement.Uncorrect;
	}

	// Function agree's first overload - two sets comparhison
	public Agreement agree(Collection<FeatureTerm> x, Collection<FeatureTerm> y) throws FeatureTermException {

		if (!(this.isContext(x) && this.isContext(y))) {
			System.out.println("THE SET IS NOT FROM A VALID CONTEXT");
			return Agreement.Uncorrect;
		}
		
		if(x.isEmpty() || y.isEmpty()){
			return Agreement.Uncorrect;
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

		if (a.size() == 0 || b.size() == 0) {
			if (a.size() == 0 && b.size() == 0) {
				return Agreement.True;
			}
			return Agreement.Correct;
		}
		return Agreement.Uncorrect;
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
			return agree(this.getConcept(s).ExtensionalDefinition(), E);
		}
		return Agreement.Uncorrect;
	}
	
	// Function aggre's overload - one set one sign
	public Agreement agree(Collection<FeatureTerm> E, Sign s) throws FeatureTermException {

		if (this.getConcept(s) != null) {
			return agree(this.getConcept(s).ExtensionalDefinition(), E);
		}
		return Agreement.Uncorrect;
	}
	
	// Function aggre's overload - one element one sign
	public Agreement agree(FeatureTerm e, Sign s) throws FeatureTermException {

		if (this.getConcept(s) != null) {
			return agree(this.getConcept(s).ExtensionalDefinition(), e);
		}
		return Agreement.Uncorrect;
	}
	
	// Function aggre's overload - one sign one element
	public Agreement agree(Sign s, FeatureTerm e) throws FeatureTermException {

		if (this.getConcept(s) != null) {
			return agree(this.getConcept(s).ExtensionalDefinition(), e);
		}
		return Agreement.Uncorrect;
	}
	// Function agree's second overload - second part : two signs
	public Agreement agree(Sign s1, Sign s2) throws FeatureTermException {
		if (this.getConcept(s1) != null && this.getConcept(s2) != null) {
			return agree(this.getConcept(s1).ExtensionalDefinition(), this.getConcept(s2).ExtensionalDefinition());
		}
		return Agreement.Uncorrect;
	}
	
	// To test if a set respects the conditions of a Context
	public boolean isContext(Collection<FeatureTerm> c) throws FeatureTermException{
		int i = 0;
		int j = 0;
		boolean output = true;
		Collection<FeatureTerm> c2 = c;
		for(FeatureTerm e : c){
			for(FeatureTerm f : c2){
				if(i == j){
					if(this.agree(e,f) == Agreement.Uncorrect){
						output = false;
					}
				}
				else{
					if(this.agree(e, f) == Agreement.True){
						System.out.println("TWICE THE ELEMENT "+e+" / "+f);
						System.out.println(e.getName());
						output = false;
					}
				}
				j++;
			}
			j = 0;
			i++;
		}
		return output;
	}

}
