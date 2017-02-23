package semiotic_elements;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import interfaces.Container;
import interfaces.SemioticElement;
import tools.LearningPackage;

public class Example implements SemioticElement{
	
	public UUID example;
	public FeatureTerm featureterm;
	
	public Example(FeatureTerm f) {
		this.example = UUID.randomUUID();
		if(LearningPackage.initialized())
			try {
				this.featureterm = f.clone(LearningPackage.dm(), LearningPackage.ontology());
			} catch (FeatureTermException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public boolean equals(Example e){
		return this.example.equals(e.example());
	}
	
	public boolean equivalent(Example e) {
		try {
			return this.featureterm.equivalents(e.representation());
		} catch (FeatureTermException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return false;
	}
	
	public UUID example(){
		return this.example;
	}
	
	public FeatureTerm representation(){
		return this.featureterm;
	}
	
	public Set<Example> getExtension(Container c){
		Set<Example> o = new HashSet<>();
		for(Example e : c.getContext()){
			if(e.equals(this)){
				o.add(this);
				return o;
			}	
		}
		return o;
	}
	
	public Example clone(){
		Example out = new Example(this.representation());
		out.example = this.example;
		return out;
	}
	
	public String toString(){
		return this.example.toString();
	}

}
