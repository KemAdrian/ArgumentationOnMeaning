package scripts;

import java.util.HashSet;
import java.util.List;

import agents.Agent_simple;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import enumerators.Phase;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import tools.ExampleSetManipulation;
import tools.LearningPackage;
import tools.Token;

public class run {

	public static void main(String[] args) throws Exception {

		// Opening of the Cases Set
		int TEST = TrainingSetUtils.SOYBEAN_DATASET;
		
		ExampleSetManipulation.error = 0.275;

		Ontology base_ontology;
		Ontology o = new Ontology();

		base_ontology = new BaseOntology();

		o.uses(base_ontology);

		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();

		case_base.uses(dm);
		dm.create_boolean_objects(o);

		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(TEST, o, dm, case_base);
		
		System.out.println(" DATA SET SIZE : "+training_set.cases.size());
		
		List<List<FeatureTerm>> training_tests = TrainingSetUtils.splitTrainingSet(training_set.cases, 2, training_set.description_path, training_set.solution_path, dm, 0., 0.);
		
		// To use when we don't have the seat dataset in input
		//training_tests = TrainingSetUtils.splitTrainingSet(training_set.cases, 3, training_set.description_path, training_set.solution_path, dm, 0, 0);
		

		List<FeatureTerm> training_set_a1 = training_tests.get(0);
		List<FeatureTerm> training_set_a2 = training_tests.get(1);
		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);
		
		g.setName(null);
		g.defineFeatureValue(training_set.description_path, null);
		g.defineFeatureValue(training_set.solution_path, null);
	
		LearningPackage.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<FeatureTerm>(training_set.differentSolutions()));

		Agent_simple adam = new Agent_simple();
		Agent_simple boby = new Agent_simple();
		
		adam.nick = "adam";
		boby.nick = "boby";

		adam.initialize(training_set_a1);
		boby.initialize(training_set_a2);
		
		adam.Kc.mergeConcepts();
		boby.Kc.mergeConcepts();
		
		System.out.println("- - - - - - Display initial score :");
		double i_score = 0.;
		double i_total = 0.;
		for(FeatureTerm f : training_set.cases){
			Example e = new Example(f.readPath(training_set.description_path));
			//System.out.println("ok");
			for (Concept c1 : adam.Kc.getAllConcepts()) {
				boolean br = false;
				for (Concept c2 : boby.Kc.getAllConcepts()) {
					if (c1.covers(e) && c2.covers(e) && c1.sign().equals(c2.sign())) {
						i_score++;
						br = true;
						break;
					}
					if(br)
						break;
				}
			}
			i_total ++;
		}
		System.out.println("   > "+i_score/i_total*100+"%\n");
		
		Token.initialize(adam, boby);
		System.out.println("Oracle : starts discussion,  agent " + Token.defender().toString() + " in defense and agent "
					+ Token.attacker().toString() + " in attack ("+((Agent_simple) Token.attacker()).current_phase+")");
		while (true) {
			Token.defender().turn();
			if (adam.current_phase == Phase.Stop && boby.current_phase == Phase.Stop)
				break;
			System.out.println("\nOracle : switch roles, agent " + Token.attacker().toString() + " in defense and agent "
					+ Token.defender().toString() + " in attack ("+((Agent_simple) Token.attacker()).current_phase+")");
			Token.switchRoles();
		}
		
		System.out.println("- - - - - - Display final score :");
		i_score = 0.;
		i_total = 0.;
		for(FeatureTerm f : training_set.cases){
			Example e = new Example(f.readPath(training_set.description_path));
			//System.out.println("ok");
			for (Concept c1 : adam.H.own_concepts) {
				boolean br = false;
				for (Concept c2 : boby.H.own_concepts) {
					if (c1.covers(e) && c2.covers(e) && c1.sign().equals(c2.sign())) {
						i_score++;
						br = true;
						break;
					}
				}
				if(br)
					break;
			}
			i_total ++;
		}
		System.out.println("   > "+(i_score/i_total)*100+"%\n");
	}

}
