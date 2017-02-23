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
import tools.ExampleSetManipulation;
import tools.Pair;

public class QuantifiedHypothesis implements Container{

	public Set<Example> context;
	public Set<Concept> own_concepts;
	public Set<Concept> others_concepts;
	public Integer index_of_created_signs;
	public Map<Pair<Concept,Concept>,Agreement> agreementTable;
	
	public QuantifiedHypothesis(Set<Example> context){
		this.context = context;
		this.own_concepts = new HashSet<>();
		this.others_concepts = new HashSet<>();
		this.agreementTable = new HashMap<>();
		this.index_of_created_signs = 0;
	}
	
	public Integer getNewSignIndex(){
		index_of_created_signs ++;
		return index_of_created_signs -1;
	}
	
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
	
	public Set<String> getOwnSigns(){
		Set<String> o = new HashSet<>();
		for(Concept c : own_concepts){
			o.add(c.sign());
		}
		return o;
	}
	
	public Set<String> getOthersSigns(){
		Set<String> o = new HashSet<>();
		for(Concept c : others_concepts){
			o.add(c.sign());
		}
		return o;
	}
	
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

	
	public Set<Example> getContext() {
		return this.context;
	}

	public Set<Concept> getAllConcepts() {
		Set<Concept> output = new HashSet<>();
		output.addAll(own_concepts);
		output.addAll(others_concepts);
		return output;
	}
	
	public Map<Agreement,LinkedList<Pair<Concept,Concept>>> getPairByAgreement(){
		Map<Agreement,LinkedList<Pair<Concept,Concept>>> output = new HashMap<Agreement, LinkedList<Pair<Concept,Concept>>>();
		for(Entry<Pair<Concept,Concept>,Agreement> e : agreementTable.entrySet()){
			if(output.get(e.getValue()) == null)
				output.put(e.getValue(), new LinkedList<>());
			output.get(e.getValue()).add(e.getKey());
		}
		return output;
	}
	
	public String displayArgumentTable(){
		String o = "{ ";
		for(Entry<Pair<Concept,Concept>,Agreement> e : agreementTable.entrySet()){
			o += "[ "+e.getKey().getLeft().sign()+" & "+e.getKey().getRight().sign()+" = "+e.getValue()+" ] ";
		}
		o += "}";
		return o;
	}
	
	public String displayMyConcepts(){
		String o = "{ ";
		for(Concept c : own_concepts){
			o += "[ "+c.sign()+" ] ";
		}
		o += "}";
		return o;
	}
	
	public String displayOthersConcepts(){
		String o = "{ ";
		for(Concept c : others_concepts){
			o += "[ "+c.sign()+" ] ";
		}
		o += "}";
		return o;
	}

}
