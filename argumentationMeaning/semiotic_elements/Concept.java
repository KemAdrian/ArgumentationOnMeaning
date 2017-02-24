package semiotic_elements;

import java.util.HashSet;
import java.util.Set;

import containers.ContrastSet;
import csic.iiia.ftl.base.utils.FeatureTermException;
import interfaces.Agent;
import interfaces.Container;
import interfaces.SemioticElement;
import tools.ExampleSetManipulation;

/**
 * A {@link Concept} is a triadic relation between the three other {@link SemioticElement}s. It represents a unity of meaning in our model.
 * @author kemoadrian
 *
 */
public class Concept implements SemioticElement{
	
	public Sign sign;
	public Set<Generalization> intensional_definition;
	public Set<Example> extensional_definition;
	
	/**
	 * Create a new {@link Concept} from three {@link SemioticElement}.
	 * @param s the first {@link SemioticElement}, the {@link Sign}.
	 * @param I the second {@link SemioticElement}, the intentional definition as a {@link Set} of {@link Generalization}.
	 * @param E the third {@link SemioticElement}, the extensional definition as a {@link Set} of {@link Example}.
	 */
	public Concept(Sign s, Set<Generalization> I, Set<Example> E){
		this.sign = s.clone();
		this.intensional_definition = new HashSet<Generalization>();
		this.extensional_definition = new HashSet<Example>();
	
		for(Generalization g : I)
			this.intensional_definition.add(g.clone());
		for(Example e : E)
			this.extensional_definition.add(e.clone());
		}
	
	// Get Informations
	
	/**
	 * Give the {@link Sign} of the {@link Concept}.
	 * @return the {@link Sign}.
	 */
	public String sign(){
		return sign.toString();
	}
	
	/**
	 * Give the intensional definition.
	 * @return the {@link Set} of {@link Generalization} that composes the intensional definition.
	 */
	public Set<Generalization> intensional_definition(){
		return this.intensional_definition;
	} 
	
	/**
	 * Give the extensional definition
	 * @return the {@link Set} of {@link Example} that composes the extensional definition
	 */
	public Set<Example> extensional_definition(){
		return this.extensional_definition;
	}
	
	/* (non-Javadoc)
	 * @see interfaces.SemioticElement#getExtension(interfaces.Container)
	 */
	public Set<Example> getExtension(Container c){
		HashSet<Set<Example>> to_test = new HashSet<Set<Example>>();
		to_test.add(this.extensional_definition());
		to_test.add(c.getContext());
		return ExampleSetManipulation.intersection(to_test);
	}
	
	/**
	 * Give the {@link Generalization} from this {@link Concept}'s intensional definition that can generalize at least one {@link Example} from the given {@link SemioticElement} extension in the context of a {@link Container}.
	 * @param e the {@link SemioticElement}.
	 * @param c the {@link Container}.
	 * @return
	 */
	public Set<Generalization> Generalizes(SemioticElement e, Container c){
		Set<Generalization> o = new HashSet<Generalization>();
		for(Generalization g : this.intensional_definition){
			if(!ExampleSetManipulation.disjoint(ExampleSetManipulation.quickSet(g.getExtension(c), e.getExtension(c))))
				o.add(g);
		}
		return o;
	}
	
	// Duplicate
	public Set<Generalization> copy_intensional_definition(){
		HashSet<Generalization> o = new HashSet<Generalization>();
		for(Generalization g : this.intensional_definition)
			o.add(g.clone());
		return o;
	}
	
	public Set<Example> copy_extensional_definition(){
		HashSet<Example> o = new HashSet<Example>();
		for(Example e : this.extensional_definition)
			o.add(e.clone());
		return o;
	}
	
	// Add elements
	public boolean addExamples(Set<Example> E) throws FeatureTermException{
		HashSet<Example> to_add = new HashSet<Example>();
		for(Example e1 : this.extensional_definition){
			for(Example e2 : E){
				if(!e1.equivalent(e2))
					to_add.add(e1);
			}
		}
		return this.extensional_definition.addAll(to_add);
		
	}
	
	public boolean addGeneralizations(Set<Generalization> I) throws FeatureTermException{
		HashSet<Generalization> to_add = new HashSet<Generalization>();
		for(Generalization g1 : this.intensional_definition){
			for(Generalization g2 : I){
				if(!g1.generalization().equivalents(g2.generalization()))
					to_add.add(g1);
			}
		}
		return this.intensional_definition.addAll(to_add);
	}
	
	// Delete elements
	public boolean removeExamples(Set<Example> E) throws FeatureTermException{
		HashSet<Example> to_remove = new HashSet<Example>();
		for(Example e1 : this.extensional_definition){
			for(Example e2 : E){
				if(e1.equivalent(e2))
					to_remove.add(e1);
			}
		}
		return this.extensional_definition.removeAll(to_remove);
	}
	
	public boolean removeGeneralizations(Set<Generalization> I) throws FeatureTermException{
		HashSet<Generalization> to_remove = new HashSet<Generalization>();
		for(Generalization g1 : this.intensional_definition){
			for(Generalization g2 : I){
				if(g1.generalization().equivalents(g2.generalization()))
					to_remove.add(g1);
			}
		}
		return this.intensional_definition.removeAll(to_remove);
	}
	
	// Test
	/**
	 * Test if the intensional definition of this {@link Concept} entirely covers its extensional definition.
	 * @return <tt>true</tt> if the intensional definition entirely covers its extensional definition.
	 * @throws FeatureTermException
	 */
	public boolean consistent() throws FeatureTermException{
		HashSet<Example> test = new HashSet<Example>();
		for(Generalization g : this.intensional_definition){
			for(Example e : this.extensional_definition){
				if(g.generalizes(e))
					test.add(e);
			}
		}
		return (test.size() == this.extensional_definition.size());
	}
	
	/**
	 * Test if the intensional definition of this {@link Concept} covers a given {@link Example}.
	 * @param e the {@link Example}.
	 * @return <tt>true</tt> if the {@link Example} is covered by the intensional definition.
	 * @throws FeatureTermException
	 */
	public boolean covers(Example e) throws FeatureTermException{
		for(Generalization g : this.intensional_definition){
			if(g.generalizes(e))
				return true;
		}
		return false;
	}
	
	/**
	 * Test if the intensional definition of this {@link Concept} covers a given {@link Set} of {@link Example}.
	 * @param E the {@link Set} of {@link Example}s.
	 * @return <tt>true</tt> if all the {@link Example}s from the {@link Set} are covered by the intensional definition.
	 * @throws FeatureTermException
	 */
	public boolean covers(Set<Example> E) throws FeatureTermException{
		for(Generalization g : this.intensional_definition){
			if(g.generalizes(E))
				return true;
		}
		return false;
	}
	
	public Concept clone(){
		return new Concept(sign.clone(), copy_intensional_definition(), copy_extensional_definition());
	}
	
	public boolean equals(Object s){
		return this.sign().equals(s.toString());
	}
	
	/**
	 * Display in the consle the {@link Sign} of the {@link Concept}, the {@link Set} of its {@link Generalization} and the number of {@link Example}s from its extensional definition.
	 */
	public void display(){
		System.out.println(this.sign());
		System.out.println(this.intensional_definition);
		System.out.println(this.extensional_definition.size());
	}
	
	/**
	 * Display in the consle the {@link Sign} of the {@link Concept}, the {@link Set} of its {@link Generalization} and the number of {@link Example}s from its extensional definition and the size of its extension in the context of a {@link Container}.
	 * @param k the {@link Container}.
	 */
	public void display(Container k){
		System.out.println(this.sign());
		System.out.println(this.intensional_definition);
		System.out.println(this.extensional_definition.size());
		System.out.println(this.getExtension(k).size());
	}
	
	public String toString(){
		return this.sign();
	}
	
	/**
	 * Gives a compatibility measurement from a diachronic perspective of this {@link Concept}'s extensional definition with another {@link Concept}'s one.
	 * @param c the other {@link Concept}.
	 * @return the measure of compatibility as a {@link Double}.
	 */
	public double diachronicExtensionalIndex(Concept c){
		double diff = ((double) ExampleSetManipulation.intersection(ExampleSetManipulation.quickSet(this.extensional_definition, c.extensional_definition)).size()) / c.extensional_definition.size();
		double same = ((double) ExampleSetManipulation.extrusion(ExampleSetManipulation.quickSet(this.extensional_definition, c.extensional_definition)).size()) /
				ExampleSetManipulation.union(ExampleSetManipulation.quickSet(this.extensional_definition, c.extensional_definition)).size();
		return 1 - Math.max(diff, same);
	}
	
	/**
	 * Gives a compatibility measurement from a synchronic perspective of this {@link Concept}'s extensional definition with another {@link Concept}'s one.
	 * @param c the other {@link Concept}.
	 * @return the measure of compatibility as a {@link Double}.
	 */
	public double synchronicExtentionalIndex(Concept c){
		return 1 - ((double) ExampleSetManipulation
				.intersection(ExampleSetManipulation.quickSet(this.extensional_definition, c.extensional_definition))
				.size()) / ExampleSetManipulation.union(ExampleSetManipulation.quickSet(this.extensional_definition, c.extensional_definition)).size();
	}
	
	/**
	 * Gives a compatibility measurement from a diachronic perspective of this {@link Concept}'s intensional definition with another {@link Concept}'s one.
	 * @param c the other {@link Concept}.
	 * @return the measure of compatibility as a {@link Double}.
	 */
	public double diachronicIntensionalIndex(Concept c){
		Set<Example> coveredByMe = new HashSet<>();
		Set<Example> coveredByOther = new HashSet<>();
		for(Example e : extensional_definition){
			try {
				if(this.covers(e))
					coveredByMe.add(e);
				if(c.covers(e) && !this.covers(e))
					coveredByOther.add(e);
			} catch (FeatureTermException e1) {
				e1.printStackTrace();
			}
		}
		double precision = ((double) coveredByMe.size()) / (coveredByMe.size() + coveredByOther.size());
		double recall = ((double) coveredByMe.size()) / extensional_definition.size();
		return 2*(precision*recall)/(precision+recall);
	}
	
	/**
	 * Gives a compatibility measurement from a synchronic perspective of this {@link Concept}'s intensional definition with another {@link Concept}'s one.
	 * @param c the other {@link Concept}.
	 * @return the measure of compatibility as a {@link Double}.
	 */
	public double synchronicIntensionalIndex(Concept c){
		Set<Example> coveredByMe = new HashSet<>();
		Set<Example> coveredByOther = new HashSet<>();
		for(Example e : extensional_definition){
			try {
				if(this.covers(e))
					coveredByMe.add(e);
				if(c.covers(e) && !this.covers(e))
					coveredByOther.add(e);
			} catch (FeatureTermException e1) {
				e1.printStackTrace();
			}
		}
		double precision = ((double) coveredByMe.size()) / (coveredByMe.size() + coveredByOther.size());
		double recall = ((double) coveredByMe.size()) / extensional_definition.size();
		return 2*(precision*recall)/(precision+recall);
	}
	
	/**
	 * Give a numerical information of the difference between this {@link Concept}'s {@link Sign} and another {@link Concept}'s one.
	 * @param c the other {@link Concept}.
	 * @return <tt>true</tt> if the {@link Sign}s are the same.
	 */
	public double signIndex(Concept c){
		if(this.sign().equals(c.sign()))
			return 1.;
		return 0.;
	}
	
	/**
	 * Gives the overall compatibility measure between this {@link Concept} and a {@link Concept} from the {@link ContrastSet} of a different {@link Agent}.
	 * @param c the other {@link Concept}.
	 * @return the measure as a {@link Double}.
	 */
	public double synchronicCompatibilityMeasurment(Concept c){
		//System.out.println(this.sign()+" vs "+c.sign());
		//System.out.println("E : "+synchronicExtentionalIndex(c)+", I: "+synchronicIntensionalIndex(c)+", S: "+signIndex(c)+", C: "+Math.abs(signIndex(c) - synchronicExtentionalIndex(c)) * synchronicIntensionalIndex(c));
		return Math.abs(signIndex(c) - synchronicExtentionalIndex(c)) * synchronicIntensionalIndex(c);
	}
	
	/**
	 * Gives the overall compatibility measure between this {@link Concept} and a {@link Concept} from a different version of the same {@link ContrastSet}.
	 * @param c the other {@link Concept}.
	 * @return the measure as a {@link Double}.
	 */
	public double diachronicCompatibilityMeasurment(Concept c){
		//System.out.println(this.sign()+" vs "+c.sign());
		//System.out.println("E : "+diachronicExtensionalIndex(c)+", I: "+diachronicExtensionalIndex(c)+", C: "+ (1 - diachronicExtensionalIndex(c)) * diachronicIntensionalIndex(c));
		return (1 - diachronicExtensionalIndex(c)) * diachronicIntensionalIndex(c);
	}
}
