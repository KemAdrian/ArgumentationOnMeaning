package tools;

import java.util.Set;
import java.util.UUID;

import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Path;
import csic.iiia.ftl.base.core.Sort;
import csic.iiia.ftl.base.core.Symbol;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.base.utils.SingletonFeatureTermException;

public class LearningPackage {
	
	private static Ontology o;
	private static FTKBase dm;
	private static Path dp,sp;
	private static FeatureTerm generic;
	private static Set<FeatureTerm> different_solutions;
	private static boolean initialized = false;
	
	public static void initialize(FeatureTerm g, Ontology o, FTKBase dm, Path dp, Path sp, Set<FeatureTerm> ds){
		// Ontology information
		LearningPackage.o = o;
		LearningPackage.dm = dm;
		LearningPackage.dp = dp;
		LearningPackage.sp = sp;
		LearningPackage.generic = g;
		LearningPackage.different_solutions = ds;
		LearningPackage.initialized = true;
		
		// New binary levels for discussion
	}
	
	public static FeatureTerm generic(){
		try {
			return generic.clone(dm, o);
		} catch (FeatureTermException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Ontology ontology(){
		return LearningPackage.o;
	}
	
	public static FTKBase dm(){
		return LearningPackage.dm;
	}
	
	public static Path description_path(){
		return LearningPackage.dp;
	}
	
	public static Path solution_path(){
		return LearningPackage.sp;
	}
	
	public static Sort description_sort(){
		try {
			return o.getSort(description_path().getEnd());
		} catch (FeatureTermException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Sort solution_sort(){
		try {
			return o.getSort(solution_path().getEnd());
		} catch (FeatureTermException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Set<FeatureTerm> different_solutions(){
		return LearningPackage.different_solutions;
	}
	
	public static FeatureTerm createFeature(FeatureTerm d, FeatureTerm s){
		TermFeatureTerm f = (TermFeatureTerm) LearningPackage.generic();
		try {
			f.setName(new Symbol(UUID.randomUUID().toString()));
			f.defineFeatureValue(dp, d);
			f.defineFeatureValue(sp, s);
		} catch (SingletonFeatureTermException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FeatureTermException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}
	
	public static boolean initialized(){
		return LearningPackage.initialized;
	}

}
