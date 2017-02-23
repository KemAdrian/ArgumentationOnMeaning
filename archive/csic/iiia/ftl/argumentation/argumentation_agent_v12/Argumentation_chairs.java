package csic.iiia.ftl.argumentation.argumentation_agent_v12;

import java.util.LinkedList;
import java.util.Set;

import csic.iiia.ftl.argumentation.agents.Agent_v1;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Concept;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.SetCast;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.Token;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;

public class Argumentation_chairs {
	
public static void main(String[] arg){
		
	try {
		
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
		
		Agent_v1.DEBUG = 2;
		Concept.DEBUG = true;
		
		Token t = new Token();

		LearningPackage l_1 = new LearningPackage(o, dm_1, training_set_a1.description_path, training_set_a1.solution_path, SetCast.cast(training_set_a1.differentSolutions()));
		Agent_v1 agent_1 = new Agent_v1("Paul",SetCast.cast(training_set_a1.cases), l_1, t);
		LearningPackage l_2 = new LearningPackage(o, dm_2, training_set_a2.description_path, training_set_a2.solution_path, SetCast.cast(training_set_a2.differentSolutions()));
		Agent_v1 agent_2 = new Agent_v1("Jean",SetCast.cast(training_set_a2.cases), l_2, t);
		
		agent_1.start();
		agent_2.start();
		agent_1.meet(agent_2);
		agent_2.meet(agent_1);
		
		LinkedList<FeatureTerm> examples = new LinkedList<FeatureTerm>();
		LinkedList<FeatureTerm> needed = ((LinkedList<FeatureTerm>)training_set_test.differentSolutions());
		
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
		

		while(!examples.isEmpty()){
			Thread.sleep(20);
			if(agent_1.is_satisfied() && agent_2.is_satisfied() && !t.is_owned(agent_1) && !t.is_owned(agent_2)){
				FeatureTerm e = examples.removeFirst();
				System.out.println("we present an example, "+examples.size()+" examples remaining");
				agent_1.isPresented(e);
				agent_2.isPresented(e);
				t.gives_to(agent_1);
				System.out.println("the token is given");
			}
		}
		
		Thread.sleep(1000);
		
		agent_1.kill();
		agent_2.kill();
		
		
	} catch (Exception e) {
		e.printStackTrace();
	}
	
}
}
