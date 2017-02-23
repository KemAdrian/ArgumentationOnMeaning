package csic.iiia.ftl.argumentation.conceptconvergence.tools;

import java.util.ArrayList;
import java.util.List;

import csic.iiia.ftl.argumentation.agents.Agent_v5;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.Concept;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.Sign;
import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Sort;
import csic.iiia.ftl.learning.core.RuleHypothesis;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;

public class SandBox {
	
	public static void main(String[] arg) throws Exception{
		
		// Opening of the Cases Set
		// Opening of the Cases Set
		ABUI.ABUI_VERSION = 2;
					
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

		List<FeatureTerm> training_set_a1 = training_tests.get(0);
		List<FeatureTerm> training_set_a2 = training_tests.get(1);
		List<FeatureTerm> training_set_test = training_tests.get(2);

		System.out.println("size a1 : " + training_set_a1.size());
		System.out.println("size a2 : " + training_set_a2.size());
		System.out.println("size test : " + training_set_test.size());

		Agent_v5 adam = new Agent_v5("adam");
		Agent_v5 boby = new Agent_v5("boby");
		Agent_v5 carl = new Agent_v5("carl");

		ABUI.ABUI_VERSION = 2;
		ABUI learner = new ABUI();

		RuleHypothesis hypothesis_1 = learner.initialTraining(training_set_a1, training_set.differentSolutions(), training_set.description_path, training_set.solution_path,o, dm);
		adam.learnFromHypothesis(hypothesis_1, training_set_a1, new LearningPackage(o, dm, training_set.description_path, training_set.solution_path, SetCast.cast((training_set.differentSolutions()))));

		RuleHypothesis hypothesis_2 = learner.initialTraining(training_set_a2, training_set.differentSolutions(), training_set.description_path, training_set.solution_path, o, dm);
		boby.learnFromHypothesis(hypothesis_2, training_set_a2, new LearningPackage(o, dm, training_set.description_path, training_set.solution_path, SetCast.cast((training_set.differentSolutions()))));

		RuleHypothesis hypothesis_T = learner.initialTraining(training_set_test, training_set.differentSolutions(), training_set.description_path, training_set.solution_path, o, dm);
		carl.learnFromHypothesis(hypothesis_T, training_set_test, new LearningPackage(o, dm, training_set.description_path, training_set.solution_path, SetCast.cast((training_set.differentSolutions()))));

		for (FeatureTerm s1 : training_set.differentSolutions()) {
			for (FeatureTerm s2 : training_set.differentSolutions()) {

				Sign S1 = new Sign("initial", s1.toStringNOOS(dm));
				Sign S2 = new Sign("initial", s2.toStringNOOS(dm));

				Concept c1 = adam.getConcept(S1);
				Concept c2 = boby.getConcept(S2);

				if (c1 != null) {
					for (FeatureTerm r1 : c1.IntensionalDefinition()) {
						if (c2 != null) {
							for (FeatureTerm r2 : c2.IntensionalDefinition()) {

								System.out.println(" - - - - - - - NEW RULE COMBINATION - - - - - - - \n");

								System.out.println(" - - - - - - - SOLUTIONS : \n");

								System.out.println(
										"The solution of the 1st agent rule is : " + c1.Sign().getPiece());
								System.out.println("The solution of the 2nd agent rule is : "
										+  c2.Sign().getPiece() + "\n");

								System.out.println(" - - - - - - - PATTERNS : \n");

								System.out.println(
										"The pattern of the 1st agent rule is : " + r1.toStringNOOS(dm) + "\n");
								System.out.println(
										"The pattern of the 2nd agent rule is : " + r2.toStringNOOS(dm) + "\n");

								System.out.println(" - - - - - - - UNIFICATION : \n");

								Sort so1 = r1.getSort();
								Sort so2 = r2.getSort();

								Sort sou = so1.Unification(so2);
								Sort soa = so1.Antiunification(so2);

								Sort uos = so2.Unification(so1);
								Sort aos = so2.Antiunification(so1);

								FeatureTerm uni = r1.clone(dm, o);
								FeatureTerm inu = r1.clone(dm, o);
								FeatureTerm ant = r2.clone(dm, o);
								FeatureTerm tna = r2.clone(dm, o);

								uni.setSort(sou);
								ant.setSort(soa);
								inu.setSort(uos);
								tna.setSort(aos);

								System.out.println(" Subsumption of Rule 1 : " + r1.subsumes(uni));
								System.out.println(" Subsumption of Rule 2 : " + r2.subsumes(uni) + "\n");

								System.out.println(" Unification of the two rules : \n" + uni.toStringNOOS(dm) + "\n");

								System.out.println(" - - - - - - - ANTIUNIFICATION : \n");

								System.out.println(
										" Antiunification of the two rules \n: " + ant.toStringNOOS(dm) + "\n");

								System.out.println(" - - - - - - - COMMUTATIVITY : \n");

								System.out.println(" Unification : " + uni.equivalents(inu));
								System.out.println(" Antiunification : " + ant.equivalents(tna));

								System.out.println("\n");

							}
						}
					}
				}

			}
		}

	}

}
