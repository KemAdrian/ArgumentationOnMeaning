package csic.iiia.ftl.argumentation.conceptconvergence.tools;

import java.util.ArrayList;
import java.util.List;

import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;


public class Testing {
	
public static void main(String[] arg){
		
		try {
			
			// Opening of the Cases Set
			ABUI.ABUI_VERSION = 2;
			
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
			
			System.out.println("size a1 : "+training_set_a1.size());
			System.out.println("size a2 : "+training_set_a2.size());
			System.out.println("size test : "+training_set_test.size());
			
			FeatureTerm f1 = training_set_a1.get(0).featureValue(training_set.solution_path.features.get(0));
			FeatureTerm f3 = training_set_a2.get(0).featureValue(training_set.solution_path.features.get(0));
			FeatureTerm f4 = training_set_test.get(0).featureValue(training_set.solution_path.features.get(0));
			
			int problem = 0;
			
			for(FeatureTerm f2 : training_set_a2){
				
				if(f1.equivalents(f2)){
					problem ++;
				}
				
			}
			System.out.println("Problem f1 vs a2 : "+problem);
			
			problem = 0;
			
			for(FeatureTerm f2 : training_set_test){

				if(f1.equivalents(f2)){
					problem ++;
				}
				
			}
			System.out.println("Problem f1 vs at : "+problem);
			
			problem = 0;
			
			for(FeatureTerm f2 : training_set_a1){

				if(f3.equivalents(f2)){
					problem ++;
				}
				
			}
			System.out.println("Problem f2 vs a1 : "+problem);
			
			problem = 0;
			
			for(FeatureTerm f2 : training_set_test){

				if(f3.equivalents(f2)){
					problem ++;
				}
				
			}
			System.out.println("Problem f2 vs at : "+problem);
			
			problem = 0;
			
			for(FeatureTerm f2 : training_set_a1){

				if(f4.equivalents(f2)){
					problem ++;
				}
				
			}
			System.out.println("Problem ft vs a1 : "+problem);
			
			problem = 0; 
			
			for(FeatureTerm f2 : training_set_a2){
				if(f4.equivalents(f2)){
					problem ++;
				}
				
			}
			System.out.println("Problem ft vs at : "+problem);
						
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
