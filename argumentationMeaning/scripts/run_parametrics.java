package scripts;

import csic.iiia.ftl.learning.core.TrainingSetUtils;
import parametric_scripts.MERGE_parametric_run;

public class run_parametrics {
	
	public static void main(final String[] args) throws Exception{
		
	MERGE_parametric_run.run(TrainingSetUtils.SEAT_ALL, 1., 0.2, 0);
		
	}

}
