package csic.iiia.ftl.argumentation.conceptconvergence.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Path;
import csic.iiia.ftl.base.utils.FeatureTermException;

public class TestTrainingSet {
	
	public static HashMap<String, Integer> nb_solution(List<FeatureTerm> examples, Path sp, FTKBase dm, int enumerate) throws FeatureTermException{
		
		HashMap<String, Integer> output = new HashMap<String, Integer>();
		
		//List<String> output = new ArrayList<String>();
		
		for(FeatureTerm f : examples){
			if(!output.containsKey(f.readPath(sp).toStringNOOS(dm))){
				output.put(f.readPath(sp).toStringNOOS(dm),1);
			}
			else{
				output.put(f.readPath(sp).toStringNOOS(dm), output.get(f.readPath(sp).toStringNOOS(dm))+1);
			}
		}
		
		if(enumerate > 0){
			for (Iterator<Entry<String, Integer>> iterator = output.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, Integer> e = iterator.next();
				System.out.println("For the solution "+e.getKey()+" there is "+e.getValue()+" examples");
			}
		}
		
		return output;
		
	}
	
public static Set<FeatureTerm> diff_solution(List<FeatureTerm> examples, Path sp, FTKBase dm) throws FeatureTermException{
		
		Set<FeatureTerm> output = new HashSet<FeatureTerm>();
		
		//List<String> output = new ArrayList<String>();
		
		for(FeatureTerm f : examples){
			if(!output.contains(f.readPath(sp))){
				output.add(f.readPath(sp));
			}
		}
		
		return output;
		
	}

}
