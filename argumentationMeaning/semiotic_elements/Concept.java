package semiotic_elements;

import java.util.HashSet;
import java.util.Set;

import csic.iiia.ftl.base.utils.FeatureTermException;
import interfaces.Container;
import interfaces.SemioticElement;
import tools.ExampleSetManipulation;

public class Concept implements SemioticElement{
	
	public Sign sign;
	public Set<Generalization> intensional_definition;
	public Set<Example> extensional_definition;
	
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
	public String sign(){
		return sign.toString();
	}
	
	public Set<Generalization> intensional_definition(){
		return this.intensional_definition;
	} 
	
	public Set<Example> extensional_definition(){
		return this.extensional_definition;
	}
	
	public Set<Example> getExtension(Container c){
		HashSet<Set<Example>> to_test = new HashSet<Set<Example>>();
		to_test.add(this.extensional_definition());
		to_test.add(c.getContext());
		return ExampleSetManipulation.intersection(to_test);
	}
	
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
	
	public boolean covers(Example e) throws FeatureTermException{
		for(Generalization g : this.intensional_definition){
			if(g.generalizes(e))
				return true;
		}
		return false;
	}
	
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
	
	public void display(){
		System.out.println(this.sign());
		System.out.println(this.intensional_definition);
		System.out.println(this.extensional_definition.size());
	}
	
	public void display(Container k){
		System.out.println(this.sign());
		System.out.println(this.intensional_definition);
		System.out.println(this.extensional_definition.size());
		System.out.println(this.getExtension(k).size());
	}
	
	public String toString(){
		return this.sign();
	}
	
	public double diachronicExtensionalIndex(Concept c){
		double diff = ((double) ExampleSetManipulation.intersection(ExampleSetManipulation.quickSet(this.extensional_definition, c.extensional_definition)).size()) / c.extensional_definition.size();
		double same = ((double) ExampleSetManipulation.extrusion(ExampleSetManipulation.quickSet(this.extensional_definition, c.extensional_definition)).size()) /
				ExampleSetManipulation.union(ExampleSetManipulation.quickSet(this.extensional_definition, c.extensional_definition)).size();
		return 1 - Math.max(diff, same);
	}
	
	public double synchronicExtentionalIndex(Concept c){
		return 1 - ((double) ExampleSetManipulation
				.intersection(ExampleSetManipulation.quickSet(this.extensional_definition, c.extensional_definition))
				.size()) / ExampleSetManipulation.union(ExampleSetManipulation.quickSet(this.extensional_definition, c.extensional_definition)).size();
	}
	
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
	
	public double signIndex(Concept c){
		if(this.sign().equals(c.sign()))
			return 1.;
		return 0.;
	}
	
	public double synchronicCompatibilityMeasurment(Concept c){
		//System.out.println(this.sign()+" vs "+c.sign());
		//System.out.println("E : "+synchronicExtentionalIndex(c)+", I: "+synchronicIntensionalIndex(c)+", S: "+signIndex(c)+", C: "+Math.abs(signIndex(c) - synchronicExtentionalIndex(c)) * synchronicIntensionalIndex(c));
		return Math.abs(signIndex(c) - synchronicExtentionalIndex(c)) * synchronicIntensionalIndex(c);
	}
	
	public double diachronicCompatibilityMeasurment(Concept c){
		//System.out.println(this.sign()+" vs "+c.sign());
		//System.out.println("E : "+diachronicExtensionalIndex(c)+", I: "+diachronicExtensionalIndex(c)+", C: "+ (1 - diachronicExtensionalIndex(c)) * diachronicIntensionalIndex(c));
		return (1 - diachronicExtensionalIndex(c)) * diachronicIntensionalIndex(c);
	}
}
