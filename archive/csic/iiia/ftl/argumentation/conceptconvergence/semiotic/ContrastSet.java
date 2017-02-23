package csic.iiia.ftl.argumentation.conceptconvergence.semiotic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import csic.iiia.ftl.argumentation.agents.Agent_v1;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.Rule;
import csic.iiia.ftl.learning.core.RuleHypothesis;

public class ContrastSet {
	
	private Context social_context;
	private Set<Concept> segregates;
	
	public ContrastSet(){
		this.social_context = null;
		this.segregates = new HashSet<Concept>();
	}
	
	public ContrastSet(Context c){
		this.social_context = c;
		this.segregates = new HashSet<Concept>();
	}
	
	public Context related_context(){
		return social_context;
	}
	
	public int add(Concept c){
		this.segregates.add(c);
		return 0;
	}
	
	public Set<FeatureTerm> all_examples(){
		Set<FeatureTerm> out = new HashSet<FeatureTerm>();
		for(Concept c : segregates){
			out.addAll(c.extensionalDefinition().get_examples());
		}
		return out;
	}
	
	public Sign categorize(FeatureTerm e) throws FeatureTermException{
		for(Concept c : segregates){
			if(c.intensionalDefinition().is_covering(e)){
				return c.sign();
			}
		}
		if(Agent_v1.DEBUG > 0){
			System.out.println("Categorization failed");
		}
		if(Agent_v1.DEBUG > 1){
			System.out.println("failed to categorize "+e.toStringNOOS(this.getInfos().dm()));
		}
		return new Sign("unknown", null);
	}
	
	public Rule get_generalization(Sign s, FeatureTerm e) throws FeatureTermException{
		Rule g = null;
		for(Concept c : segregates){
			if(c.sign().sign().equals(s.sign())){
				return c.intensionalDefinition().getCoveringGeneralization(e);
			}
		}
		return g;
	}
	
	public Set<Concept> get_attacked(RuleHypothesis rule) throws FeatureTermException{
		Set<Concept> output = new HashSet<Concept>();
		for(Concept c : segregates){
			boolean add = false;
			for(FeatureTerm e : c.extensionalDefinition().get_examples()){
				if(rule.coveredByAnyRule(e.featureValue(c.extensionalDefinition().get_informations().description_path().features.get(0))) != null){
					add = true;
					break;
				}
			}
			if(add){
				output.add(c);
			}
		}
		return output;
	}
	
	public int forget(Concept c){
		if(this.segregates.remove(c)){
			return 0;
		}
		return 1;
	}
	
	public Set<Concept> get_others(Concept c){
		Set<Concept> output = new HashSet<Concept>();
		for(Concept i : this.segregates){
			if(!i.equals(c)){
				output.add(i);
			}
		}
		return output;
	}
	
	public int extend_solutions(FeatureTerm s){
		for(Concept c : segregates){
			c.extensionalDefinition().get_informations().different_solutions().add(s);
		}
		return 0;
	}

	public ArrayList<String> list_known_concepts() {
		ArrayList<String> output = new ArrayList<String>();
		for(Concept c : this.segregates){
			output.add(c.sign().sign());
		}
		if(output.isEmpty())
			output.add("empty");
		return output;
	}
	
	public void show_rules(){
		for(Concept c : this.segregates){
			System.out.println("--                   rule for "+c.sign().sign()+"                  --");
			c.intensionalDefinition().tell_rules();
		}
	}
	
	public LearningPackage getInfos(){
		LearningPackage l = null;
		for(Concept c : segregates){
			if(l != null && !c.getInfos().equals(l)){
				return null;
			}
			l = c.getInfos();
		}
		return l;
	}

}
