package parametric_scripts;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
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
import tools.Pair;
import tools.Token;

/**
 * This script creates an argumentation between two {@link Agent_simple}.
 * One of them does not make the distinction between two of the other {@link Agent_simple}'s {@link Concept}.
 * The results are saved in different files at the root of the repository.
 * 
 * @author kemoadrian
 *
 */
public class MERGE_parametric_run {
	
	/**
	 * Gives the result of the argumentation in files at the root of the repository.
	 * 
	 * @param test_set the index of the NOOS dataset from {@link TrainingSetUtils}.
	 * @param redundancy make copies of some entry in the dataset to extend it.
	 * @param errorValue allows a tolerance toward the equivalence between two sets of {@link Example}. At 0, the two sets have to be equals. At 1, they are considered as equals even if they are totally disjoint. 
	 * @param ex the index of the experiment, in the case of multiple experiments runned at the same time.
	 * @throws Exception
	 */
	public static void run(int test_set, double redundancy, double errorValue, int ex) throws Exception {

		// Opening of the Cases Set
		int TEST = test_set;
		
		ExampleSetManipulation.error = errorValue;

		Ontology base_ontology;
		Ontology o = new Ontology();

		base_ontology = new BaseOntology();

		o.uses(base_ontology);

		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();

		case_base.uses(dm);
		dm.create_boolean_objects(o);

		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(TEST, o, dm, case_base);
		
		List<List<FeatureTerm>> training_tests = TrainingSetUtils.splitTrainingSet(training_set.cases, 2, training_set.description_path, training_set.solution_path, dm, 0., redundancy);
		

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
		
		System.out.println(training_set_a1.size());
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

		
		System.out.println(training_set_a1.size());
		adam.initialize(training_set_a1);
		boby.initialize(training_set_a2);
		
		// Create experiment file
		Path mainFile = Paths.get("Experiment_"+ex);
		List<String> lines = new LinkedList<>();
		ArrayList<String> i_scores_s = new ArrayList<>();
		ArrayList<Double> i_scores_d = new ArrayList<>();
		ArrayList<String> f_scores_s = new ArrayList<>();
		ArrayList<Double> f_scores_d = new ArrayList<>();
		
		// Save initial contrast sets
		String ICSA = "Initial_Contrast_Set_Adam_"+ex;
		String ICSB = "Initial_Contrast_Set_Boby_"+ex;
		List<String> initial_csset_a = new ArrayList<String>(adam.Kc.saveInFile());
		List<String> initial_csset_b = new ArrayList<String>(boby.Kc.saveInFile());
		
		// Change
		Pair<String,String> mergedA = new Pair<String, String>(training_set.differentSolutions().get(0).toStringNOOS(dm), training_set.differentSolutions().get(0).toStringNOOS(dm));
		Pair<String,String> mergedB = new Pair<String, String>("none", "none");
		
		// Save changed contrast sets
		String CCSA = "Changed_Contrast_Set_Adam_"+ex;
		String CCSB = "Changed_Contrast_Set_Boby_"+ex;
		List<String> changed_csset_a = new ArrayList<String>(adam.Kc.saveInFile());
		List<String> changed_csset_b = new ArrayList<String>(boby.Kc.saveInFile());
		
		Token.initialize(adam, boby);
		System.out.println("Oracle : starts discussion,  agent " + Token.defender().toString() + " in defense and agent "
					+ Token.attacker().toString() + " in attack ("+((Agent_simple) Token.attacker()).current_phase+")");
		
		// Initial measure of disagreement
		Pair<Double,Double> p1 = new Pair<Double, Double>(0., 0.);
		Pair<Double,Double> p2 = new Pair<Double, Double>(0., 0.);
		
		boolean diditonce = false;
		while (true) {
			Token.defender().turn();
			if (adam.current_phase == Phase.Stop && boby.current_phase == Phase.Stop)
				break;
			if(adam.current_phase == Phase.ExpressAgreementState && boby.current_phase == Phase.ExpressAgreementState && !diditonce){
				System.out.println(" adam vs boby ice : "+adam.Kc.synchronicCompatibilityError(adam.CreateOtherCsetFromHypothesis()));
				p1 = adam.Kc.synchronicCompatibilityError(adam.CreateOtherCsetFromHypothesis());
				System.out.println(" boby vs adam ice : "+boby.Kc.synchronicCompatibilityError(boby.CreateOtherCsetFromHypothesis()));
				p2 = boby.Kc.synchronicCompatibilityError(boby.CreateOtherCsetFromHypothesis());
				diditonce = true;
			}
			System.out.println("\nOracle : switch roles, agent " + Token.attacker().toString() + " in defense and agent "
					+ Token.defender().toString() + " in attack ("+((Agent_simple) Token.attacker()).current_phase+")");
			Token.switchRoles();
		}
		
		// Save initial scores
		i_scores_s.add("Adam's Cs Integrated Error");
		i_scores_s.add("Boby's Cs Integrated Error");
		i_scores_s.add("Adam's Cs Integrated Variance");
		i_scores_s.add("Boby's Cs Integrated Variance");
		
		i_scores_s.add("Adam vs Boby Integrated Error");
		i_scores_s.add("Boby vs Adam Integrated Error");
		i_scores_s.add("Adam vs Boby Integrated Variance");
		i_scores_s.add("Boby vs Adam Integrated Variance");
		
		i_scores_d.add(adam.Kc.synchronicCompatibilityError(adam.Kc).getLeft());
		i_scores_d.add(boby.Kc.synchronicCompatibilityError(boby.Kc).getLeft());
		i_scores_d.add(adam.Kc.synchronicCompatibilityError(adam.Kc).getRight());
		i_scores_d.add(boby.Kc.synchronicCompatibilityError(boby.Kc).getRight());
		
		i_scores_d.add(p1.getLeft());
		i_scores_d.add(p2.getLeft());
		i_scores_d.add(p1.getRight());
		i_scores_d.add(p2.getRight());
		
		// Save final contrast set
		String FCSA = "Final_Contrast_Set_Adam_"+ex;
		String FCSB = "Final_Contrast_Set_Boby_"+ex;
		List<String> final_csset_a = new ArrayList<String>(adam.CreateMyCsetFromHypothesis().saveInFile());
		List<String> final_csset_b = new ArrayList<String>(boby.CreateMyCsetFromHypothesis().saveInFile());
	
		// Save final scores
		Pair<Double,Double> p3 = new Pair<Double, Double>(0., 0.);
		Pair<Double,Double> p4 = new Pair<Double, Double>(0., 0.);
		System.out.println("diachronic");
		p3 = adam.Kc.diachronicCompatibilityError(adam.CreateMyCsetFromHypothesis());
		p4 = boby.Kc.diachronicCompatibilityError(boby.CreateMyCsetFromHypothesis());
		
		// Check the intern compatibility of contrast sets
		Pair<Double, Double> p5 = new Pair<Double, Double>(0., 0.);
		Pair<Double, Double> p6 = new Pair<Double, Double>(0., 0.);
		System.out.println("synchronic");
		p5 = adam.CreateMyCsetFromHypothesis().synchronicCompatibilityError(adam.CreateMyCsetFromHypothesis());
		p6 = boby.CreateMyCsetFromHypothesis().synchronicCompatibilityError(boby.CreateMyCsetFromHypothesis());
		
		f_scores_s.add("Adam's newCs Integrated Error");
		f_scores_s.add("Boby's newCs Integrated Error");
		f_scores_s.add("Adam's newCs Integrated Variance");
		f_scores_s.add("Boby's newCs Integred Variance");
		f_scores_d.add(p5.getLeft());
		f_scores_d.add(p6.getLeft());
		f_scores_d.add(p5.getRight());
		f_scores_d.add(p6.getRight());
		
		f_scores_s.add("Adam vs newCs Integrated Error");
		f_scores_s.add("Boby vs newCs Integrated Error");
		f_scores_s.add("Adam vs newCs Integrated Variance");
		f_scores_s.add("Boby vs newCs Integrated Variance");
		f_scores_d.add(p3.getLeft());
		f_scores_d.add(p4.getLeft());
		f_scores_d.add(p3.getRight());
		f_scores_d.add(p4.getRight());
		
		// Write in files
		lines.add(ICSA);
		lines.add(ICSB);
		lines.add(CCSA);
		lines.add(CCSB);
		lines.add(FCSA);
		lines.add(FCSB);
		lines.add("\n");
		lines.add(mergedA.getLeft()+"\t"+mergedA.getRight());
		lines.add(mergedB.getLeft()+"\t"+mergedB.getRight());
		Files.write(Paths.get(ICSA), initial_csset_a, Charset.forName("UTF-8"));
		Files.write(Paths.get(ICSB), initial_csset_b, Charset.forName("UTF-8"));
		Files.write(Paths.get(CCSA), changed_csset_a, Charset.forName("UTF-8"));
		Files.write(Paths.get(CCSB), changed_csset_b, Charset.forName("UTF-8"));
		Files.write(Paths.get(FCSA), final_csset_a, Charset.forName("UTF-8"));
		Files.write(Paths.get(FCSB), final_csset_b, Charset.forName("UTF-8"));
		lines.add("\n");
		for(int i = 0;i<i_scores_d.size();i++){
			lines.add(i_scores_s.get(i)+'\t'+i_scores_d.get(i));
		}
		lines.add("\n");
		for(int i = 0;i<f_scores_d.size();i++){
			lines.add(f_scores_s.get(i)+'\t'+f_scores_d.get(i));
		}
		Files.write(mainFile, lines, Charset.forName("UTF-8"));
	}

}
