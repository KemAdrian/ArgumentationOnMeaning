package csic.iiia.ftl.argumentation.conceptconvergence.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.utils.FeatureTermException;

public abstract class SetCast {
	
	public static Set<FeatureTerm> cast(List<FeatureTerm> list){
		Set<FeatureTerm> output = new HashSet<FeatureTerm>();
		while(!list.isEmpty()){
			output.add(list.remove(0));
		}
		return output;
	}
	
	public static ArrayList<FeatureTerm> cast(Set<FeatureTerm> set){
		ArrayList<FeatureTerm> output = new ArrayList<FeatureTerm>();
		for(FeatureTerm f : set){
			output.add(f);
		}
		return output;
	}
	
	public static Set<FeatureTerm> duplicate(List<FeatureTerm> list){
		Set<FeatureTerm> output = new HashSet<FeatureTerm>();
		for(int i = 0; i<list.size(); i++){
			output.add(list.get(i));
		}
		return output;
	}
	
	public static Set<FeatureTerm> duplicate(Set<FeatureTerm> set, Ontology o) throws FeatureTermException{
		Set<FeatureTerm> output = new HashSet<FeatureTerm>();
		for(FeatureTerm f : set){
			output.add(f.clone(o));
		}
		return output;
	}
 
}
