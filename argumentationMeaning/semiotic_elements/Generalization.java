package semiotic_elements;

import java.util.HashSet;
import java.util.Set;

import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import interfaces.Container;
import interfaces.SemioticElement;
import tools.LearningPackage;

public class Generalization implements SemioticElement{
	
	public FeatureTerm generalization;
	
	public Generalization(FeatureTerm g) throws FeatureTermException{
		if(LearningPackage.initialized())
			this.generalization = g.clone(LearningPackage.dm(), LearningPackage.ontology());
	}
	
	public FeatureTerm generalization(){
		return this.generalization;
	}
	
	public boolean equals(Generalization g) throws FeatureTermException{
		return this.generalization.equivalents(g.generalization());
	}
	
	public boolean generalizes(Example e) throws FeatureTermException{
		return this.generalization().subsumes(e.representation());
	}
	
	public boolean generalizes(Set<Example> E) throws FeatureTermException{
		for(Example e : E){
			if(!this.generalization().subsumes(e.representation()))
				return false;
		}
		return true;
	}
	
	public Set<Example> getExtension(Container c){
		HashSet<Example> o = new HashSet<Example>();
		for(Example e : c.getContext()){
			try {
				if(this.generalizes(e))
					o.add(e);
			} catch (FeatureTermException e1) {
				e1.printStackTrace();
			}
		}
		return o;
	}
	
	public Generalization clone(){
		try {
			return new Generalization(this.generalization());
		} catch (FeatureTermException e) {
			e.printStackTrace();
		}
		return null;
	}

}
