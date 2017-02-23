package csic.iiia.ftl.argumentation.argumentation_cross_exam;

import java.util.List;

import csic.iiia.ftl.argumentation.agents.Agent_v3;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic.Concept;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.SetCast;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;

public class CrossExam_Agents {
	
	public static void main(String[] arg) throws Exception{
		
		int CB = TrainingSetUtils.ZOOLOGY_DATASET_LB;

		// Creating Onltology material	
		Ontology base_ontology;
		Ontology o = new Ontology();
		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();
		
		// Intstantiating Ontology material
		base_ontology = new BaseOntology();
		o.uses(base_ontology);
		case_base.uses(dm);
		dm.create_boolean_objects(o);
		
		
		// Creating Data Sets
		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(CB, o, dm, case_base);
		LearningPackage ln = new LearningPackage(o, dm, training_set.description_path, training_set.solution_path, SetCast.cast(training_set.differentSolutions()));
		List<List<FeatureTerm>> training_sets = TrainingSetUtils.splitTrainingSet(training_set.cases, 2, training_set.description_path, training_set.solution_path, dm, 0, 0);
		
		
		// Creating Agents
		Concept.DEBUG = true;
		Agent_v3 agent1 = new Agent_v3();
		Agent_v3 agent2 = new Agent_v3();

		// Learning Agent 1
		System.out.println("- - - - LEARNING AGENT 1 - - - -");
		agent1.Learn(SetCast.duplicate(training_sets.get(0)), ln);
		// Learning Agent 2
		System.out.println("- - - - LEARNING AGENT 2 - - - -");
		agent2.Learn(SetCast.duplicate(training_sets.get(1)), ln);
		
		
		// Making new subsets
		agent1.start();
		agent2.start();
		agent1.meet(agent2);
		agent2.meet(agent1);
		agent1.getToken();
		
		
	}

}
