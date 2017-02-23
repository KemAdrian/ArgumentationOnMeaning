package csic.iiia.ftl.argumentation.argumentation_agent_v12;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import csic.iiia.ftl.argumentation.agents.Agent_v1;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Concept;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.IntensionalDefinition;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.SetCast;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.TestTrainingSet;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.Token;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;

public class Argumenation_animals_v2 {
	
public static void main(String[] arg){
		
		try {
			
			// Opening of the Cases Set
			int CB = TrainingSetUtils.ZOOLOGY_DATASET;
			
			Ontology base_ontology;
			Ontology o = new Ontology();
			
			base_ontology = new BaseOntology();
			
			o.uses(base_ontology);
			
			FTKBase dm_1 = new FTKBase();
			FTKBase case_base_1 = new FTKBase();
			
			case_base_1.uses(dm_1);
			dm_1.create_boolean_objects(o);
			
			TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(CB, o, dm_1, case_base_1);
			training_set.cases.addAll(training_set.cases);
			
			System.out.println(training_set.cases.size());
			
			List<List<FeatureTerm>> training_sets = TrainingSetUtils.splitTrainingSet(training_set.cases, 3, training_set.description_path, training_set.solution_path, dm_1, 2, 4);

			System.out.println(training_sets.get(0).size());
			System.out.println(training_sets.get(1).size());
			System.out.println(training_sets.get(2).size());
			
			
			List<FeatureTerm> training_set_a1 = training_sets.get(0);
			List<FeatureTerm> training_set_a2 = training_sets.get(1);
			List<FeatureTerm> training_set_test = training_sets.get(2);

			/*System.out.println(training_set_a1.size());
			System.out.println(training_set_a2.size());
			System.out.println(training_set_test.size());*/
			
			Agent_v1.DEBUG = 1;
			IntensionalDefinition.DEBUG = 1;
			Concept.DEBUG = true;
			
			Token t = new Token();

			LearningPackage l_1 = new LearningPackage(o, dm_1, training_set.description_path, training_set.solution_path, TestTrainingSet.diff_solution(training_set_a1, training_set.solution_path, dm_1));
			Agent_v1 agent_1 = new Agent_v1("Paul",SetCast.cast(training_set_a1), l_1, t);
			LearningPackage l_2 = new LearningPackage(o, dm_1, training_set.description_path, training_set.solution_path, TestTrainingSet.diff_solution(training_set_a2, training_set.solution_path, dm_1));
			Agent_v1 agent_2 = new Agent_v1("Jean",SetCast.cast(training_set_a2), l_2, t);
			
			agent_1.start();
			agent_2.start();
			agent_1.meet(agent_2);
			agent_2.meet(agent_1);
			
			LinkedList<FeatureTerm> examples = new LinkedList<FeatureTerm>();
			
			LinkedList<FeatureTerm> needed = ((LinkedList<FeatureTerm>)training_set.differentSolutions());
			Set<FeatureTerm> possible_examples = SetCast.cast(training_set_test);
			
			
			while(!needed.isEmpty()){
				for(FeatureTerm ft : possible_examples){
					FeatureTerm si = needed.removeFirst();
					FeatureTerm sc = ft.readPath(training_set.solution_path);
					if(sc.equals(si)){
						examples.add(ft);
						break;
					}
					needed.add(si);
				}
			}
			
			while(!examples.isEmpty()){
				Thread.sleep(200);
				if(agent_1.is_satisfied() && agent_2.is_satisfied() && !t.is_owned(agent_1) && !t.is_owned(agent_2)){
					FeatureTerm e = examples.removeFirst();
					System.out.println("we present an example of "+ e.readPath(training_set.solution_path).toStringNOOS(dm_1) +", "+examples.size()+" examples remaining");
					agent_1.isPresented(e.readPath(training_set.description_path));
					agent_2.isPresented(e.readPath(training_set.description_path));
					t.gives_to(agent_1);
					System.out.println("the token is given");
				}
			}
			
			agent_1.kill();
			agent_2.kill();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
