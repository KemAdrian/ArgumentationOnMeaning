package csic.iiia.ftl.argumentation.argumentation_cross_exam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import csic.iiia.ftl.argumentation.agents.Agent_v1;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Concept;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Sign;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.HashSetRadomizable;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.SetCast;
import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Sort;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.RuleHypothesis;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;

public class CrossExam_SplitFunction {
	
	public static void main(String[] arg) throws Exception{
			
		int CB = TrainingSetUtils.DEMOSPONGIAE_120_DATASET;
		
		Ontology base_ontology;
		Ontology o = new Ontology();
		
		base_ontology = new BaseOntology();
		o.uses(base_ontology);
		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();
		case_base.uses(dm);
		dm.create_boolean_objects(o);
		
		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(CB, o, dm, case_base);
		List<List<FeatureTerm>> training_sets = TrainingSetUtils.splitTrainingSet(training_set.cases, 2, training_set.description_path, training_set.solution_path, dm, 0, 0);
	
		LearningPackage ln = new LearningPackage(o, dm, training_set.description_path, training_set.solution_path, SetCast.cast(training_set.differentSolutions()));
		
		ABUI.ABUI_VERSION = 2;
	
		HashSetRadomizable<Concept> agent_1_concepts = new HashSetRadomizable<Concept>();
		HashSetRadomizable<Concept> agent_2_concepts = new HashSetRadomizable<Concept>();
		
		Agent_v1.DEBUG = 1;
		Concept.DEBUG = false;
		
		// Learning agent 1
		System.out.println("- - - - - - - - - - ");
		System.out.println(">>> The system is learning concepts for Agent 1");
		for(FeatureTerm s : training_set.differentSolutions()){
			Sign s1 = new Sign(s.toStringNOOS(dm), s);
			Concept c = new Concept(s1, ln);
			Set<FeatureTerm> training = SetCast.duplicate(training_sets.get(0));
			c.intensionalDefinition().learn(training);
			c.extensionalDefinition().creates_from_intensional(SetCast.duplicate(training_sets.get(0)));
			if(c.isMeaningful()){
				agent_1_concepts.add(c);
			}
		}
		
		// Learning agent 2
		System.out.println("- - - - - - - - - - ");	
		System.out.println(">>> The system is learning concepts for Agent 2");
		for(FeatureTerm s : training_set.differentSolutions()){
			Sign s1 = new Sign(s.toStringNOOS(dm), s);
			Concept c = new Concept(s1, ln);
			Set<FeatureTerm> training = SetCast.duplicate(training_sets.get(1));
			c.intensionalDefinition().learn(training);
			c.extensionalDefinition().creates_from_intensional(SetCast.duplicate(training_sets.get(1)));
			if(c.isMeaningful()){
				agent_2_concepts.add(c);
			}
		}
		
		// Making the new intensional definitions
		HashSetRadomizable<Concept> agent_1_to_debate = new HashSetRadomizable<Concept>();
		HashSetRadomizable<Concept> agent_2_to_debate = new HashSetRadomizable<Concept>();
		
		HashMap<Integer, Concept> agent_1_new_concept = new HashMap<Integer, Concept>();
		HashMap<Integer, Concept> agent_2_new_concept = new HashMap<Integer, Concept>();
		
		// Initialize the concepts to debate on
		for(Concept c : agent_1_concepts){
			agent_1_to_debate.add(c);
		}
		for(Concept c : agent_2_concepts){
			agent_2_to_debate.add(c);
		}
		
		// Agent 1 starts
		ArrayList<Concept> toDebate = new ArrayList<Concept>();
		toDebate.add(agent_2_to_debate.getRandomElement());
		
		// START THE DEBATE ON MEANING (TURNS LOOP)
		int round = 0;
		int signs = 0;
		int final_concepts = 0;
		
		// Display the number of concepts
		System.out.println("- - - - - - - - - - ");
		System.out.println(">>> Agent 1 starts with  "+agent_1_concepts.size()+" concepts");
		System.out.println(">>> Agent 2 starts with  "+agent_2_concepts.size()+" concepts");
		
		Scanner sc = new Scanner(System.in);
		
		while(true){
			
			// DEFENDER - Select the right defender (By checking the modulo of the nb of turns)
			System.out.println("- - - - - - - - - - ");
			System.out.println(">>> The system is switching attackant and defender");
			
			HashSetRadomizable<Concept> defender =  agent_2_concepts;
			HashSetRadomizable<Concept> attackant_still_in_debate = agent_1_to_debate;
			HashSetRadomizable<Concept> defender_still_in_debate = agent_2_to_debate;
			HashSetRadomizable<Concept> buffer = new HashSetRadomizable<Concept>();
			HashSet<Concept> toLearn = new HashSet<Concept>();
			HashMap<Integer, Concept> attackant_accepted = agent_1_new_concept;
			HashMap<Integer, Concept> defender_accepted = agent_2_new_concept;
			
			if(round%2 == 0){
				System.out.println("Agent 2 in attack, Agent 1 in defense");
				defender = agent_1_concepts;
				attackant_still_in_debate = agent_2_to_debate;
				defender_still_in_debate = agent_1_to_debate;
				attackant_accepted = agent_2_new_concept;
				defender_accepted = agent_1_new_concept;
			}
			else{
				System.out.println("Agent 1 in attack, Agent 2 in defense");
			}
			
			buffer.addAll(defender);
			
			// COUNTER ATTACK - Prepare the counter attack (empty for the moment)
			System.out.println("- - - - - - - - - - ");
			System.out.println(">>> Array of counter attacks initialized");
			ArrayList<Concept> counterAttacks = new ArrayList<Concept>();
			
			// CHECK THE ATTACKS
			System.out.println("- - - - - - - - - - ");
			System.out.println(">>> There are "+toDebate.size()+" attacks to debate on this turn");
			
			// If the attack is empty
			if(toDebate.isEmpty()){
				System.out.println(">>> There is no attack!!!");
				// And if agent still has something to debate, it sends it
				if(!defender.isEmpty()){
					System.out.println(">>> But there are still things to debate on for this agent, one concept is chosen randomly");
					counterAttacks.add(defender_still_in_debate.getRandomElement());
				}
			}
			
			// If the attack is not empty
			else{
				
				// For each attack to debate
				for(Concept argument : toDebate){
					
					// Duplicate attack
					Concept attackingConcept = new Concept(new Sign(argument.sign().sign(), argument.sign().symbol()), ln);
					attackingConcept.intensionalDefinition().fromExisting(argument.intensionalDefinition().getAllGeneralizations(), argument.sign().symbol());
					attackingConcept.extensionalDefinition().add_example_set(argument.extensionalDefinition().get_examples());
					
					RuleHypothesis attack = attackingConcept.intensionalDefinition().getAllGeneralizations();
					
					if(!attack.getRules().isEmpty()){
						System.out.println(">>> For the attack on the meaning of "+attack.getRules().get(0).solution.toStringNOOS());
					}
					else {
						System.out.println(">>> Attack is meaningless");
					}
					
					// Prepare the outcome of the defender's reaction
					boolean no_concept_attacked = true;
					
					boolean one_concept_attacked_all = false;
					boolean one_concept_attacked_some = false;
					Concept attacked_concept = null;
					HashSet<FeatureTerm> one_touched = null;
					HashSet<FeatureTerm> one_left = null;
					
					
					boolean multiple_concepts_attacked = false;
					HashSet<Concept> attacked_concepts_all = null;
					HashSet<Concept> attacked_concepts_some = null;
					HashMap<Concept, HashSet<FeatureTerm>> multiple_touched = null;
					HashMap<Concept, HashSet<FeatureTerm>> multiple_left = null;
					
					// Check each concept remaining to debate for the defender how it attacks
					for(Concept toDefend : buffer){
						System.out.println(">>>    The defender concept is "+toDefend.sign().sign());
						HashSet<FeatureTerm> old_extensional = new HashSet<FeatureTerm>();
						HashSet<FeatureTerm> new_extensional = new HashSet<FeatureTerm>();
						
						for(FeatureTerm f : toDefend.extensionalDefinition().get_examples()){
							FeatureTerm e =  f.featureValue(training_set.description_path.features.get(0));
							if(attack.coveredByAnyRule(e) != null){
								new_extensional.add(f);
							}
							else {
								old_extensional.add(f);
							}
						}
						
						// If the concept is not affected by the attack, notify
						if(new_extensional.isEmpty()){
							System.out.println(">>>    The defender concept "+toDefend.sign().sign()+" is not affected by the attack");
						}
						
						// If entirely attacked by the new definition, PREPARE to : duplicate old concept OR accept attack
						else if(new_extensional.size() == toDefend.extensionalDefinition().get_examples().size()){
							System.out.println(">>>    The defender concept "+toDefend.sign().sign()+" is entirely affected by the attack");
							no_concept_attacked = false;
							
							// If it's the first concept attacked
							if(!one_concept_attacked_all && !one_concept_attacked_some && !multiple_concepts_attacked){
								one_concept_attacked_all = true;
								attacked_concept = toDefend;
							}
							
							// If it's the second concept attacked
							else if(!multiple_concepts_attacked){
								attacked_concepts_some = new HashSet<Concept>();
								attacked_concepts_all = new HashSet<Concept>();
								multiple_touched = new HashMap<Concept, HashSet<FeatureTerm>>();
								multiple_left = new HashMap<Concept, HashSet<FeatureTerm>>();
								if(one_concept_attacked_all){
									attacked_concepts_all.add(attacked_concept);
								}
								if(one_concept_attacked_some){
									attacked_concepts_some.add(attacked_concept);
									multiple_touched.put(attacked_concept, one_touched);
									multiple_left.put(attacked_concept, one_left);
								}
								attacked_concepts_all.add(toDefend);
								one_concept_attacked_all = false;
								one_concept_attacked_some = false;
								multiple_concepts_attacked = true;
								attacked_concept = null;
							}
							
							// If it's the third or more concept attacked
							else{
								attacked_concepts_all.add(toDefend);
							}
							
						}
						
						// If partialy attacked by the new definition, prepare to build two concepts
						else if(!new_extensional.isEmpty() && !old_extensional.isEmpty()){
							System.out.println(">>>    The defender concept "+toDefend.sign().sign()+" is partially affected by the attack");
							System.out.println(">>>    The defender concept "+toDefend.sign().sign()+" has "+(new_extensional.size()*100/toDefend.extensionalDefinition().get_examples().size())+"% of its extensional definition ("+new_extensional.size() +" examples) covered by the new definition");
							no_concept_attacked = false;
							
							// If it's the first concept attacked
							if(!one_concept_attacked_all && !one_concept_attacked_some && !multiple_concepts_attacked){
								one_concept_attacked_some = true;
								attacked_concept = toDefend;
								one_touched = new_extensional;
								one_left = old_extensional;
							}
							
							// If it's the second concept attacked
							else if(!multiple_concepts_attacked){
								attacked_concepts_all = new HashSet<Concept>();
								attacked_concepts_some = new HashSet<Concept>();
								multiple_touched = new HashMap<Concept, HashSet<FeatureTerm>>();
								multiple_left = new HashMap<Concept, HashSet<FeatureTerm>>();
								if(one_concept_attacked_all){
									attacked_concepts_all.add(attacked_concept);
								}
								if(one_concept_attacked_some){
									attacked_concepts_some.add(attacked_concept);
									multiple_touched.put(attacked_concept, one_touched);
									multiple_left.put(attacked_concept, one_left);
								}
								attacked_concepts_some.add(toDefend);
								multiple_touched.put(toDefend, new_extensional);
								multiple_left.put(toDefend, old_extensional);
								one_concept_attacked_all = false;
								one_concept_attacked_some = false;
								multiple_concepts_attacked = true;
							}
							
							// If it's the third or more concept attacked
							else{
								attacked_concepts_some.add(toDefend);
								multiple_touched.put(toDefend, new_extensional);
								multiple_left.put(toDefend, old_extensional);
							}
						}
						
					}
					
					System.out.println("- - - - - - - - - - ");
					
					// CONCEPT BUILD
					
					if(no_concept_attacked){
						System.out.println(">>> The attack missed, we will accept it");
						
						// Create a concept from the attack
						// New concept and new concept's sign
						Sort s = new Sort(training_set.solution_path.features.get(0).get(), null, o);
						FeatureTerm nf = new TermFeatureTerm("solution-custom-"+signs, s);
						System.out.println(">>> We create a new concept Solution-custom-"+signs);
						Concept new_concept = new Concept(new Sign("solution-custom-"+signs, nf), ln);
						signs ++;
						// New concept's definition
						new_concept.intensionalDefinition().fromExisting(attack, nf);
						
						// Accept the new concept
						defender_accepted.put(final_concepts,new_concept);
						
						// Accept the attack for attackant
						attackant_accepted.put(final_concepts,attackingConcept);
						attackant_still_in_debate.remove(argument);
						
						// Delete the insignificat brother from the debate
						if(argument.hasInsignificantBrother()){
							attackant_still_in_debate.remove(argument.getBrother());
						}
						
						final_concepts ++;
						
					}
					
					else if(one_concept_attacked_all){
						System.out.println(">>> Only one concept attacked, and it is entirely attacked");
						
						// Create a concept from the attack
						// New concept and new concept's sign
						Sort s = new Sort(training_set.solution_path.features.get(0).get(), null, o);
						FeatureTerm nf = new TermFeatureTerm("solution-custom-"+signs, s);
						System.out.println(">>> We create a new concept Solution-custom-"+signs);
						Concept new_concept = new Concept(new Sign("solution-custom-"+signs, nf), ln);
						signs ++;
						// New concept's definition
						new_concept.intensionalDefinition().fromExisting(attack, nf);
						new_concept.extensionalDefinition().add_example_set(SetCast.duplicate(attacked_concept.extensionalDefinition().get_examples(),o));
						new_concept.extensionalDefinition().switch_solution(nf);
						
						// Accept the new concept
						defender_accepted.put(final_concepts,new_concept);
						buffer.add(new_concept);
						defender_still_in_debate.remove(attacked_concept);
						buffer.remove(attacked_concept);
						
						// Accept the attack for attackant
						attackant_accepted.put(final_concepts,attackingConcept);
						attackant_still_in_debate.remove(argument);
						
						// Delete the insignificat brother from the debate
						if(argument.hasInsignificantBrother()){
							attackant_still_in_debate.remove(argument.getBrother());
						}
						
						final_concepts ++;
						
					}
					
					else if(one_concept_attacked_some){
						System.out.println(">>> Only one concept attacked, and it is partially attacked");
						// Split concept for defenser
						
						// Covered
						// New concept and new concept's sign
						Sort s1 = new Sort(training_set.solution_path.features.get(0).get(), null, o);
						FeatureTerm nf1 = new TermFeatureTerm("solution-custom-"+signs, s1);
						System.out.println(">>> We create a new concept Solution-custom-"+signs);
						Concept new_concept_1 = new Concept(new Sign("solution-custom-"+signs, nf1), ln);
						signs ++;
						// New concept's definition
						new_concept_1.intensionalDefinition().fromExisting(attack, nf1);
						new_concept_1.extensionalDefinition().add_example_set(SetCast.duplicate(one_touched,o));
						new_concept_1.extensionalDefinition().switch_solution(nf1);
						
						// Uncovered
						// New concept and new concept's sign
						Sort s2 = new Sort(training_set.solution_path.features.get(0).get(), null, o);
						FeatureTerm nf2 = new TermFeatureTerm("solution-custom-"+signs, s2);
						System.out.println(">>> We create a new concept Solution-custom-"+signs);
						Concept new_concept_2 = new Concept(new Sign("solution-custom-"+signs, nf2), ln);
						signs ++;
						// New concept's definition
						toLearn.add(new_concept_2);
						new_concept_2.intensionalDefinition().acceptArgument(attack);
						new_concept_2.extensionalDefinition().add_example_set(SetCast.duplicate(one_left,o));
						new_concept_2.extensionalDefinition().switch_solution(nf2);
						
						// Make them brothers
						new_concept_1.setBrother(new_concept_2);
						new_concept_2.setBrother(new_concept_1);
						
						// Accept attack for the covered part (for both attackant and defender)
						defender_accepted.put(final_concepts,new_concept_1);
						buffer.add(new_concept_1);
						defender_still_in_debate.remove(attacked_concept);
						buffer.remove(attacked_concept);
						
						attackant_accepted.put(final_concepts,attackingConcept);
						attackant_still_in_debate.remove(argument);
						
						// Delete the insignificat brother from the debate
						if(argument.hasInsignificantBrother()){
							attackant_still_in_debate.remove(argument.getBrother());
						}
						
						// Uses the uncovered part as a counter-attack
						counterAttacks.add(new_concept_2);
						
						final_concepts ++;
						
					}
					
					else if(multiple_concepts_attacked){
						System.out.println(">>> Multiple concepts attacked by the attack, check indivifually if they are entirely or partially attacked");
						
						// For all the concepts entirely covered, use them as counter-attacks
						counterAttacks.addAll(attacked_concepts_all);
						
						// For the concepts partially covered, split them
						for(Concept c : attacked_concepts_some){
							// Covered
							// New concept and new concept's sign
							Sort s1 = new Sort(training_set.solution_path.features.get(0).get(), null, o);
							FeatureTerm nf1 = new TermFeatureTerm("solution-custom-"+signs, s1);
							System.out.println(">>> We create a new concept Solution-custom-"+signs);
							Concept new_concept_1 = new Concept(new Sign("solution-custom-"+signs, nf1), ln);
							signs ++;
							// New concept's definition
							toLearn.add(new_concept_1);
							new_concept_1.intensionalDefinition().acceptArgument(attack);
							new_concept_1.extensionalDefinition().add_example_set(SetCast.duplicate(multiple_touched.get(c),o));
							new_concept_1.extensionalDefinition().switch_solution(nf1);
							
							// Uncovered
							// New concept and new concept's sign
							Sort s2 = new Sort(training_set.solution_path.features.get(0).get(), null, o);
							FeatureTerm nf2 = new TermFeatureTerm("solution-custom-"+signs, s2);
							System.out.println(">>> We create a new concept Solution-custom-"+signs);
							Concept new_concept_2 = new Concept(new Sign("solution-custom-"+signs, nf2), ln);
							signs ++;
							// New concept's definition
							toLearn.add(new_concept_2);
							new_concept_2.intensionalDefinition().acceptArgument(attack);
							new_concept_2.extensionalDefinition().add_example_set(SetCast.duplicate(multiple_left.get(c),o));
							new_concept_2.extensionalDefinition().switch_solution(nf2);
							
							// Make them brothers
							new_concept_1.setBrother(new_concept_2);
							new_concept_2.setBrother(new_concept_1);
							
							// Delete the insignificat brother from the debate
							if(argument.hasInsignificantBrother()){
								attackant_still_in_debate.remove(argument.getBrother());
							}
						
							// Use both covered and uncovered parts as counter-attacks
							counterAttacks.add(new_concept_1);
							counterAttacks.add(new_concept_2);
						}
					}
					
					else{
						System.out.println(">>> Impossible situation -- Check for debug");
					}
						
				}
			}
			
			// Learning
			Set<FeatureTerm> trainingSet = new HashSet<FeatureTerm>();
			for(Concept c : buffer){
				trainingSet.addAll(c.extensionalDefinition().get_examples());
			}
			for(Concept c : toLearn){
				c.intensionalDefinition().learn(trainingSet);
			}
			
			// Remove meaningless concepts
			HashSet<Concept> meaningless = new HashSet<Concept>();
			if(!counterAttacks.isEmpty()){
				for(Concept c : counterAttacks){
					if(!c.isMeaningful()){
						meaningless.add(c);
					}
				}
			}
			for(Concept c : meaningless){
				System.out.println(">>> Concept "+c.sign().sign()+" is meaningless and will be deleted");
				counterAttacks.remove(c);
			}
		
			
			// Counter attacks are new attacks
			toDebate = counterAttacks;
			if(toDebate.isEmpty() && !defender_still_in_debate.isEmpty()){
				toDebate.add(defender_still_in_debate.getRandomElement());
			}
			
			// INFO
			System.out.println("- - - - - - - - - - ");
			System.out.println("Concept remaining in Agent 1: "+agent_1_to_debate.size());
			System.out.println("Concept remaining in Agent 2: "+agent_2_to_debate.size());
			
			// Continue?
			System.out.println("Continue?");
			String answer = sc.nextLine();
			if(answer.equals("h")){
				HashSet<Concept> toCheck = new HashSet<Concept>();
				toCheck.addAll(agent_1_to_debate);
				toCheck.addAll(agent_2_to_debate);
				for(Concept c : toCheck){
					if(c.isMeaningful()){
						System.out.println("Concept "+c.sign().sign()+" is meaningfull but unreasolvable");
					}
					else{
						System.out.println("Concept "+c.sign().sign()+" is meaningless");
					}
				}
			}
			if(!answer.equals("y")){
				break;
			}
						
			round ++;
			
			// Nothing to debate on
			if(agent_1_to_debate.isEmpty() && agent_2_to_debate.isEmpty()){
				break;
			}
		}
		
		sc.close();
		
		// Creating the missing number of new labels
		System.out.println("- - - - - - - - - - ");
		System.out.println(">>> Creating new labels for concepts");
		
		// Initialize parameters
		signs = 100;
		int final_number_solution = agent_1_new_concept.size();
		
		// Check the number of signs needed
		if(agent_2_new_concept.size() != final_number_solution){
			System.out.println(">>>    Final number of concepts not matching between agents!");
		}
		
		// Make the number of signs needed
		for(int i=0 ; i < final_number_solution ; i++){
			Sort s1 = new Sort(training_set.solution_path.features.get(0).get(), null, o);
			FeatureTerm nf1 = new TermFeatureTerm("solution-custom-"+signs, s1);
			Sign new_solution = new Sign("solution-custom-"+signs, nf1);
			signs ++;
			
			agent_1_new_concept.get(i).sign().changeSign(new_solution);
			agent_2_new_concept.get(i).sign().changeSign(new_solution);
		}
		
		// Give the final statistics
		int total_of_examples = training_set.cases.size();
		int initial_cover_1 = 0;
		int initial_cover_2 = 0;
		int final_cover_1 = 0;
		int final_cover_2 = 0;
		int initial_agree = 0;
		int final_agree = 0;
		
		for(FeatureTerm f : training_set.cases){
			FeatureTerm e = f.featureValue(training_set.description_path.features.get(0));
			Concept problem_1 = null;
			Concept problem_2 = null;
			Concept solution_1 = null;
			Concept solution_2 = null;
			for(Concept c : agent_1_concepts){
				if(c.intensionalDefinition().is_covering(e)){
					initial_cover_1 ++;
					problem_1 = c;
					break;
				}
			}
			for(Concept c : agent_2_concepts){
				if(c.intensionalDefinition().is_covering(e)){
					initial_cover_2 ++;
					problem_2 = c;
					break;
				}
			}
			for(Concept c : agent_1_new_concept.values()){
				if(c.intensionalDefinition().is_covering(e)){
					final_cover_1 ++;
					solution_1 = c;
					break;
				}
			}
			for(Concept c : agent_2_new_concept.values()){
				if(c.intensionalDefinition().is_covering(e)){
					final_cover_2 ++;
					solution_2 = c;
					break;
				}
			}
			if(problem_1 != null && problem_2 != null){
				if(problem_1.sign().sign().equals(problem_2.sign().sign())){
					initial_agree ++;
				}
			}
			if(solution_1 != null && solution_2 != null){
				if(solution_1.sign().sign().equals(solution_2.sign().sign())){
					final_agree ++;
				}
			}
		}
		
		System.out.println("- - - - - - - - - - ");	
		System.out.println("FINAL COUNT");
		System.out.println("# of total examples : "+total_of_examples);
		System.out.println("# of initial covered examples by agent 1 : "+initial_cover_1);
		System.out.println("# of initial covered examples by agent 2 : "+initial_cover_2);
		System.out.println("# of final covered examples by agent 1: "+final_cover_1);
		System.out.println("# of final covered examples by agent 2: "+final_cover_2);
		System.out.println("# of initial agreements : "+initial_agree);
		System.out.println("# of final agreements : "+final_agree);
	}

}
