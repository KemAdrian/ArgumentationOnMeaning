package csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Agreement;
import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Problem;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.Rule;
import csic.iiia.ftl.learning.core.RuleHypothesis;

public class ContrastSet {
	
	private Agent agent;
	private String cake;
	private HashMap<String, Concept> sign_to_concept;
	private HashMap<FeatureTerm, HashSet<Sign>> element_to_sign;
	private LinkedList<ContrastSet> alternatives;
	
	public ContrastSet(Agent dad, String cake){
		this.cake = cake;
		this.agent = dad;
		this.sign_to_concept = new HashMap<String, Concept>();
		this.element_to_sign = new HashMap<FeatureTerm, HashSet<Sign>>();
		this.alternatives = new LinkedList<ContrastSet>();
	}
	
	public Collection<Concept> getAllConcepts(){
		return this.sign_to_concept.values();
	}
	
	public void putConcept(Concept c) throws FeatureTermException{
		if(this.cake.equals(c.Sign().getCake())){
			sign_to_concept.put(c.Sign().getPiece(), c);
			for(FeatureTerm e : c.ExtensionalDefinition()){
				if(element_to_sign.get(e) == null){
					element_to_sign.put(e, new HashSet<Sign>());
				}
				element_to_sign.get(e).add(c.Sign());
			}
			for(FeatureTerm e : this.element_to_sign.keySet()){
				if(c.covers(e, this.agent.getLearningPackage())){
					c.addExtensional(e, this.agent.getLearningPackage());
					if(element_to_sign.get(e) == null){
						element_to_sign.put(e, new HashSet<Sign>());
					}
					element_to_sign.get(e).add(c.Sign());
				}
			}
		}
		System.out.println("Error - tries to put concept in the wrong contrast set");
	}
	
	// Handle the buffer (add or delet concepts)
	public boolean putElementandSignAssociation(Sign s, FeatureTerm e) throws FeatureTermException{
		boolean sign_change = false;
		if(this.element_to_sign.containsKey(e)){
			if(!this.element_to_sign.get(e).equals(s)){
				sign_change = true;
			}
		}
		Concept c = null;
		if(!this.sign_to_concept.containsKey(s)){
			c = new Concept(s, new HashSet<FeatureTerm>(), this.agent.getLearningPackage());
			this.sign_to_concept.put(s.getPiece(), c);
		}
		else{
			c = this.sign_to_concept.get(s);
		}
		c.addExtensional(e, this.agent.getLearningPackage());
		if(element_to_sign.get(e) == null){
			element_to_sign.put(e, new HashSet<Sign>());
		}
		element_to_sign.get(e).add(s);
		return sign_change;
	}
	
	public boolean putRuleandSignAssociation(Sign s, FeatureTerm r) throws FeatureTermException{
		boolean sign_change = false;
		Concept c = null;
		if(!this.sign_to_concept.containsKey(s)){
			c = new Concept(s, new HashSet<FeatureTerm>(), this.agent.getLearningPackage());
			this.sign_to_concept.put(s.getPiece(), c);
		}
		else{
			c = this.sign_to_concept.get(s);
		}
		c.addIntensional(r, this.agent.getLearningPackage());
		for(FeatureTerm e : this.getContext()){
			if(c.covers(e, this.agent.getLearningPackage())){
				if(this.element_to_sign.containsKey(e)){
					if(!this.element_to_sign.get(e).equals(s)){
						sign_change = true;
					}
				}
				c.addExtensional(e, this.agent.getLearningPackage());
				if(element_to_sign.get(e) == null){
					element_to_sign.put(e, new HashSet<Sign>());
				}
				element_to_sign.get(e).add(s);
			}
		}
		return sign_change;
	}
	
	
	public boolean removeElementandSignAssociation(Sign s, FeatureTerm e){
		// In sign to concept
		if(sign_to_concept.containsKey(s)){
			return this.sign_to_concept.get(s).ExtensionalDefinition().remove(e);
		}
		
		// In element to sign
		if(element_to_sign.containsKey(e)){
			return this.element_to_sign.get(e).remove(s);
		}
		return false;
	}
	
	public boolean removeRuleandSignAssociation(Sign s, FeatureTerm r){
		// In sign to concept
		if(sign_to_concept.containsKey(s)){
			return this.sign_to_concept.get(s).IntensionalDefinition().remove(r);
		}
		return false;
	}
	
	// Return all the disagreements
	public ArrayList<Case> getDisagreements(Problem p) throws FeatureTermException{
		ArrayList<Case> output = new ArrayList<Case>();
		for(FeatureTerm e : this.element_to_sign.keySet()){
			if(element_to_sign.get(e).size() > 1){
				for(Sign s1 : element_to_sign.get(e)){
					for(Sign s2 : element_to_sign.get(e)){
						if(s1 != null && s2 != null){
							if(!s1.equals(s2)){
								if(agent.agree(s1, s2) == Agreement.True && p == Problem.Synonym){
									output.add(new Case(Problem.Synonym, sign_to_concept.get(s1).clone(agent.getLearningPackage()), sign_to_concept.get(s2)));
								}
								if(agent.agree(s1, s2) == Agreement.Correct && p == Problem.Hypernym){
									output.add(new Case(Problem.Hypernym, sign_to_concept.get(s1).clone(agent.getLearningPackage()), sign_to_concept.get(s2)));
								}
								if(agent.agree(s1, s2) == Agreement.Uncorrect && p == Problem.Overlap){
									output.add(new Case(Problem.Overlap, sign_to_concept.get(s1).clone(agent.getLearningPackage()), sign_to_concept.get(s2)));
								}
							}
						}
					}
				}
			}
		}
		return output;
	}
	
	public void putContext(Collection<FeatureTerm> context) throws Exception{
		for(FeatureTerm e : context){
			this.addElement(e);
		}
	}
	
	public Concept getConcept(String s){
		return this.sign_to_concept.get(s);
	}
	
	public Set<Concept> getConcepts(FeatureTerm e) throws Exception{
		HashSet<Concept> output = new HashSet<Concept>();
		
		// We have seen e
		if(this.element_to_sign.get(e) != null){
			for(Sign s : this.element_to_sign.get(e)){
				output.add(this.sign_to_concept.get(s.getPiece()));
			}
			return output;
		}
		
		// We have an equivalent of e
		for(FeatureTerm e1 : this.element_to_sign.keySet()){
			if(agent.agree(e, e1) == Agreement.True){
				for(Sign s : this.element_to_sign.get(e1)){
					output.add(this.sign_to_concept.get(s.getPiece()));
				}
				return output;
			}
		}
		
		// We can subsume e
		FeatureTerm f = this.addElement(e);
		if(f != null){
			return this.getConcepts(f);
		}
		
		// We cannot subsume e
		FeatureTerm g = e.clone(this.agent.getLearningPackage().dm(), this.agent.getLearningPackage().ontology());
		this.element_to_sign.put(g, null);
		return this.getConcepts(g);
	}
	
	public Collection<FeatureTerm> getContext(){
		Collection<FeatureTerm> output = new HashSet<FeatureTerm>();
		for(Concept c : this.sign_to_concept.values()){
			output.addAll(c.ExtensionalDefinition());
		}
		return output;
	}
	
	public LinkedList<ContrastSet> getAlternatives(){
		return this.alternatives;
	}
	
	public boolean addAlternative(ContrastSet cs){
		return this.alternatives.add(cs);
	}
	
	public boolean addAllAlternatives(Collection<ContrastSet> css){
		return this.alternatives.addAll(css);
	}
	
	// Test if a set of elements can be a contrast set
	public boolean isContrastSetOn(Collection<FeatureTerm> context) throws FeatureTermException {

		for (FeatureTerm e : context) {
			if (element_to_sign.get(e) == null)
				return false;
		}

		for (String si : this.sign_to_concept.keySet()) {
			if(this.sign_to_concept.get(si) == null)
				return false;
			if(!this.sign_to_concept.get(si).Sign().getCake().equals(cake))
				return false;
			for (String sj : this.sign_to_concept.keySet()) {
				if (si.equals(sj) && agent.agree(this.sign_to_concept.get(si).ExtensionalDefinition(),
					this.sign_to_concept.get(sj).ExtensionalDefinition()) != Agreement.True) {
					return false;
				}
			}
		}

		return true;
	}
	
	// Create a contrast set from a Rulehypothesis
	public void fromRuleHypothesis(RuleHypothesis h, Collection<FeatureTerm> E) throws FeatureTermException{
		LearningPackage ln = this.agent.getLearningPackage();
		for(Rule r : h.getRules()){
			String piece = r.solution.toStringNOOS(ln.dm());
			if(!this.sign_to_concept.containsKey(piece)){
				this.sign_to_concept.put(piece, new Concept(this.cake,piece));
			}
			Concept c = this.sign_to_concept.get(piece);
			c.addIntensional(r.pattern, ln);
			for(FeatureTerm e : E){
				if(r.pattern.subsumes(e.featureValue(ln.description_path().features.get(0)))){
					if(agent.agree(e.featureValue(ln.description_path().features.get(0)), c.ExtensionalDefinition()) == Agreement.Uncorrect
							|| agent.agree(e.featureValue(ln.description_path().features.get(0)), c.ExtensionalDefinition()) == Agreement.False){
						FeatureTerm f = c.addExtensional(e.featureValue(ln.description_path().features.get(0)), ln);
						if(element_to_sign.get(f) == null){
							element_to_sign.put(f, new HashSet<Sign>());
						}
						element_to_sign.get(f).add(c.Sign());
					}
				}
			}
		}
	}
	
	private FeatureTerm addElement(FeatureTerm e) throws Exception{
		FeatureTerm to_return = null;
		for(Concept c :this.sign_to_concept.values()){
			if(c.covers(e, this.agent.getLearningPackage())){
				FeatureTerm f = c.addExtensional(e, agent.getLearningPackage());
				if(element_to_sign.get(f) == null){
					element_to_sign.put(f, new HashSet<Sign>());
				}
				element_to_sign.get(f).add(c.Sign());
				to_return = f;	
			}
		}
		return to_return;
	}

	public void showLearning(LearningPackage ln) {
		for(String s : this.sign_to_concept.keySet()){
			System.out.println(">>>>>      Concept :"+s);
			System.out.println(">>>>>        size : "+this.sign_to_concept.get(s).ExtensionalDefinition().size());
			for(FeatureTerm r :this.sign_to_concept.get(s).IntensionalDefinition()){
				System.out.println(">>>     Rule    <<<");
				System.out.println(r.toStringNOOS(ln.dm()));
			}
		}
		
	}
	
	

}
