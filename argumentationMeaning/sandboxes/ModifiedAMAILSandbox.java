package sandboxes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.argumentation.core.AMAIL;
import csic.iiia.ftl.argumentation.core.Argument;
import csic.iiia.ftl.argumentation.core.ArgumentAcceptability;
import csic.iiia.ftl.argumentation.core.ArgumentationBasedLearning;
import csic.iiia.ftl.argumentation.core.LaplaceArgumentAcceptability;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.RuleHypothesis;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import tools.LearningPackage;

public class ModifiedAMAILSandbox {
	
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
		;

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
		
		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);
		g.setName(null);
		g.defineFeatureValue(training_set.description_path.getEnd(), null);
		g.defineFeatureValue(training_set.solution_path.getEnd(), null);
		
		LearningPackage.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<>(training_set.differentSolutions()));
		
		ABUI learner = new ABUI();
		ABUI.ABUI_VERSION = 2;
		ABUI l1 = new ABUI();
		ABUI l2 = new ABUI();
		
		ArgumentAcceptability aa = new LaplaceArgumentAcceptability(training_tests.get(0), training_set.solution_path, training_set.description_path, (float)0.9);
		ArgumentAcceptability aa2 = new LaplaceArgumentAcceptability(training_tests.get(1), training_set.solution_path, training_set.description_path, (float)0.9);
		
		ArgumentationBasedLearning abl1 = new ArgumentationBasedLearning();
		ArgumentationBasedLearning abl2 = new ArgumentationBasedLearning();
		
		List<ArgumentationBasedLearning> l_abl = new ArrayList<>();
		l_abl.add(abl1);
		l_abl.add(abl2);
		
		List<ArgumentAcceptability> l_a = new ArrayList<>();
		l_a.add(aa);
		l_a.add(aa2);
		
		// Hypothesis
		RuleHypothesis h1 = learner.learnConceptABUI(training_tests.get(0), training_set.differentSolutions(),
				new ArrayList<Argument>(), aa, training_set.description_path, training_set.solution_path, o, dm);
		RuleHypothesis h2 = learner.learnConceptABUI(training_tests.get(1), training_set.differentSolutions(),
				new ArrayList<Argument>(), aa2, training_set.description_path, training_set.solution_path, o, dm);

		/*for(Rule r : h1.getRules()){
			if(r.solution.equals(training_set.differentSolutions().get(0)))
				h1.removeRule(r);
		}*/
		System.out.println(h1.toString(dm));
		
		/*for(Rule r : h2.getRules()){
			if(r.solution.equals(training_set.differentSolutions().get(1)))
				h2.removeRule(r);
		}*/
		System.out.println(h2.toString(dm));
		
		// Hypothesis l_h
		List<RuleHypothesis> l_h = new ArrayList<>();
		l_h.add(h1);
		l_h.add(h2);
		
		List<List<FeatureTerm>> l_f = new ArrayList<>();
		l_f.add(training_tests.get(0));
		l_f.add(training_tests.get(1));
		
		
		
		// Delet a Rule in RuleHypotheses
		System.out.println(training_set.differentSolutions().get(2).toStringNOOS(dm));
		//AMAIL amail = new AMAIL(l_h,training_set.differentSolutions().get(2), l_f, l_a, l_abl, true, training_set.description_path, training_set.solution_path, o, dm);
		AMAIL amail = new AMAIL(h1, h2, training_set.differentSolutions().get(2), training_tests.get(0), training_tests.get(1), aa, aa2, l1, l2, training_set.description_path, training_set.solution_path, o, dm);
		
		while(amail.moreRoundsP()){
			amail.round(true);
		}
		
		for(RuleHypothesis rh : amail.result()){
			System.out.println(rh.toString(dm));
		}

		
		
	}

}
