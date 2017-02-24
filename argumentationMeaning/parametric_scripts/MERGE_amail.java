package parametric_scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import agents.Agent_simple;
import containers.ContrastSet;
import csic.iiia.ftl.argumentation.core.AMAIL;
import csic.iiia.ftl.argumentation.core.ArgumentAcceptability;
import csic.iiia.ftl.argumentation.core.ArgumentationBasedLearning;
import csic.iiia.ftl.argumentation.core.LaplaceArgumentAcceptability;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.Rule;
import csic.iiia.ftl.learning.core.RuleHypothesis;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import semiotic_elements.Sign;
import tools.ExampleSetManipulation;
import tools.LearningPackage;
import tools.Pair;

/**
 * This script creates an argumentation between two {@link Agent_simple}.
 * One of them does not make the distinction between two of the other {@link Agent_simple}'s {@link Concept}.
 * The results are saved in different files at the root of the repository.
 * 
 * This version tries to use only AMAIL and will not work most of the time.
 * 
 * @author kemoadrian
 *
 */
public class MERGE_amail {
	
	/**
	 * Gives the result of the argumentation in files at the root of the repository.
	 * 
	 * @param test_set the index of the NOOS dataset from {@link TrainingSetUtils}.
	 * @param redundancy make copies of some entry in the dataset to extend it.
	 * @param errorValue allows a tolerance toward the equivalence between two sets of {@link Example}. At 0, the two sets have to be equals. At 1, they are considered as equals even if they are totally disjoint. 
	 * @param ex the index of the experiment, in the case of multiple experiments runned at the same time.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Opening of the Cases Set
		int TEST = TrainingSetUtils.SEAT_TEST;
		
		ExampleSetManipulation.error = 0.25;

		Ontology base_ontology;
		Ontology o = new Ontology();

		base_ontology = new BaseOntology();

		o.uses(base_ontology);

		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();

		case_base.uses(dm);
		dm.create_boolean_objects(o);

		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(TEST, o, dm, case_base);
		
		List<List<FeatureTerm>> training_tests = TrainingSetUtils.splitTrainingSet(training_set.cases, 2, training_set.description_path, training_set.solution_path, dm, 0., 0.);
		

		List<FeatureTerm> training_set_a1 = training_tests.get(0);
		List<FeatureTerm> training_set_a2 = training_tests.get(1);
		
		System.out.println(" DATA SET SIZE : "+(training_set_a1.size()+training_set_a2.size()));
		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);
		
		g.setName(null);
		g.defineFeatureValue(training_set.description_path, null);
		g.defineFeatureValue(training_set.solution_path, null);
	
		LearningPackage.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<FeatureTerm>(training_set.differentSolutions()));

		Agent_simple adam = new Agent_simple();
		Agent_simple boby = new Agent_simple();
		
		adam.nick = "adam";
		boby.nick = "boby";
		
		List<FeatureTerm> training_set_a3 = new ArrayList<>();
		List<FeatureTerm> training_set_a4 = new ArrayList<>();
		for(FeatureTerm f : training_set_a1){
			if(f.readPath(LearningPackage.solution_path()).equals(training_set.differentSolutions().get(0))){
				training_set_a3.add(LearningPackage.createFeature(f.readPath(LearningPackage.description_path()), training_set.differentSolutions().get(1)));
				training_set_a4.add(f);
			}
		}
		training_set_a1.removeAll(training_set_a4);
		training_set_a1.addAll(training_set_a3);

		adam.initialize(training_set_a1);
		boby.initialize(training_set_a2);
		
		// Initial measure of disagreement
		Pair<Double,Double> p1 = new Pair<Double, Double>(0., 0.);
		Pair<Double,Double> p2 = new Pair<Double, Double>(0., 0.);
		
		RuleHypothesis h1 = new RuleHypothesis();
		RuleHypothesis h2 = new RuleHypothesis();
		
		Map<String, FeatureTerm> name_to_ft = new HashMap<>();
		Map<FeatureTerm, String> ft_to_name = new HashMap<>();
		
		for(Concept c : adam.Kc.set){
			FeatureTerm solutionToken = name_to_ft.get(c.sign());
			if(solutionToken == null){
				solutionToken = dm.getByName(c.sign.piece);
				LearningPackage.different_solutions().add(solutionToken);
				LearningPackage.dm().addFT(solutionToken);
				ft_to_name.put(solutionToken, c.sign());
				name_to_ft.put(c.sign(), solutionToken);
			}
			for(Generalization j : c.intensional_definition){
				Rule r = new Rule(j.generalization, solutionToken);
				h1.addRule(r);
			}
		}
		
		for(Concept c : boby.Kc.set){
			FeatureTerm solutionToken = name_to_ft.get(c.sign());
			if(solutionToken == null){
				solutionToken = dm.getByName(c.sign.piece);
				LearningPackage.different_solutions().add(solutionToken);
				LearningPackage.dm().addFT(solutionToken);
				ft_to_name.put(solutionToken, c.sign());
				name_to_ft.put(c.sign(), solutionToken);
			}
			for(Generalization j : c.intensional_definition){
				Rule r = new Rule(j.generalization, solutionToken);
				h2.addRule(r);
			}
		}
		
		
		Set<Example> context1 = adam.Kc.context;
		Set<Example> context2 = boby.Kc.context;
		
		ContrastSet c1 = makeContrastSet(context1, h1);
		ContrastSet c2 = makeContrastSet(context2, h2);
		for(Concept c : c1.set){
			c.display();
		}
		System.out.println("_ _ _ _ _ _");
		for(Concept c : c2.set){
			c.display();
		}
		p1 = c1.synchronicCompatibilityError(makeContrastSet(context1, h2));
		p2 = c2.synchronicCompatibilityError(makeContrastSet(context2, h1));
		
		System.out.println(p1.getLeft());
		System.out.println(p2.getLeft());
		
		RuleHypothesis hs1 = new RuleHypothesis();
		RuleHypothesis hs2 = new RuleHypothesis();
		
		for(FeatureTerm solution : training_set.differentSolutions()){
		
			// Hypothesis l_h
			ArgumentAcceptability aa = new LaplaceArgumentAcceptability(training_tests.get(0),
					training_set.solution_path, training_set.description_path, (float) 0.75);
			ArgumentAcceptability aa2 = new LaplaceArgumentAcceptability(training_tests.get(0),
					training_set.solution_path, training_set.description_path, (float) 0.75);

			ArgumentationBasedLearning abl1 = new ArgumentationBasedLearning();
			ArgumentationBasedLearning abl2 = new ArgumentationBasedLearning();

			List<RuleHypothesis> l_h = new ArrayList<>();
			l_h.add(h1);
			l_h.add(h2);

			List<List<FeatureTerm>> l_f = new ArrayList<>();
			l_f.add(training_tests.get(0));
			l_f.add(training_tests.get(1));

			List<ArgumentationBasedLearning> l_abl = new ArrayList<>();
			l_abl.add(abl1);
			l_abl.add(abl2);

			List<ArgumentAcceptability> l_a = new ArrayList<>();
			l_a.add(aa);
			l_a.add(aa2);

			AMAIL amail = new AMAIL(l_h, solution, l_f, l_a, l_abl, false,
					training_set.description_path, training_set.solution_path, o, dm);

			while (amail.moreRoundsP()) {
				amail.round(true);
			}
			
			for(Rule r : amail.result().get(0).getRules()){
				if(r.solution.equals(solution))
					hs1.addRule(r);
			}
			
			for(Rule r : amail.result().get(1).getRules()){
				if(r.solution.equals(solution))
					hs2.addRule(r);
			}
		
		}
		
		ContrastSet c3 = makeContrastSet(context1, hs1);
		ContrastSet c4 = makeContrastSet(context2, hs2);
		p1 = c3.synchronicCompatibilityError(makeContrastSet(context1, hs2));
		p2 = c4.synchronicCompatibilityError(makeContrastSet(context2, hs1));
		
		System.out.println(c1.diachronicCompatibilityError(c3).getLeft());
		System.out.println(c2.diachronicCompatibilityError(c4).getLeft());
		
		for(Concept c : c3.set){
			c.display();
		}
		System.out.println("_ _ _ _ _ _");
		for(Concept c : c4.set){
			c.display();
		}
		
		System.out.println(p1.getLeft());
		
		System.out.println(p2.getLeft());
		
		System.out.println(c1.diachronicCoverageError(c3).getLeft());
		
	}

	private static ContrastSet makeContrastSet(Set<Example> context1, RuleHypothesis h1) {
		Map<String,Concept> cs = new HashMap<String,Concept>();
		for(Rule r : h1.getRules()){
			Concept c = cs.get(r.solution.toStringNOOS(LearningPackage.dm()));
			if(c == null){
				//System.out.println(r.solution.toStringNOOS(LearningPackage.dm()));
				c = new Concept(new Sign("test",r.solution.toStringNOOS(LearningPackage.dm())), new HashSet<>(), new HashSet<>());
				cs.put(c.sign(), c);
			}
			try {
				c.intensional_definition.add(new Generalization(r.pattern));
			} catch (FeatureTermException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		for(Example e : context1){
			for(Concept c : cs.values()){
				try {
					if(c.covers(e))
						c.extensional_definition.add(e);
				} catch (FeatureTermException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		ContrastSet out = new ContrastSet(new HashSet<>(cs.values()), context1);
		return out;
	}

}
