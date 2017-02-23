package sandboxes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import agents.Agent_simple;
import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Sign;
import tools.LearningPackage;

public class AgreementAgentSandbox {
	
	public static int test(Object n1, Object n2){
		return 0;
	}
	
	public static int test(FeatureTerm n1, FeatureTerm n2){
		return 1;
	}
	
	public static void main(String[] arg) throws Exception{
		
		int CB = TrainingSetUtils.SEAT_TEST;

		Agent_simple nick = new Agent_simple();
		Ontology base_ontology;
		Ontology o = new Ontology();
		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();
		
		base_ontology = new BaseOntology();
		o.uses(base_ontology);
		case_base.uses(dm);
		dm.create_boolean_objects(o);
		
		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(CB, o, dm, case_base);
		
		ABUI.ABUI_VERSION = 2;

		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0);
		g.setName(null);
		g.defineFeatureValue(training_set.description_path.getEnd(), null);
		g.defineFeatureValue(training_set.solution_path.getEnd(), null);
		
		LearningPackage.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<>(training_set.differentSolutions()));
		nick.initialize(training_set.cases);
		
		HashMap<String, List<Example>> liste = new HashMap<String, List<Example>>();
		LinkedList<String> different_solutions = new LinkedList<>();
		liste.put("all", new ArrayList<Example>());
		for(Concept c : nick.Kc.getAllConcepts()){
			different_solutions.add(c.sign());
			liste.get("all").addAll(c.extensional_definition());
			liste.put(c.sign(), new ArrayList<>(c.extensional_definition()));
		}
		
		String S1 = different_solutions.removeFirst();
		String S2 = different_solutions.removeFirst();
		String S3 = different_solutions.removeFirst();
		
		Example test_1 = liste.get(S1).get(0);
		Example test_2 = liste.get(S2).get(1);
		
		List<Example> overlapping_1 = new ArrayList<Example>();
		List<Example> overlapping_2 = new ArrayList<Example>();
		
		overlapping_1.addAll(liste.get(S1));
		overlapping_1.addAll(liste.get(S2));
		
		overlapping_2.addAll(liste.get(S2));
		overlapping_2.addAll(liste.get(S3));
		
		Sign sign = new Sign(S1);
		Sign sign2 = new Sign(S2);
		Sign unknown = new Sign("unknown");
		
		System.out.println("------ Examples and Set of Examples ------");
		
		System.out.println("> With objects as parameters");
		System.out.println(nick.agree(test_1,test_2));
		System.out.println(nick.agree(test_1, test_1));
		System.out.println("");
		
		System.out.println("> With set and subset as parameters + commutation");
		System.out.println(nick.agree(new HashSet<>(liste.get(S1)), new HashSet<>(liste.get("all"))));
		System.out.println(nick.agree(new HashSet<>(liste.get("all")), new HashSet<>(liste.get(S1))));
		System.out.println("");
		
		System.out.println("> Two different sets as parameters + commutation");
		System.out.println(nick.agree(new HashSet<>(liste.get(S1)), new HashSet<>(liste.get(S2))));
		System.out.println(nick.agree(new HashSet<>(liste.get(S2)), new HashSet<>(liste.get(S1))));
		System.out.println("");
		
		System.out.println("> Same sets as parameters");
		System.out.println(nick.agree(new HashSet<>(liste.get(S1)), new HashSet<>(liste.get(S1))));
		System.out.println("");
		
		System.out.println("> Object and Set with inclusion as parameters");
		System.out.println(nick.agree(liste.get(S1).get(0), new HashSet<>(liste.get(S1))));
		System.out.println("");
		
		System.out.println("> Overlapping extensional definitions");
		System.out.println(nick.agree(new HashSet<>(overlapping_1), new HashSet<>(overlapping_2)));
		System.out.println("");
		
		System.out.println("------ Signs and Sets of Examples ------");
		
		System.out.println("> Sign and Set matching");
		System.out.println(nick.agree(sign ,new HashSet<>(liste.get(S1))));
		System.out.println("");
		
		System.out.println("> Sign and bigger set + commutation");
		System.out.println(nick.agree(sign ,new HashSet<>(liste.get("all"))));
		System.out.println(nick.agree(sign ,new HashSet<>(liste.get("all"))));
		System.out.println("");
		
		System.out.println("> Sign and element from its set + commutation");
		System.out.println(nick.agree(sign ,liste.get(S1).get(0)));
		System.out.println(nick.agree(liste.get(S1).get(0) ,sign));
		System.out.println("");
		
		System.out.println("> Unknown Sign");
		System.out.println(nick.agree(unknown ,liste.get(S1).get(0)));
		System.out.println("");
		
		System.out.println("> Two different signs");
		System.out.println(nick.agree(sign , sign2));
		System.out.println("");
		
	}

}
