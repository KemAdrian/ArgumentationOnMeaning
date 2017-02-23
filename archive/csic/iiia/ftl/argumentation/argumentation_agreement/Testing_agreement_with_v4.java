package csic.iiia.ftl.argumentation.argumentation_agreement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import csic.iiia.ftl.argumentation.agents.Agent_v4;
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


public class Testing_agreement_with_v4 {
	
	public static int test(Object n1, Object n2){
		return 0;
	}
	
	public static int test(FeatureTerm n1, FeatureTerm n2){
		return 1;
	}
	
	public static void main(String[] arg) throws Exception{
		
		int CB = TrainingSetUtils.SEAT_TEST;

		Agent_v4 nick = new Agent_v4("nick");
		Ontology base_ontology;
		Ontology o = new Ontology();
		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();
		
		base_ontology = new BaseOntology();
		o.uses(base_ontology);
		case_base.uses(dm);
		dm.create_boolean_objects(o);
		
		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(CB, o, dm, case_base);
		
		HashMap<String, List<FeatureTerm>> liste = new HashMap<String, List<FeatureTerm>>();
		liste.put("all", new ArrayList<FeatureTerm>());
		
		for(FeatureTerm s : training_set.differentSolutions()){
			ArrayList<FeatureTerm> cases = new ArrayList<FeatureTerm>();
			for(FeatureTerm e : training_set.cases){
				if(e.featureValue(training_set.solution_path.features.get(0)).equals(s)){
					cases.add(e.featureValue(training_set.description_path.features.get(0)));
					liste.get("all").add(e.featureValue(training_set.description_path.features.get(0)));
				}
			}
			liste.put(s.toStringNOOS(dm), cases);
		}
		
		FeatureTerm s1 = training_set.differentSolutions().get(0);
		FeatureTerm s2 = training_set.differentSolutions().get(1);
		FeatureTerm s3 = training_set.differentSolutions().get(2);
		
		String S1 = s1.toStringNOOS(dm);
		String S2 = s2.toStringNOOS(dm);
		String S3 = s3.toStringNOOS(dm);
		
		FeatureTerm test_1 = liste.get(S1).get(0);
		FeatureTerm test_2 = liste.get(S2).get(1);
		
		List<FeatureTerm> overlapping_1 = new ArrayList<FeatureTerm>();
		List<FeatureTerm> overlapping_2 = new ArrayList<FeatureTerm>();
		
		overlapping_1.addAll(liste.get(S1));
		overlapping_1.addAll(liste.get(S2));
		
		overlapping_2.addAll(liste.get(S2));
		overlapping_2.addAll(liste.get(S3));
		
		Sign sign = new Sign("seat", S1);
		Sign sign2 = new Sign("seat", S2);
		Sign unknown = new Sign("seat", "unknown");
		
		ABUI.ABUI_VERSION = 2;
		ABUI learner = new ABUI();

		RuleHypothesis hypothesis = learner.initialTraining(training_set.cases, training_set.differentSolutions(), training_set.description_path, training_set.solution_path, o, dm);
		nick.learnFromHypothesis("seat", hypothesis, training_set.cases, new LearningPackage(o, dm, training_set.description_path, training_set.solution_path, SetCast.cast((training_set.differentSolutions()))));
		
		System.out.println("------ Examples and Set of Examples ------");
		
		System.out.println("> With objects as parameters");
		System.out.println(nick.agree(test_1,test_2));
		System.out.println(nick.agree(test_1, test_1));
		System.out.println("");
		
		System.out.println("> With set and subset as parameters + commutation");
		System.out.println(nick.agree(liste.get(S1), liste.get("all")));
		System.out.println(nick.agree(liste.get("all"), liste.get(S1)));
		System.out.println("");
		
		System.out.println("> Two different sets as parameters + commutation");
		System.out.println(nick.agree(liste.get(S1), liste.get(S2)));
		System.out.println(nick.agree(liste.get(S2), liste.get(S1)));
		System.out.println("");
		
		System.out.println("> Same sets as parameters");
		System.out.println(nick.agree(liste.get(S1), liste.get(S1)));
		System.out.println("");
		
		System.out.println("> Object and Set with inclusion as parameters");
		System.out.println(nick.agree(liste.get(S1).get(0), liste.get(S1)));
		System.out.println("");
		
		System.out.println("> Overlapping extensional definitions");
		System.out.println(nick.agree(overlapping_1, overlapping_2));
		System.out.println("");
		
		System.out.println("------ Signs and Sets of Examples ------");
		
		System.out.println("> Sign and Set matching");
		System.out.println(nick.agree(sign ,liste.get(S1)));
		System.out.println("");
		
		System.out.println("> Sign and bigger set");
		System.out.println(nick.agree(sign ,liste.get("all")));
		System.out.println("");
		
		System.out.println("> Sign and element from its set");
		System.out.println(nick.agree(sign ,liste.get(S1).get(0)));
		System.out.println("");
		
		System.out.println("> Unknown Sign");
		System.out.println(nick.agree(unknown ,liste.get(S1).get(0)));
		System.out.println("");
		
		System.out.println("> Two different signs");
		System.out.println(nick.agree(sign , sign2));
		System.out.println("");
		
	}

}
