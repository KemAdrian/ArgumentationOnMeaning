package interfaces;

import java.util.Set;

import semiotic_elements.Concept;
import semiotic_elements.Example;

/**
 * {@link Container}s are sets of {@link Concept}s that partition a {@link Set} of {@link Example} known as a {@link Concept}.
 * They differ by the relations that their {@link Concept}s have.
 * 
 * @author kemoadrian
 *
 */
public interface Container {
	
	/**
	 * Check if this instance verifies the criteria of a {@link ContrastSet} (complete partition of its {@link Set} of {@link Example} constituing its context) 
	 * @return <tt>true</tt> if the {@link Container} is a {@link ContrastSet}.
	 */
	public boolean consistent();
	
	/**
	 * Extracting the {@link Set} of {@link Example} constituing the context of the {@link Container}
	 * @return the {@link Set} of {@link Example}
	 */
	public Set<Example> getContext();
	
	/**
	 * Extracting the {@link Concept} of the {@link Container}
	 * @returnthe {@link Concept} of the {@link Container}
	 */
	public Set<Concept> getAllConcepts();
	
	/**
	 * Getting the {@link Set} of {@link Concept} related to the specified {@link SemioticElement}
	 * @param the specified {@link SemioticElement}
	 * @return the related {@link Set} of {@link Concept}
	 */
	public Set<Concept> getAssociatedConcepts(SemioticElement se);

}
