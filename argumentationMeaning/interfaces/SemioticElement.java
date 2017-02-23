package interfaces;

import java.util.Set;

import semiotic_elements.Example;

public interface SemioticElement {
	
	public SemioticElement clone();
	public Set<Example> getExtension(Container c);

}
