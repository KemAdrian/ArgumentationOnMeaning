package csic.iiia.ftl.argumentation.argumentation_agreement;

import java.util.ArrayList;
import java.util.List;

import csic.iiia.ftl.argumentation.agents.Agent_v2;
import csic.iiia.ftl.argumentation.conceptconvergence.messages.Universal;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.Sign;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.Token;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;


public class Testing_agreement_with_v2 {
	
	public static int test(Object n1, Object n2){
		return 0;
	}
	
	public static int test(FeatureTerm n1, FeatureTerm n2){
		return 1;
	}
	
	public static void main(String[] arg) throws Exception{
		
		Token t = new Token();
		Agent_v2 nick = new Agent_v2("nick",t);
		
		int CB1 = TrainingSetUtils.SEAT_1;
		int CB2 = TrainingSetUtils.SEAT_2;
		
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
	
		Object test_1 = null;
		Object test_2 = null;
		
		test_1 = training_set_a1.cases.get(0);
		test_2 = training_set_a2.cases.get(0);
		
		List<List<FeatureTerm>> liste = TrainingSetUtils.splitTrainingSet(training_set_a1.cases, 3, training_set_a1.description_path, training_set_a1.solution_path, dm_1, 0, 0);
		
		List<FeatureTerm> overlapping_1 = new ArrayList<FeatureTerm>();
		List<FeatureTerm> overlapping_2 = new ArrayList<FeatureTerm>();
		
		overlapping_1.addAll(liste.get(0));
		overlapping_1.addAll(liste.get(1));
		
		overlapping_2.addAll(liste.get(1));
		overlapping_2.addAll(liste.get(2));
		
		Sign sign = new Sign("test", "test");
		Sign sign2 = new Sign("test", "test2");
		Sign unknown = new Sign("test", "unknown");
		
		Universal m = new Universal(nick,nick,test_1,test_2);
		Universal n = new Universal(nick,nick,test_1,test_1);
		
		Universal p = new Universal(nick, nick, liste.get(0), training_set_a1.cases);
		Universal q = new Universal(nick, nick, training_set_a1.cases, liste.get(0));
		Universal r = new Universal(nick, nick, liste.get(0), liste.get(1));
		Universal s = new Universal(nick, nick, liste.get(1), liste.get(0));
		Universal u = new Universal(nick, nick, liste.get(1), liste.get(1));
		
		Universal v = new Universal(nick, nick, liste.get(0).get(0), liste.get(0));
		Universal w = new Universal(nick, nick, overlapping_1, overlapping_2);
		
		Universal a = new Universal(nick, nick, sign ,liste.get(0));
		Universal b = new Universal(nick, nick, sign ,training_set_a1.cases);
		Universal c = new Universal(nick, nick, sign ,liste.get(0).get(0));
		Universal d = new Universal(nick, nick, unknown ,liste.get(0).get(0));
		Universal e = new Universal(nick, nick, sign , sign2);
		
		nick.associate_set_to_sign(liste.get(0), sign);
		nick.associate_set_to_sign(liste.get(1), sign2);
		nick.start();
		
		System.out.println("------ Examples and Set of Examples ------");
		
		System.out.println("> With objects as parameters");
		nick.get(m);
		nick.get(n);
		t.gives_to(nick);
		Thread.sleep(2000);
		t.remove_from(nick);
		System.out.println("");
		Thread.sleep(500);
		
		System.out.println("> With set and subset as parameters + commutation");
		nick.get(p);
		nick.get(q);
		t.gives_to(nick);
		Thread.sleep(2000);
		t.remove_from(nick);
		System.out.println("");
		Thread.sleep(500);
		
		System.out.println("> Two different sets as parameters + commutation");
		nick.get(r);
		nick.get(s);
		t.gives_to(nick);
		Thread.sleep(2000);
		t.remove_from(nick);
		System.out.println("");
		Thread.sleep(500);
		
		System.out.println("> Same sets as parameters");
		nick.get(u);
		t.gives_to(nick);
		Thread.sleep(2000);
		t.remove_from(nick);
		System.out.println("");
		Thread.sleep(500);
		
		System.out.println("> Object and Set with inclusion as parameters");
		nick.get(v);
		t.gives_to(nick);
		Thread.sleep(2000);
		t.remove_from(nick);
		System.out.println("");
		Thread.sleep(500);
		
		System.out.println("> Overlapping extensional definitions");
		nick.get(w);
		t.gives_to(nick);
		Thread.sleep(2000);
		t.remove_from(nick);
		System.out.println("");
		Thread.sleep(500);
		
		System.out.println("------ Signs and Sets of Examples ------");
		
		System.out.println("> Sign and Set matching");
		nick.get(a);
		t.gives_to(nick);
		Thread.sleep(2000);
		t.remove_from(nick);
		System.out.println("");
		Thread.sleep(500);
		
		System.out.println("> Sign and bigger set");
		nick.get(b);
		t.gives_to(nick);
		Thread.sleep(2000);
		t.remove_from(nick);
		System.out.println("");
		Thread.sleep(500);
		
		System.out.println("> Sign and element from its set");
		nick.get(c);
		t.gives_to(nick);
		Thread.sleep(2000);
		t.remove_from(nick);
		System.out.println("");
		Thread.sleep(500);
		
		System.out.println("> Unknown Sign");
		nick.get(d);
		t.gives_to(nick);
		Thread.sleep(2000);
		t.remove_from(nick);
		System.out.println("");
		Thread.sleep(500);
		
		System.out.println("> Two different signs");
		nick.get(e);
		t.gives_to(nick);
		Thread.sleep(2000);
		t.remove_from(nick);
		System.out.println("");
		Thread.sleep(500);
		
		nick.kill();
		
	}

}
