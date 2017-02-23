package csic.iiia.ftl.argumentation.conceptconvergence.semiotic;

import java.util.HashSet;
import java.util.Set;

import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;

public class ExtensionalDefinition {
	
	private LearningPackage info;
	private Set<FeatureTerm> examples;
	private Concept concept;
	
	public ExtensionalDefinition(LearningPackage info, Concept c){
		this.info = info;
		this.examples = new HashSet<FeatureTerm>();
		this.concept = c;
	}
	
	public int add_example(FeatureTerm e){
		examples.add(e);
		return 0;
	}
	
	public int add_example(FeatureTerm e, FeatureTerm s) throws FeatureTermException{
		FeatureTerm f = e.clone(info.ontology());
		f.substitute(f.featureValue(info.solution_path().features.get(0)), s);
		examples.add(f);
		return 0;
	}
	
	public int add_example_set(Set<FeatureTerm> examples){
		this.examples.addAll(examples);
		return 0;
	}
	
	public Set<FeatureTerm> get_examples(){
		return this.examples;
	}
	
	public LearningPackage get_informations(){
		return info;
	}
	
	public int creates_from_intensional(Set<FeatureTerm>training_examples) throws FeatureTermException{
		for(FeatureTerm ft : training_examples){
			if(concept.intensionalDefinition().is_covering(ft.featureValue(this.info.description_path().features.get(0)))){
				examples.add(ft);
			}
		}
		return 0;
	}
	
	public int creates_from_intensional(Set<FeatureTerm>training_examples, FeatureTerm new_solution) throws FeatureTermException{
		for(FeatureTerm ft : training_examples){
			if(concept.intensionalDefinition().is_covering(ft.featureValue(this.info.description_path().features.get(0)))){
				FeatureTerm of = ft.featureValue(concept.getInfos().solution_path().features.get(0));
				ft.substitute(of, new_solution);
				examples.add(ft);
			}
		}
		return 0;
	}
	
	public int test_solutions() throws FeatureTermException{
		HashSet<FeatureTerm> solutions = new HashSet<FeatureTerm>();
		for(FeatureTerm e : examples){
			if(!solutions.contains(e.featureValue(this.info.solution_path().features.get(0)))){
				solutions.add(e.featureValue(this.info.solution_path().features.get(0)));
			}
		}
		for(FeatureTerm e : solutions){
			System.out.println("Concept "+this.concept.sign().sign()+" contains the solution "+e.toStringNOOS(this.info.dm()));
		}
		return 0;
	}
	
	public boolean switch_solution(FeatureTerm new_solution) throws FeatureTermException{
		for(FeatureTerm f : this.examples){
			f.substitute(f.featureValue(info.description_path().features.get(0)), new_solution);
		}
		return true;
	}

}
