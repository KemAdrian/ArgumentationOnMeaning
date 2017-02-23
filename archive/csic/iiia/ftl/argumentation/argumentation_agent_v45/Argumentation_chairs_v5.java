package csic.iiia.ftl.argumentation.argumentation_agent_v45;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import csic.iiia.ftl.argumentation.agents.Agent_v5;
import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Agreement;
import csic.iiia.ftl.argumentation.conceptconvergence.Interfaces.Agent;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.Concept;
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

public class Argumentation_chairs_v5 {
	
	public static void main(String[] arg){
			
		try {
			
			// Opening of the Cases Set
			ABUI.ABUI_VERSION = 2;
			ABUI learner = new ABUI();
			
			// Opening of the Cases Set
			int TEST = TrainingSetUtils.SEAT_ALL;
			
			Ontology base_ontology;
			Ontology o = new Ontology();
			
			base_ontology = new BaseOntology();
			
			o.uses(base_ontology);
			
			FTKBase dm = new FTKBase();
			FTKBase case_base = new FTKBase();
			
			
			case_base.uses(dm);
			dm.create_boolean_objects(o);;
			
			
			TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(TEST, o, dm, case_base);
			List<List<FeatureTerm>> training_tests = new ArrayList<List<FeatureTerm>>();
			
			for(int i=0; i<3; i++){
				ArrayList<FeatureTerm> hey = new ArrayList<FeatureTerm>();
				training_tests.add(hey);
			}
			
			for(FeatureTerm e : training_set.cases){
				if(Integer.parseInt(e.getName().toString().replace("e","")) < 50){
					training_tests.get(0).add(e);
				}
				else if(Integer.parseInt(e.getName().toString().replace("e","")) < 100){
					training_tests.get(1).add(e);
				}
				else{
					training_tests.get(2).add(e);
				}
			}
		
			
			List<FeatureTerm> training_set_a1 = training_tests.get(0);
			List<FeatureTerm> training_set_a2 = training_tests.get(1);
			List<FeatureTerm> training_set_test = training_tests.get(2);
	
			LearningPackage ln = new LearningPackage(o, dm, training_set.description_path, training_set.solution_path, SetCast.cast(training_set.differentSolutions()));
			
			LinkedList<FeatureTerm> examples = new LinkedList<FeatureTerm>();
			LinkedList<FeatureTerm> solutions = new LinkedList<FeatureTerm>();
			LinkedList<FeatureTerm> needed = new LinkedList<FeatureTerm>();
			
			needed.addAll(ln.different_solutions());
			needed.addAll(ln.different_solutions());
			
			Agent_v5 agent_1 = new Agent_v5("adam");
			Agent_v5 agent_2 = new Agent_v5("boby");
			
			RuleHypothesis h1 = learner.initialTraining(training_set_a1, ln.different_solutions(), ln.description_path(), ln.solution_path(), o, dm);
			RuleHypothesis h2 = learner.initialTraining(training_set_a2, ln.different_solutions(), ln.description_path(), ln.solution_path(), o, dm);
			
			agent_1.learnFromHypothesis(h1, training_set_a1, ln);
			agent_2.learnFromHypothesis(h2, training_set_a2, ln);
			
			Set<FeatureTerm> possible_examples = SetCast.cast(training_set_test);
			
			while(!needed.isEmpty()){
				for(FeatureTerm ft : possible_examples){
					FeatureTerm si = needed.removeFirst();
					FeatureTerm sc = ft.featureValue(ln.solution_path().features.get(0));

					if(sc.equals(si)){
						examples.add(ft.featureValue(ln.description_path().features.get(0)));
						solutions.add(ft.featureValue(ln.solution_path().features.get(0)));
						break;
					}
					needed.add(si);
				}
			}
			
			while(!examples.isEmpty()){
				
				boolean isOver = false;
				
				System.out.println(">>>> New round");
				
				System.out.println(">>>> The expected solution is : "+solutions.remove().toStringNOOS(dm));
				
				FeatureTerm to_debate = examples.remove();

				// Decides who is starting (reversed)
				Agent_v5 attacker = agent_1;
				Agent_v5 defender = agent_2;
				
				// Check if there a disagreement (reversed)
				Sign proposal = attacker.nameObject(to_debate, "initial");
				
				if(defender.agree(to_debate, proposal) == Agreement.True || defender.agree(to_debate, proposal) == Agreement.Correct){
					System.out.println(">>>> The defender agreed on the attacker making the proposal "+proposal.getPiece()+" for the feature-term");
					isOver = true;
				}
				
				else{
					
					// Get the counter proposal
					Sign counter_proposal = defender.nameObject(to_debate, "initial");
					
					// Alarm about the disagreement
					System.out.println(">>>> There is a disagreement : Attacker proposes "+proposal.getPiece()+" but Defender would rather propose "+counter_proposal.getPiece());
					
					// Put the first(s) concept(s)
					attacker.addAssociation(counter_proposal, to_debate, true);
					defender.addAssociation(counter_proposal, to_debate, false);
				}
				
				HashMap<Agent, Integer> toEnd = new HashMap<Agent, Integer>();
				toEnd.put(agent_1, 0);
				toEnd.put(agent_2, 0);
				
				// Start the communication
				while(!isOver){
					
					// Switching attacker and defender
					if(attacker.equals(agent_1) && defender.equals(agent_2)){
						attacker = agent_2;
						defender = agent_1;
					}
					
					else if(attacker.equals(agent_2) && defender.equals(agent_1)){
						attacker = agent_1;
						defender = agent_2;
					}
					
					// Detecting disagreement
					boolean accept = true;
					
					// Synchronic disagreement
					for(FeatureTerm f1 : defender.getBuffer().getContext()){
						if(defender.getBuffer().getConcepts(f1).size() != 0){
							accept = false;
						}
					}
					
					// Diachronic disagreement
					Collection<Concept> initial_concepts = new HashSet<Concept>();
					Collection<Concept> buffer_concepts = new HashSet<Concept>();
					initial_concepts.addAll(defender.getInitialConcepts());
					buffer_concepts.addAll(defender.getBufferConcepts());
					
					for(Concept c1 : buffer_concepts){
						for(Concept c2 : initial_concepts){
							if(defender.agree(c1.Sign(), c2.Sign()) == Agreement.False){
								accept = false;
								for(FeatureTerm r : c2.IntensionalDefinition()){
									attacker.addAssociation(new Sign(), r, true);
									defender.addAssociation(new Sign(), r, false);
								}
							}
						}
					}
					
					// Ending the argumentation
					if(accept){
						toEnd.put(defender, 1);
					}
					
					// Keeping the argumentation going
					/*else{
						
						// List all the disagreements and the treatment to operate
						for(Case ca : defender.getBuffer().getDisagreements(Problem.Overlap)){
									
						}
						
						for(Case ca : defender.getBuffer().getDisagreements(Problem.Hypernym)){
							
						}
						
						for(Case ca : defender.getBuffer().getDisagreements(Problem.Synonym)){
							
						}
						
					}*/
					
					if(toEnd.get(agent_1) == 1 && toEnd.get(agent_2) == 1){
						System.out.println(">>>>> Exit");
						isOver = true;
					}
				
				}
				
				System.out.println("");
	
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
