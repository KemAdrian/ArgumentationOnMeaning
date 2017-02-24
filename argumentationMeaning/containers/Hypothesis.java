package containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import enumerators.Agreement;
import interfaces.Agent;
import interfaces.Container;
import interfaces.SemioticElement;
import messages.Evaluate;
import messages.Forget;
import messages.Message;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Sign;
import sun.management.resources.agent;
import tools.ExampleSetManipulation;
import tools.Pair;

/**
 * The {@link Hypothesis} is a draft for future {@link ContrastSet}. It has {@link Concept} from two different {@link Agent}'s intensional definitions and {@link Sign}s and one {@link Agent}'s {@link Example}s.
 * For each couple of {@link Concept}s from different {@link Agent}s, it has their relation as an {@link Agreement}. 
 * 
 * @author kemoadrian
 *
 */
public class Hypothesis implements Container{

	public Set<Example> context;
	public Set<Concept> own_concepts;
	public Set<Concept> others_concepts;
	public Integer index_of_created_signs;
	public Map<Pair<Concept,Concept>,Agreement> agreementTable;
	
	/**
	 * Build a new {@link Hypothesis} from a given {@link Set} of {@link Example}.
	 * @param context the given {@link Set} of {@link Example}.
	 */
	public Hypothesis(Set<Example> context){
		this.context = context;
		this.own_concepts = new HashSet<>();
		this.others_concepts = new HashSet<>();
		this.agreementTable = new HashMap<>();
		this.index_of_created_signs = 0;
	}
	
	/**
	 * Get the index for a new {@link Sign}, for newly created {@link Concept}s.
	 * @return the index as an {@link Integer}.
	 */
	public Integer getNewSignIndex(){
		index_of_created_signs ++;
		return index_of_created_signs -1;
	}
	
	/**
	 * Put a {@link Concept} in the {@link Set} of the {@link agent}'s own {@link Concept}s.
	 * @param c the {@link Concept} to add.
	 * @param a the {@link Agent}.
	 * @return the {@link Message}s that need to be sent to the other {@link Agent} in order to make him update this {@link Agent}'s {@link Agreement}s in his {@link Hypothesis}.
	 */
	public List<Message> putOwnConcept(Concept c, Agent a){
		List<Message> o = new ArrayList<>();
		if(c == null)
			return o;
		for(Concept c2 : own_concepts){
			if(c2.equals(c))
				return o;
			if(a.agree(c2, c) == Agreement.True){
				System.out.println("   > "+c2.sign()+" is already equivalent to "+c.sign());
				o.add(new Forget(new Sign(c.sign())));
				return o;
			}
		}
		this.own_concepts.add(c);
		for(Concept c3 : others_concepts){
			Pair<Concept, Concept> p = new Pair<Concept, Concept>(c, c3);
			agreementTable.put(p, a.agree(c, c3));
			o.add(new Evaluate(c.sign()+"__"+c3.sign(), agreementTable.get(p)));
		}
		return o;
	}
	
	/**
	 * Get all the {@link Sign}s of this {@link Agent}'s {@link Concept}s in the {@link Hypothesis}.
	 * @return the {@link Set} of this {@link Agent}'s {@link Sign}s.
	 */
	public Set<String> getOwnSigns(){
		Set<String> o = new HashSet<>();
		for(Concept c : own_concepts){
			o.add(c.sign());
		}
		return o;
	}
	
	/**
	 * Get all the {@link Sign}s of the other {@link Agent}'s {@link Concept}s in the {@link Hypothesis}.
	 * @return the {@link Set} of the other {@link Agent}'s {@link Sign}s.
	 */
	public Set<String> getOthersSigns(){
		Set<String> o = new HashSet<>();
		for(Concept c : others_concepts){
			o.add(c.sign());
		}
		return o;
	}
	
	/**
	 * Put a {@link Concept} in the {@link Set} of the other {@link agent}'s {@link Concept}s.
	 * @param c the {@link Concept} to add.
	 * @param a the other {@link Agent}.
	 * @return the {@link Message}s that need to be sent to the other {@link Agent} in order to make him update this {@link Agent}'s {@link Agreement}s in his {@link Hypothesis}.
	 */
	public List<Message> putOthersConcept(Concept c, Agent a){
		List<Message> o = new ArrayList<>();
		if(c == null)
			return o;
		for(Concept c2 : others_concepts){
			if(c2.equals(c))
				return o;
		}
		this.others_concepts.add(c);
		for(Concept c3 : own_concepts){
			Pair<Concept, Concept> p = new Pair<Concept, Concept>(c3, c);
			agreementTable.put(p, a.agree(c3, c));
			o.add(new Evaluate(c.sign()+"__"+c3.sign(), agreementTable.get(p)));
		}
		return o;
	}
	
	/**
	 * Remove a {@link Concept} from the {@link Agent}'s own {@link Set}.
	 * @param c the {@link Concept} to remove.
	 */
	public void removeOwnConcept(Concept c){
		if(c == null)
			return;
		Concept to_remove = null;
		for(Concept c2 : own_concepts){
			if(c2.equals(c))
				to_remove = c2;
		}
		if(to_remove != null)
			own_concepts.remove(to_remove);
	}
	
	/**
	 * Remove a {@link Concept} from the other {@link Agent}'s {@link Set}.
	 * @param c the {@link Concept} to remove.
	 */
	public void removeOthersConcept(Concept c){
		if(c == null)
			return;
		Concept to_remove = null;
		for(Concept c2 : others_concepts){
			if(c2.equals(c))
				to_remove = c2;
		}
		if(to_remove != null)
			others_concepts.remove(to_remove);
	}
	
	/**
	 * Remove a {@link Pair} of {@link Concept}s from the table that keeps track of all the {@link Agreement}s.
	 * @param pc the {@link Pair} to remove.
	 * @return {@value 0} if the parameter is invalid, {@value 1} if there is no such entry in the table, {@value 2} if the {@link Concept} have been correctly removed.
	 */
	public Integer removeFromTable(Pair<Concept, Concept> pc){
		Pair<Concept, Concept> to_delet = null;
		if(pc == null)
			return 0;
		for(Pair<Concept, Concept> p : agreementTable.keySet()){
			if(p.equals(pc))
				to_delet = p;
		}
		if(to_delet == null)
			return 1;
		agreementTable.remove(to_delet);
		return 2;
	}
	
	/* (non-Javadoc)
	 * @see interfaces.Container#consistent()
	 */
	public boolean consistent() {
		Set<String> all_signs = new HashSet<String>();
		for(Concept c : this.getAllConcepts()){
			for(String s : all_signs){
				if(s.toString().equals(c.sign()))
					return false;
			}
			all_signs.add(c.sign());
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see interfaces.Container#getAssociatedConcepts(interfaces.SemioticElement)
	 */
	public Set<Concept> getAssociatedConcepts(SemioticElement se) {
		HashSet<Concept> o = new HashSet<Concept>();
		for (Concept c : this.getAllConcepts()) {
			HashSet<Set<Example>> to_test = new HashSet<Set<Example>>();
			to_test.add(c.getExtension(this));
			to_test.add(se.getExtension(this));
			if(!ExampleSetManipulation.disjoint(to_test))
				o.add(c);
		}
		return o;
	}

	
	/* (non-Javadoc)
	 * @see interfaces.Container#getContext()
	 */
	public Set<Example> getContext() {
		return this.context;
	}

	/* (non-Javadoc)
	 * @see interfaces.Container#getAllConcepts()
	 */
	public Set<Concept> getAllConcepts() {
		Set<Concept> output = new HashSet<>();
		output.addAll(own_concepts);
		output.addAll(others_concepts);
		return output;
	}
	
	/**
	 * Get all the {@link Pair}s of {@link Concept}s that are seen as having the different {@link Agreement} relation.
	 * @return a {@link Map} with, for each {@link Agreement} as a key, a {@link LinkedList} of {@link Pair}s of {@link Concept}s.
	 */
	public Map<Agreement,LinkedList<Pair<Concept,Concept>>> getPairByAgreement(){
		Map<Agreement,LinkedList<Pair<Concept,Concept>>> output = new HashMap<Agreement, LinkedList<Pair<Concept,Concept>>>();
		for(Entry<Pair<Concept,Concept>,Agreement> e : agreementTable.entrySet()){
			if(output.get(e.getValue()) == null)
				output.put(e.getValue(), new LinkedList<>());
			output.get(e.getValue()).add(e.getKey());
		}
		return output;
	}
	
	/**
	 * Gives a description of the current table of {@link Agreement}s.
	 * @return a {@link String} with the different entries of the table.
	 */
	public String displayArgumentTable(){
		String o = "{ ";
		for(Entry<Pair<Concept,Concept>,Agreement> e : agreementTable.entrySet()){
			o += "[ "+e.getKey().getLeft().sign()+" & "+e.getKey().getRight().sign()+" = "+e.getValue()+" ] ";
		}
		o += "}";
		return o;
	}
	
	/**
	 * Gives a description of the current {@link Concept}s from this {@link Agent} in the table of {@link Agreement}s.
	 * @return a {@link String} with the different {@link Concept}s of this {@link Agent}'s information.
	 */
	public String displayMyConcepts(){
		String o = "{ ";
		for(Concept c : own_concepts){
			o += "[ "+c.sign()+" ] ";
		}
		o += "}";
		return o;
	}
	
	/**
	 * Gives a description of the current {@link Concept}s from the other {@link Agent} in the table of {@link Agreement}s.
	 * @return a {@link String} with the different {@link Concept}s of the other {@link Agent}'s information.
	 */
	public String displayOthersConcepts(){
		String o = "{ ";
		for(Concept c : others_concepts){
			o += "[ "+c.sign()+" ] ";
		}
		o += "}";
		return o;
	}

}
