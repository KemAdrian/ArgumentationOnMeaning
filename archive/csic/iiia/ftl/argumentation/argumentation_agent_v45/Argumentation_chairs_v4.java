package csic.iiia.ftl.argumentation.argumentation_agent_v45;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import csic.iiia.ftl.argumentation.agents.Agent_v4;
import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Agreement;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.Sign;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.SetCast;
import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.learning.core.RuleHypothesis;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;

public class Argumentation_chairs_v4 {
	
	public static void main(String[] arg){
			
		try {
			
			ABUI.ABUI_VERSION = 2;
			ABUI learner = new ABUI();
			
			// Opening of the Cases Set
			int CB1 = TrainingSetUtils.SEAT_1;
			int CB2 = TrainingSetUtils.SEAT_2;
			int TEST = TrainingSetUtils.SEAT_TEST;
			
			Ontology base_ontology;
			Ontology o = new Ontology();
			
			base_ontology = new BaseOntology();
			
			o.uses(base_ontology);
			
			FTKBase dm_1 = new FTKBase();
			FTKBase case_base_1 = new FTKBase();
			FTKBase dm_2 = new FTKBase();
			FTKBase case_base_2 = new FTKBase();			
			FTKBase dm_T = new FTKBase();
			FTKBase case_base_T = new FTKBase();
			
			
			case_base_1.uses(dm_1);
			case_base_2.uses(dm_2);
			case_base_T.uses(dm_T);
			dm_1.create_boolean_objects(o);
			dm_2.create_boolean_objects(o);
			dm_T.create_boolean_objects(o);
			
			TrainingSetProperties training_set_a1 = TrainingSetUtils.loadTrainingSet(CB1, o, dm_1, case_base_1);
			TrainingSetProperties training_set_a2 = TrainingSetUtils.loadTrainingSet(CB2, o, dm_2, case_base_2);
			TrainingSetProperties training_set_test = TrainingSetUtils.loadTrainingSet(TEST, o, dm_T, case_base_T);
	
			LearningPackage l_1 = new LearningPackage(o, dm_1, training_set_a1.description_path, training_set_a1.solution_path, SetCast.cast(training_set_a1.differentSolutions()));
			LearningPackage l_2 = new LearningPackage(o, dm_2, training_set_a2.description_path, training_set_a2.solution_path, SetCast.cast(training_set_a2.differentSolutions()));
			
			LinkedList<FeatureTerm> examples = new LinkedList<FeatureTerm>();
			LinkedList<FeatureTerm> needed = ((LinkedList<FeatureTerm>)training_set_test.differentSolutions());
			
			Agent_v4 agent_1 = new Agent_v4("Paul");
			Agent_v4 agent_2 = new Agent_v4("Roger");
			
			RuleHypothesis h1 = learner.initialTraining(training_set_a1.cases, training_set_a1.differentSolutions(), training_set_a1.description_path, training_set_a1.solution_path, o, dm_1);
			RuleHypothesis h2 = learner.initialTraining(training_set_a2.cases, training_set_a2.differentSolutions(), training_set_a2.description_path, training_set_a2.solution_path, o, dm_2);
			
			agent_1.learnFromHypothesis("old", h1, training_set_a1.cases, l_1);
			agent_2.learnFromHypothesis("old", h2, training_set_a2.cases, l_2);
			
			agent_1.testLearning();
			agent_2.testLearning();
			
			System.out.println("");
			
			needed.addAll(((LinkedList<FeatureTerm>)training_set_test.differentSolutions()));
			Set<FeatureTerm> possible_examples = SetCast.cast(training_set_test.cases);
			
			
			while(!needed.isEmpty()){
				for(FeatureTerm ft : possible_examples){
					FeatureTerm si = needed.removeFirst();
					FeatureTerm sc = ft.featureValue(training_set_test.solution_path.features.get(0));
					if(sc.equals(si)){
						examples.add(ft.featureValue(training_set_test.description_path.features.get(0)));
						break;
					}
					needed.add(si);
				}
			}
	
			agent_1.createBufferContrastSet("new");
			agent_2.createBufferContrastSet("new");
			
			while(!examples.isEmpty()){
				System.out.println(">>>> New round");
				
				FeatureTerm to_debate = examples.remove();
				System.out.println("Argumentation on "+to_debate.toString());
				
				Sign answer_1 = agent_1.nameObject(to_debate, "new");
				Sign answer_2 = agent_2.nameObject(to_debate, "new");
				
				if(answer_1.equals("unknown.unknown")){
					answer_1 = agent_1.nameObject(to_debate, "old");
				}
				
				if(answer_2.equals("unknown.unknown")){
					answer_2 = agent_2.nameObject(to_debate, "old");
				}
				
				System.out.println("Agent 1 uses the "+answer_1.getCake()+", Agent 2 uses the "+answer_2.getCake());
				System.out.println("Agent 1 calls it a "+answer_1.getPiece()+", Agent 2 calls it a "+answer_2.getPiece());
				
				System.out.println("Result of the agremment for Agent 1 : "+agent_1.agree(answer_1, answer_2));
				System.out.println("Result of the agremment for Agent 2 : "+agent_2.agree(answer_1, answer_2));
				
				// If there is a disagreement
				Agent_v4 attacker = agent_2;
				Agent_v4 defender = agent_1;
				
				HashMap<Sign, FeatureTerm> old_attacks = new HashMap<Sign, FeatureTerm>();
				HashMap<Sign, FeatureTerm> new_attacks = new HashMap<Sign, FeatureTerm>();
				
				old_attacks.put(answer_1, to_debate);
				new_attacks.put(answer_2, to_debate);
				
				// Argumentation
				while(!old_attacks.isEmpty()){
					
					// Switching attacker and defender
					if(attacker.equals(agent_1) && defender.equals(agent_2)){
						attacker = agent_2;
						defender = agent_1;
					}
					
					else if(attacker.equals(agent_2) && defender.equals(agent_1)){
						attacker = agent_1;
						defender = agent_2;
					}
					
					// Resolving the current attacks
					for(Sign s : old_attacks.keySet()){
		
						// If there is an agreement :
						if(defender.agree(s, old_attacks.get(s)) != Agreement.Uncorrect){
							
							// If it is an example, we add to the extensional definition
							
							// If it is a generalization, we add to the intensional definition
						}
						
					}
					
					old_attacks = new_attacks;
					new_attacks = new HashMap<Sign, FeatureTerm>();
				
				}
				
				System.out.println("");
	
			}
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
