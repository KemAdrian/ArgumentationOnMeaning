package semiotic_elements;

import java.util.HashSet;
import java.util.Set;

import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import interfaces.Agent;
import interfaces.Container;
import interfaces.SemioticElement;
import tools.LearningPackage;

/**
 * The component of the intensional definition. It represents the formalized knowledge that an {@link Agent} has about a {@link Concept}.
 * 
 * @author kemoadrian
 *
 */
public class Generalization implements SemioticElement{
	
	public FeatureTerm generalization;
	
	/**
	 * Create a new {@link Generalization} from a {@link FeatureTerm}
	 * @param g a {@link FeatureTerm} that subsumes {@link Example}s' {@link FeatureTerm} from the extensional definition.
	 * @throws FeatureTermException
	 */
	public Generalization(FeatureTerm g) throws FeatureTermException{
		if(LearningPackage.initialized())
			this.generalization = g.clone(LearningPackage.dm(), LearningPackage.ontology());
	}
	
	/**
	 * Give the {@link FeatureTerm} related to this {@link Generalization}.
	 * @return the {@link FeatureTerm}.
	 */
	public FeatureTerm generalization(){
		return this.generalization;
	}
	
	/**
	 * Test if the {@link FeatureTerm} of an other {@link Generalization} is equivalent to this one's.
	 * This method uses the method {@link #equivalents(FeatureTerm) equivalents} of the class {@link FeatureTerm}.
	 * @param g the other {@link Generalization}.
	 * @return <tt>true</tt> if the {@link FeatureTerm} are equivalent.
	 * @throws FeatureTermException
	 */
	public boolean equals(Generalization g) throws FeatureTermException{
		return this.generalization.equivalents(g.generalization());
	}
	
	/**
	 * Test if this {@link Generalization} generalizes the given {@link Example}.
	 * This method tries to subsume the {@link FeatureTerm} of the {@link Example} with the {@link FeatureTerm} of this instance.
	 * @param e the {@link Example} to test.
	 * @return <tt>true</tt> if the {@link Example}s is generalized by this instance.
	 * @throws FeatureTermException
	 */
	public boolean generalizes(Example e) throws FeatureTermException{
		return this.generalization().subsumes(e.representation());
	}
	
	/**
	 * Test if this {@link Generalization} generalizes the given {@link Set} of {@link Example}.
	 * This method tries to subsume all the {@link FeatureTerm}s of the {@link Set} with the {@link FeatureTerm} of this instance.
	 * @param E the {@link Set} of {@link Example}s to test.
	 * @return <tt>true</tt> if all the {@link Example} are generalized by this instance.
	 * @throws FeatureTermException
	 */
	public boolean generalizes(Set<Example> E) throws FeatureTermException{
		for(Example e : E){
			if(!this.generalization().subsumes(e.representation()))
				return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see interfaces.SemioticElement#getExtension(interfaces.Container)
	 */
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Generalization clone(){
		try {
			return new Generalization(this.generalization());
		} catch (FeatureTermException e) {
			e.printStackTrace();
		}
		return null;
	}

}
