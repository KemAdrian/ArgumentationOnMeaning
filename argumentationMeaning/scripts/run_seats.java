package scripts;

import java.util.ArrayList;
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
import tools.LearningPackage;
import tools.Token;

public class run_seats {

	public static void main(String[] args) throws Exception {

		// Opening of the Cases Set
		int TEST = TrainingSetUtils.SEAT_ALL;

		Ontology base_ontology;
		Ontology o = new Ontology();

		base_ontology = new BaseOntology();

		o.uses(base_ontology);

		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();

		case_base.uses(dm);
		dm.create_boolean_objects(o);

		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(TEST, o, dm, case_base);
		
		List<List<FeatureTerm>> training_tests = new ArrayList<List<FeatureTerm>>();
		
		// To use when we have the seat dataset in input
		for (int i = 0; i < 3; i++) {
			ArrayList<FeatureTerm> hey = new ArrayList<FeatureTerm>();
			training_tests.add(hey);
		}

		for (FeatureTerm e : training_set.cases) {
			if (Integer.parseInt(e.getName().toString().replace("e", "")) < 50) {
				training_tests.get(0).add(e);
			} else if (Integer.parseInt(e.getName().toString().replace("e", "")) < 100) {
				training_tests.get(1).add(e);
			} else {
				training_tests.get(2).add(e);
			}
		}
		
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
		
		System.out.println("- - - - - - Display initial score :");
		double i_score = 0.;
		double i_total = 0.;
		for(FeatureTerm f : training_tests.get(2)){
			Example e = new Example(f.readPath(training_set.description_path));
			//System.out.println("ok");
			for (Concept c1 : adam.Kc.getAllConcepts()) {
				for (Concept c2 : boby.Kc.getAllConcepts()) {
					if (c1.covers(e) && c2.covers(e) && c1.sign().equals(c2.sign())) {
						i_score++;
						break;
					}
				}
			}
			i_total ++;
		}
		System.out.println("   > "+i_score/i_total*100+"%\n");

		Token.initialize(adam, boby);
		System.out.println("Oracle : starts discussion,  agent " + Token.attacker().toString() + " in defense and agent "
					+ Token.defender().toString() + " in attack ("+((Agent_simple) Token.attacker()).current_phase+")");
		while (true) {
			Token.defender().turn();
			if (adam.current_phase == Phase.ExpressAgreementState && boby.current_phase == Phase.ExpressAgreementState){
				System.out.println("Adam vs Boby : "+adam.CreateMyCsetFromHypothesis().synchronicCompatibilityError(adam.CreateOtherCsetFromHypothesis()));
				System.out.println("Boby vs Adam : "+boby.CreateMyCsetFromHypothesis().synchronicCompatibilityError(boby.CreateOtherCsetFromHypothesis()));
			}
			if (adam.current_phase == Phase.Stop && boby.current_phase == Phase.Stop)
				break;
			System.out.println("\nOracle : switch roles, agent " + Token.attacker().toString() + " in defense and agent "
					+ Token.defender().toString() + " in attack ("+((Agent_simple) Token.attacker()).current_phase+")");
			Token.switchRoles();
		}
		
		System.out.println("- - - - - - Display initial score :");
		i_score = 0.;
		i_total = 0.;
		for(FeatureTerm f : training_tests.get(2)){
			Example e = new Example(f.readPath(training_set.description_path));
			//System.out.println("ok");
			for (Concept c1 : adam.H.own_concepts) {
				for (Concept c2 : boby.H.own_concepts) {
					if (c1.covers(e) && c2.covers(e) && c1.sign().equals(c2.sign())) {
						i_score++;
						break;
					}
				}
			}
			i_total ++;
		}
		System.out.println("   > "+i_score/i_total*100+"%\n");
		
		System.out.println("Adam : "+adam.CreateMyCsetFromHypothesis().synchronicCompatibilityError(adam.CreateMyCsetFromHypothesis()));
		System.out.println("Boby : "+boby.CreateMyCsetFromHypothesis().synchronicCompatibilityError(boby.CreateMyCsetFromHypothesis()));
		System.out.println("Adam vs Boby : "+adam.Kc.diachronicCompatibilityError(adam.CreateOtherCsetFromHypothesis()));
		System.out.println("Boby vs Adam : "+boby.Kc.diachronicCompatibilityError(boby.CreateOtherCsetFromHypothesis()));
		
	}

}
