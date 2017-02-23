package sandboxes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import tools.LearningPackage;

public class Sandbox {
	
	public static void main(String[] args) throws FeatureTermException, IOException {

		// Opening of the Cases Set
		int TEST = TrainingSetUtils.SEAT_ALL;

		Ontology base_ontology;
		Ontology o = new Ontology();

		base_ontology = new BaseOntology();

		o.uses(base_ontology);

		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();

		case_base.uses(dm);
		dm.create_boolean_objects(o);
		;

		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(TEST, o, dm, case_base);
		List<List<FeatureTerm>> training_tests = new ArrayList<List<FeatureTerm>>();

		// To use when we have the seat dataset in input
		for (int i = 0; i < 3; i++) {
			ArrayList<FeatureTerm> hey = new ArrayList<FeatureTerm>();
			training_tests.add(hey);
		}

		for (FeatureTerm e : training_set.cases) {
			if (Integer.parseInt(e.getName().toString().replace("e", "")) < 50) {
				training_tests.get(0).add(e);
			} else if (Integer.parseInt(e.getName().toString().replace("e", "")) < 100) {
				training_tests.get(1).add(e);
			} else {
				training_tests.get(2).add(e);
			}
		}
		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);
		g.setName(null);
		g.defineFeatureValue(training_set.description_path.getEnd(), null);
		g.defineFeatureValue(training_set.solution_path.getEnd(), null);
		
		LearningPackage.initialize(g, o, dm, training_set.description_path, training_set.solution_path,new HashSet<>(training_set.differentSolutions()));
		
		//training_set.cases.get(0)

		
	}

}
