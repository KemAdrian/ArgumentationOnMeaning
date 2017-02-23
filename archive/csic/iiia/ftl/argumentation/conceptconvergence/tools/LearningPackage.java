package csic.iiia.ftl.argumentation.conceptconvergence.tools;

import java.util.Set;

import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Path;

public class LearningPackage {
	
	private Ontology o;
	private FTKBase dm;
	private Path dp,sp;
	private Set<FeatureTerm> different_solutions;
	
	public LearningPackage(Ontology o, FTKBase dm, Path dp, Path sp, Set<FeatureTerm> ds){
		this.o = o;
		this.dm = dm;
		this.dp = dp;
		this.sp = sp;
		this.different_solutions = ds;
	}
	
	public Ontology ontology(){
		return o;
	}
	
	public FTKBase dm(){
		return dm;
	}
	
	public Path description_path(){
		return dp;
	}
	
	public Path solution_path(){
		return sp;
	}
	
	public Set<FeatureTerm> different_solutions(){
		return this.different_solutions;
	}
	

}
