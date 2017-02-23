package interfaces;

import java.util.Set;

import semiotic_elements.Concept;
import semiotic_elements.Example;

public interface Container {
	
	public boolean consistent();
	public Set<Example> getContext();
	public Set<Concept> getAllConcepts();
	public Set<Concept> getAssociatedConcepts(SemioticElement se);

}
