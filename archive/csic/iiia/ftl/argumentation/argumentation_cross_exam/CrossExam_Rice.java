package csic.iiia.ftl.argumentation.argumentation_cross_exam;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import csic.iiia.ftl.argumentation.agents.Agent_v1;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Concept;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Sign;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.SetCast;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.TwoKeyMap;
import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Sort;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;


public class CrossExam_Rice {
	
	public static void main(String[] arg) throws Exception{
		
		int CB = TrainingSetUtils.SOYBEAN_DATASET;
		
		Ontology base_ontology;
		Ontology o = new Ontology();
		
		base_ontology = new BaseOntology();
		o.uses(base_ontology);
		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();
		case_base.uses(dm);
		dm.create_boolean_objects(o);
		
		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(CB, o, dm, case_base);
		List<List<FeatureTerm>> training_sets = TrainingSetUtils.splitTrainingSet(training_set.cases, 4, training_set.description_path, training_set.solution_path, dm, 0, 0);

		LearningPackage ln = new LearningPackage(o, dm, training_set.description_path, training_set.solution_path, SetCast.cast(training_set.differentSolutions()));
		
		ABUI.ABUI_VERSION = 2;

		HashSet<Concept> agent_1_concepts = new HashSet<Concept>();
		HashSet<Concept> agent_2_concepts = new HashSet<Concept>();
		
		Agent_v1.DEBUG = 1;
		Concept.DEBUG = true;
		
		// Learning agent 1
		for(FeatureTerm s : training_set.differentSolutions()){
			Sign s1 = new Sign(s.toStringNOOS(dm), s);
			Concept c = new Concept(s1, ln);
			Set<FeatureTerm> training = SetCast.duplicate(training_sets.get(0));
			c.intensionalDefinition().learn(training);
			c.extensionalDefinition().creates_from_intensional(SetCast.duplicate(training_sets.get(0)));
			agent_1_concepts.add(c);
		}
		
		// Learning agent 2
		for(FeatureTerm s : training_set.differentSolutions()){
			Sign s1 = new Sign(s.toStringNOOS(dm), s);
			Concept c = new Concept(s1, ln);
			Set<FeatureTerm> training = SetCast.duplicate(training_sets.get(1));
			c.intensionalDefinition().learn(training);
			c.extensionalDefinition().creates_from_intensional(SetCast.duplicate(training_sets.get(1)));
			agent_2_concepts.add(c);
		}
		
		// Making the new subsets
		TwoKeyMap map = new TwoKeyMap();
		for(FeatureTerm f : training_set.cases){
			FeatureTerm e = f.featureValue(training_set.description_path.features.get(0));
			String k1 = "none";
			String k2 = "none";
			
			for(Concept c : agent_1_concepts){
				if(c.intensionalDefinition().is_covering(e)){
					k1 = c.sign().sign();
				}
			}
			
			for(Concept c : agent_2_concepts){
				if(c.intensionalDefinition().is_covering(e)){
					k2 = c.sign().sign();
				}
			}
			map.put(k1, k2, f);
		}
		
		//System.out.println(map.size());
		
		map.give_combinations();
	
		HashSet<Concept> new_concepts_1 = new HashSet<Concept>();
		HashSet<Concept> new_concepts_2 = new HashSet<Concept>();
		HashMap<String, Concept> map1 = new HashMap<String, Concept>();
		HashMap<String, Concept> map2 = new HashMap<String, Concept>();
		
		// Learn a new rule for each of the subconcepts -- Give them a new name by the same occasion
		Set<String> training_diff_solutions = new HashSet<String>();
		Set<FeatureTerm> training = new HashSet<FeatureTerm>();
		HashMap<String, List<String>> new_to_old = new HashMap<String, List<String>>();
		
		int i = 0;
		for(Set<FeatureTerm> set : map.getAll()){
			Sort s = new Sort(training_set.solution_path.features.get(0).get(), null, o);
			FeatureTerm nf = new TermFeatureTerm("solution-custom-"+i, s);
			new_to_old.put("solution-custom-"+i, map.give_Labels(set));
			for(FeatureTerm f : set){
				FeatureTerm of = f.featureValue(ln.solution_path().features.get(0));
				f.substitute(of, nf);
			}
			training.addAll(set);
			Sign s1 = new Sign("solution-custom-"+i, nf);
			Sign s2 = new Sign("solution-custom-"+i, nf);
			Concept c1 = new Concept(s1, ln);
			Concept c2 = new Concept(s2, ln);
			training_diff_solutions.add("solution-custom-"+i);
			new_concepts_1.add(c1);
			new_concepts_2.add(c2);
			i++;
		}
		
		System.out.println("NEXT LEARNING PHASE");
		
		for(Concept c : new_concepts_1){
			c.intensionalDefinition().learn(training);
			c.extensionalDefinition().creates_from_intensional(training);
			map1.put(c.sign().sign(), c);
		}
		
		for(Concept c : new_concepts_2){
			c.intensionalDefinition().learn(training);
			c.extensionalDefinition().creates_from_intensional(training);
			map2.put(c.sign().sign(), c);
		}
		
		// Comparing the different concepts
		HashSet<String> solutions = new HashSet<String>();
		HashSet<Concept> valid_concepts_1 = new HashSet<Concept>();
		HashSet<Concept> valid_concepts_2 = new HashSet<Concept>();
		HashSet<HashMap<String, Concept>> valid_concepts = new HashSet<HashMap<String,Concept>>();
		
		for(String s : training_diff_solutions){
			Concept c1 = map1.get(s);
			Concept c2 = map2.get(s);
			if((!Float.isNaN(c1.intensionalDefinition().getAllGeneralizations().evaluate(c1.extensionalDefinition().get_examples(), dm, ln.solution_path(), ln.description_path(), false)))
			|| (!Float.isNaN(c2.intensionalDefinition().getAllGeneralizations().evaluate(c2.extensionalDefinition().get_examples(), dm, ln.solution_path(), ln.description_path(), false)))){
				
				if(Float.isNaN(c1.intensionalDefinition().getAllGeneralizations().evaluate(c1.extensionalDefinition().get_examples(), dm, ln.solution_path(), ln.description_path(), false))){
					c1.intensionalDefinition().set(c2.intensionalDefinition().getAllGeneralizations());
				}
				
				if(Float.isNaN(c2.intensionalDefinition().getAllGeneralizations().evaluate(c2.extensionalDefinition().get_examples(), dm, ln.solution_path(), ln.description_path(), false))){
					c2.intensionalDefinition().set(c1.intensionalDefinition().getAllGeneralizations());
				}
				
				valid_concepts_1.add(c1);
				valid_concepts_2.add(c2);
				System.out.println(s);
				System.out.println(new_to_old.get(s).get(2));
				System.out.println(c1.extensionalDefinition().get_examples().size());
				
				HashMap<String, Concept> new_valid_concept = new HashMap<String, Concept>();
				
				Sort s1 = new Sort(training_set.solution_path.features.get(0).get(), null, o);
				FeatureTerm nf1 = new TermFeatureTerm(new_to_old.get(s).get(0), s1);
				c1.sign().changeSign(new_to_old.get(s).get(0), nf1);
				solutions.add(new_to_old.get(s).get(0));
				new_valid_concept.put("Agent1", c1);
				
				Sort s2 = new Sort(training_set.solution_path.features.get(0).get(), null, o);
				FeatureTerm nf2 = new TermFeatureTerm(new_to_old.get(s).get(1), s2);
				c2.sign().changeSign(new_to_old.get(s).get(1), nf2);
				solutions.add(new_to_old.get(s).get(1));
				new_valid_concept.put("Agent2", c2);
				
				valid_concepts.add(new_valid_concept);
			}
		}
		
		System.out.println(">>>");
		System.out.println("The system forgot "+(map.size() - valid_concepts.size())+" concepts");
		System.out.println(">>>");
		
		System.out.println("- - - - - - - - - - ");		
		
		// Creating the missing number of new labels
		solutions.remove("none");
		HashSet<HashMap<String, Concept>> final_concepts = new HashSet<HashMap<String,Concept>>();
		
		int j = 0;
		while(solutions.size() < valid_concepts.size()){
			solutions.add("Solution-"+j);
			j++;
		}

		for(String s : solutions){
			System.out.println(s);
		}
		
		System.out.println("- - - - - - - - - - ");
		
		for(HashMap<String, Concept> set : valid_concepts){
			if(set.get("Agent1").sign().sign().equals(set.get("Agent2").sign().sign())){
				if(set.get("Agent1").sign().sign().equals("none")){
					String n_string = "";
					for(String s : solutions){
						if(s.contains("Solution-")){
							n_string = s;
							break;
						}
					}
					Sort s = new Sort(training_set.solution_path.features.get(0).get(), null, o);
					FeatureTerm f = new TermFeatureTerm(n_string, s);
					set.get("Agent1").sign().changeSign(n_string, f);
					set.get("Agent2").sign().changeSign(n_string, f);
				}
				final_concepts.add(set);
				solutions.remove(set.get("Agent1").sign().sign());
			}
		}
		valid_concepts.removeAll(final_concepts);
		
		for(String s : solutions){
			System.out.println(s);
		}
		
		System.out.println("- - - - - - - - - - ");
		
		HashSet<String> to_delet = new HashSet<String>();
		for(String s : solutions){
			int count_concept_1 = 0;
			int count_concept_2 = 0;
			for(HashMap<String, Concept> set : valid_concepts){
				if(set.get("Agent1").sign().sign().equals(s)){
					count_concept_1 ++;
				}
				if(set.get("Agent2").sign().sign().equals(s)){
					count_concept_2 ++;
				}
			}
			System.out.println(s+" has "+count_concept_1+" concepts in Agent 1 and "+count_concept_2+" concepts in Agent 2");
			for(HashMap<String, Concept> set : valid_concepts){
				if(((count_concept_2 == 0) && ((set.get("Agent1").sign().sign().equals(s)))) || ((count_concept_1 <= count_concept_2) && (set.get("Agent1").sign().sign().equals(s)))){
					set.get("Agent2").sign().changeSign(set.get("Agent1").sign());
					final_concepts.add(set);
					to_delet.add(s);
					break;
				}
				if(((count_concept_1 == 0) && ((set.get("Agent2").sign().sign().equals(s)))) || ((count_concept_1 > count_concept_2) && (set.get("Agent2").sign().sign().equals(s)))){
					set.get("Agent1").sign().changeSign(set.get("Agent2").sign());
					final_concepts.add(set);
					to_delet.add(s);
					break;
				}
			}
		}
		valid_concepts.removeAll(final_concepts);
		solutions.removeAll(to_delet);
		
		System.out.println("- - - - - - - - - - ");
		
		for(String s : solutions){
			System.out.println(s);
		}
		
		System.out.println("- - - - - - - - - - ");
		
		LinkedList<String> solutions_array = new LinkedList<String>();
		for(String s : solutions){
			solutions_array.add(s);
		}
		
		for(HashMap<String, Concept> set : valid_concepts){
			String name = solutions_array.removeFirst();
			Sort s = new Sort(training_set.solution_path.features.get(0).get(), null, o);
			FeatureTerm f = new TermFeatureTerm(name, s);
			set.get("Agent1").sign().changeSign(name, f);
			set.get("Agent2").sign().changeSign(name, f);
			final_concepts.add(set);
			solutions.remove(name);
		}
		
		for(HashMap<String, Concept> set : final_concepts){
			System.out.println("Agent 1 labels it as "+set.get("Agent1").sign().sign()+" and Agent 2 labels it as "+set.get("Agent2").sign().sign());
		}
		
		System.out.println("- - - - - - - - - - ");
		
		int initial_examples = 0;
		int initial_covered_1 = 0;
		int initial_covered_2 = 0;
		int final_covered = 0;
		
		initial_examples = training_set.cases.size();
		
		for(FeatureTerm f : training_set.cases){
			FeatureTerm e = f.featureValue(training_set.description_path.features.get(0));
			for(Concept c : agent_1_concepts){
				if(c.intensionalDefinition().is_covering(e)){
					initial_covered_1 ++;
					break;
				}
			}
			for(Concept c : agent_2_concepts){
				if(c.intensionalDefinition().is_covering(e)){
					initial_covered_2 ++;
					break;
				}
			}
			for(HashMap<String, Concept> set : final_concepts){
				if(set.get("Agent1").intensionalDefinition().is_covering(e)){
					final_covered ++;
					break;
				}
			}
		}
		
		
		System.out.println("FINAL COUNT");
		System.out.println("# of total examples : "+initial_examples);
		System.out.println("# of initial covered examples by agent 1 : "+initial_covered_1);
		System.out.println("# of initial covered examples by agent 2 : "+initial_covered_2);
		System.out.println("# of final covered examples : "+final_covered);
		
		
		
	}

}
