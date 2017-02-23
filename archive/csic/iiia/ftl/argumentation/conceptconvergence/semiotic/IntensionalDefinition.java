package csic.iiia.ftl.argumentation.conceptconvergence.semiotic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.argumentation.core.Argument;
import csic.iiia.ftl.argumentation.core.ArgumentAcceptability;
import csic.iiia.ftl.argumentation.core.LaplaceArgumentAcceptability;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Path;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.Rule;
import csic.iiia.ftl.learning.core.RuleHypothesis;

public class IntensionalDefinition {
	
	public static int DEBUG = 0;
	
	private RuleHypothesis generalizations;
	private List<Argument> acceptedArguments;
	private ABUI learner;
	private Concept concept;
	
	public IntensionalDefinition(Concept c){
		this.concept = c;
		this.generalizations = new RuleHypothesis();
		this.acceptedArguments = new ArrayList<Argument>();
		this.learner = new ABUI();
		ABUI.ABUI_VERSION = 2;
	}
	
	public int learn(Set<FeatureTerm> examples) throws Exception{
		Path dp = concept.extensionalDefinition().get_informations().description_path();
		Path sp = concept.extensionalDefinition().get_informations().solution_path();
		Ontology o = concept.extensionalDefinition().get_informations().ontology();
		FTKBase dm = concept.extensionalDefinition().get_informations().dm();
		FeatureTerm solution = concept.sign().symbol();
		ArgumentAcceptability aa = new LaplaceArgumentAcceptability(examples, sp, dp, (float)0.75);
		this.generalizations = learner.learnConceptABUI(examples, solution , acceptedArguments, aa, dp, sp, o, dm);
		if(IntensionalDefinition.DEBUG > 0){
			System.out.println(generalizations.size() +" has been learned");
		}
		if(generalizations.size() == 0){
			return 1;
		}
		return 0;
	}
	
	public int set(RuleHypothesis generalizations) throws FeatureTermException{
		this.generalizations = generalizations;
		for(FeatureTerm e : concept.extensionalDefinition().get_examples()){
			if(generalizations.coveredByAnyRule(e) != null){
				System.out.println("WARNING : example "+e.toStringNOOS(concept.extensionalDefinition().get_informations().dm())+" is not covered by any rule");
			}
		}
		return 0;
	}
	
	public boolean is_covering(FeatureTerm e) throws FeatureTermException{
		if(generalizations.coveredByAnyRule(e) != null){
			return true;
		}
		return false;
	}
	
	public boolean is_covering(Set<FeatureTerm> set) throws FeatureTermException{
		for(FeatureTerm e : set){
			if(generalizations.coveredByAnyRule(e) == null){
				return false;
			}
		}
		return true;
	}
	
	public Rule getCoveringGeneralization(FeatureTerm e) throws FeatureTermException{
		return generalizations.coveredByAnyRule(e);
	}

	public int tell_rules() {
		for(Rule r : this.generalizations.getRules()){
			System.out.println(r.toStringNOOS(this.concept.extensionalDefinition().get_informations().dm()));
		}
		return 0;
	}
	
	public RuleHypothesis getAllGeneralizations(){
		return this.generalizations;
	}
	
	public boolean fromExisting(RuleHypothesis h, FeatureTerm s){
		RuleHypothesis new_generalizations = new RuleHypothesis();
		for(Rule r : h.getRules()){
			Rule r1 = new Rule(r.pattern, s);
			new_generalizations.addRule(r1);
		}
		this.generalizations = new_generalizations;
		return true;
	}
	
	public boolean acceptArgument(RuleHypothesis hypothesis){
		for(Rule r : hypothesis.getRules()){
			Argument a = new Argument(r);
			this.acceptedArguments.add(a);
		}
		return true;
	}

}
