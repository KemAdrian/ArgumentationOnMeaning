package csic.iiia.ftl.argumentation.argumentation_cross_exam;

import java.util.HashSet;
import java.util.Set;

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
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;


public class CrossExam_Chairs {
	
	public static void main(String[] arg) throws Exception{
		
		int CB1 = TrainingSetUtils.SEAT_1;
		int CB2 = TrainingSetUtils.SEAT_2;
		int CBT = TrainingSetUtils.SEAT_TEST;
		
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
		TrainingSetProperties training_set_test = TrainingSetUtils.loadTrainingSet(CBT, o, dm_T, case_base_T);
		
		LearningPackage ln1 = new LearningPackage(o, dm_1, training_set_a1.description_path, training_set_a1.solution_path, SetCast.cast(training_set_a1.differentSolutions()));
		LearningPackage ln2 = new LearningPackage(o, dm_1, training_set_a2.description_path, training_set_a2.solution_path, SetCast.cast(training_set_a2.differentSolutions()));
		
		ABUI.ABUI_VERSION = 2;

		HashSet<Concept> agent_1_concepts = new HashSet<Concept>();
		HashSet<Concept> agent_2_concepts = new HashSet<Concept>();
		
		// Learning agent 1
		for(FeatureTerm s : training_set_a1.differentSolutions()){
			Sign s1 = new Sign(s.toString(), s);
			Concept c = new Concept(s1, ln1);
			Set<FeatureTerm> training = SetCast.duplicate(training_set_a1.cases);
			c.intensionalDefinition().learn(training);
			c.extensionalDefinition().creates_from_intensional(SetCast.duplicate(training_set_a1.cases));
			agent_1_concepts.add(c);
		}
		
		// Learning agent 2
		for(FeatureTerm s : training_set_a2.differentSolutions()){
			Sign s1 = new Sign(s.toString(), s);
			Concept c = new Concept(s1, ln2);
			if(c.intensionalDefinition().learn(SetCast.duplicate(training_set_a2.cases)) < 1){
				c.extensionalDefinition().creates_from_intensional(SetCast.duplicate(training_set_a2.cases));
				agent_2_concepts.add(c);
			}
		}
		
		// Making the new subsets
		TwoKeyMap map = new TwoKeyMap();
		for(FeatureTerm f : training_set_test.cases){
			FeatureTerm e = f.featureValue(training_set_test.description_path.features.get(0));
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
			map.put(k1, k2, e);
		}
		
		System.out.println(map.size());
		
		

		
		/*for(Concept c : agent_2_concepts){
			c.intensionalDefinition().tell_rules();
		}*/
		
		// Making the subparts of the concepts' conjunction
		
		
		
	}

}
