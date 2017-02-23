package containers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import interfaces.Container;
import interfaces.SemioticElement;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Sign;
import tools.ExampleSetManipulation;
import tools.Pair;

public class ContrastSet implements Container{
	
	public static int custom_name = 0;;
	public Set<Concept> set;
	public Set<Example> context;
	private Scanner keyboard;
	
	/**
	 * A {@link ContrastSet} is a {@link Container} where the set of {@link Concept}s makes a partition of the context.
	 * @param concepts The set of {@link Concept}s partitioning the context
	 * @param context The set of {@link Example}s representing the context
	 */
	public ContrastSet(Set<Concept> concepts, Set<Example> context){
		this.set = new HashSet<>();
		for(Concept c : concepts)
			this.set.add(c.clone());
		this.context = context;
	}

	public boolean consistent(){
		Set<Set<Example>> all_extensional_definitions = new HashSet<Set<Example>>();
		Set<String> all_signs = new HashSet<String>();
		for(Concept c : this.set){
			all_extensional_definitions.add(c.extensional_definition());
			for(String s : all_signs){
				if(s.toString().equals(c.sign()))
					return false;
			}
			all_signs.add(c.sign());
		}
		if(!ExampleSetManipulation.intersection(all_extensional_definitions).isEmpty())
			return false;
		Set<Example> cumul = new HashSet<>();
		Set<Set<Example>> to_test = new HashSet<>();
		for(Set<Example> set : all_extensional_definitions)
			cumul.addAll(set);
		to_test.add(cumul);
		to_test.add(context);
		return ExampleSetManipulation.equivalent(to_test);
	}
	
	public Set<Concept> getAssociatedConcepts(SemioticElement se) {
		HashSet<Concept> o = new HashSet<Concept>();
		for (Concept c : set) {
			HashSet<Set<Example>> to_test = new HashSet<Set<Example>>();
			to_test.add(c.getExtension(this));
			to_test.add(se.getExtension(this));
			if(!ExampleSetManipulation.disjoint(to_test))
				o.add(c);
		}
		return o;
	}

	public Set<Example> getContext(){
		return this.context;
	}

	public Set<Concept> getAllConcepts(){
		return this.set;
	}
	
	public Set<Concept> copyConcepts(){
		Set<Concept> o = new HashSet<Concept>();
		for(Concept c : this.set)
			o.add(c.clone());
		return o;
	}
	
	public Pair<String,String> mergeConcepts(){
		keyboard = new Scanner(System.in);
		Random rnd = new Random();
		
		ArrayList<Concept> concepts = new ArrayList<>();
		for(Concept c : set)
			concepts.add(c);
		System.out.println("The contrast set has "+concepts.size()+" concepts:");
		for(Concept c : concepts)
			System.out.println("   > "+concepts.indexOf(c)+" : "+c.sign());
		
		int i = -1;
		int j = -1;
		System.out.println("Do you want to merge one ? (n = no, r = random, i = chose index of the merge)");
		String myString = keyboard.nextLine();
		switch (myString) {
		case "r":
			i = (int)(rnd.nextInt(concepts.size()));
			break;
		case "i":
			System.out.println("   > Give an index:");
			i = keyboard.nextInt();
			while(i<0 || i >= concepts.size()){
				System.out.println("   > The index is out of boundaries, please retry:");
				i = keyboard.nextInt();
			}
			break;
		case "n":
		default:
			break;
		}
		if(i != -1){
			System.out.println("The first concept is "+concepts.get(i).sign());
			System.out.println("With which other concept do you want to merge it ? (r = random, i = chose index of the merge)");
			String mysecondString = keyboard.nextLine();
			switch (mysecondString) {
			case "r":
				j = (int)(rnd.nextInt(concepts.size()));
				while( i== j)
					j = (int)(rnd.nextInt(concepts.size()));
				break;
			case "i":
				System.out.println("   > Give an index:");
				j = keyboard.nextInt();
				while(j<0 || j >= concepts.size() || i==j){
					System.out.println("   > The index is out of boundaries or equal to the first one, please retry:");
					j = keyboard.nextInt();
				}
				break;
			default:
				System.out.println("Invalid character, no concept will be merged");
				i=-1;
				j=-1;
				break;
			}
		}
		if(i != -1){
			System.out.println("The second concept is "+concepts.get(j).sign());
			Concept c1 = concepts.get(i);
			Concept c2 = concepts.get(j);
			c2.intensional_definition.addAll(c1.intensional_definition);
			c2.extensional_definition.addAll(c1.extensional_definition);
			set.remove(c1);
			System.out.println("Merged");
			return new Pair<String, String>(c1.sign(), c2.sign());
		}
		return new Pair<String, String>("none", "none");
	}
	
	public Pair<String,String> mergeConcepts(String s1, String s2, int i1, int i2){
		Random rnd = new Random();
		
		ArrayList<Concept> concepts = new ArrayList<>();
		for(Concept c : set)
			concepts.add(c);
		
		int i = -1;
		int j = -1;
		switch (s1) {
		case "r":
			i = (int)(rnd.nextInt(concepts.size()));
			break;
		case "i":
			if(i1 <0 || i1 > concepts.size())
				i = (int)(rnd.nextInt(concepts.size()));
			else
				i = i1;
			break;
		case "n":
		default:
			break;
		}
		if(i != -1){
			System.out.println("The first concept is "+concepts.get(i).sign());
			switch (s2) {
			case "r":
				j = (int)(rnd.nextInt(concepts.size()));
				while( i == j)
					j = (int)(rnd.nextInt(concepts.size()));
				break;
			case "i":
				if(i2<0 || i2 > concepts.size() || i2==i){
					j = (int)(rnd.nextInt(concepts.size()));
					while( i == j)
						j = (int)(rnd.nextInt(concepts.size()));
				}
				else
					j = i2;
				break;
			default:
				System.out.println("Invalid character, no concept will be merged");
				i=-1;
				j=-1;
				break;
			}
		}
		if(i != -1){
			System.out.println("The second concept is "+concepts.get(j).sign());
			Concept c1 = concepts.get(i);
			Concept c2 = concepts.get(j);
			c2.intensional_definition.addAll(c1.intensional_definition);
			c2.extensional_definition.addAll(c1.extensional_definition);
			set.remove(c1);
			System.out.println("Merged");
			return new Pair<String, String>(c1.sign(), c2.sign());
		}
		return new Pair<String, String>("none", "none");
	}
	
	public String removeConcept(String s, int i1){
		Random rnd = new Random();

		ArrayList<Concept> concepts = new ArrayList<>();
		for (Concept c : set)
			concepts.add(c);

		int i = -1;
		switch (s) {
		case "r":
			i = (int) (rnd.nextInt(concepts.size()));
			break;
		case "i":
			if (i1 < 0 || i1 > concepts.size())
				i = (int) (rnd.nextInt(concepts.size()));
			else
				i = i1;
			break;
		case "n":
		default:
			break;
		}
		if(i>0){
			Concept ci = concepts.get(i);
			set.remove(ci);
			return ci.sign();
		}
		return "";
	}
	
	public Pair<String,String> renameConcept(String s, int i1){
		Random rnd = new Random();

		ArrayList<Concept> concepts = new ArrayList<>();
		for (Concept c : set)
			concepts.add(c);

		int i = -1;
		switch (s) {
		case "r":
			i = (int) (rnd.nextInt(concepts.size()));
			break;
		case "i":
			if (i1 < 0 || i1 > concepts.size())
				i = (int) (rnd.nextInt(concepts.size()));
			else
				i = i1;
			break;
		case "n":
		default:
			break;
		}
		System.out.println(i);
		if(i > 0){
			Concept ci = concepts.get(i).clone();
			concepts.get(i).sign = new Sign(ci.sign.cake, "custom_name_" + ContrastSet.custom_name);
			ContrastSet.custom_name++;
			return new Pair<String, String>(ci.sign(), concepts.get(i).sign());
		}
		else{
			return new Pair<String, String>("","");
		}
	}
	
	public Pair<Double,Double> synchronicCompatibility(ContrastSet cs){
		double n = set.size()*cs.set.size();
		double sum = 0;
		double var = 0;
		for(Concept c1 : set)
			for(Concept c2 : cs.set)
				sum += c1.synchronicCompatibilityMeasurment(c2);
		double mean = sum/n;
		for(Concept c1 : set)
			for(Concept c2 : cs.set)
				var += Math.pow((c1.synchronicCompatibilityMeasurment(c2) - mean), 2);
		var /= n-1;
		return new Pair<Double, Double>(mean, var);
	}
	
	public Pair<Double,Double> diachronicCompatibility(ContrastSet cs){
		double n = set.size()*cs.set.size();
		double sum = 0;
		double var = 0;
		for(Concept c1 : set)
			for(Concept c2 : cs.set)
				sum += c1.diachronicCompatibilityMeasurment(c2);
		double mean = sum/n;
		for(Concept c1 : set)
			for(Concept c2 : cs.set)
				var += Math.pow((c1.diachronicCompatibilityMeasurment(c2) - mean), 2);
		var /= n-1;
		return new Pair<Double, Double>(mean, var);
	}
	
	public Pair<Double,Double> synchronicCompatibilityError(ContrastSet cs){
		double n = 0;
		double sum = 0;
		double var = 0;
		for(Concept c1 : set){
			for(Concept c2 : cs.set){
				sum += (1 - c1.synchronicCompatibilityMeasurment(c2));
				n ++;
			}
		}
		double mean = sum/n;
		for(Concept c1 : set){
			for(Concept c2 : cs.set){
				var += Math.pow(((1 - c1.synchronicCompatibilityMeasurment(c2)) - mean), 2);
			}
		}
		return new Pair<Double, Double>(sum, var);
	}
	
	public Pair<Double,Double> diachronicCompatibilityError(ContrastSet cs){
		double n = 0;
		double sum = 0;
		double var = 0;
		for(Concept c1 : set){
			for(Concept c2 : cs.set){
				sum += (1 - c1.diachronicCompatibilityMeasurment(c2));
				n ++;
			}
		}
		double mean = sum/n;
		for(Concept c1 : set){
			for(Concept c2 : cs.set){
				var += Math.pow(((1 - c1.diachronicCompatibilityMeasurment(c2)) - mean), 2);
			}
		}
		return new Pair<Double, Double>(sum, var);
	}
	
	public Pair<Double,Double> diachronicCoverageError(ContrastSet cs){
		double n = 0;
		double sum = 0;
		double var = 0;
		for(Concept c1 : set){
			Set<Example> covered = new HashSet<>();
			for(Concept c2 : cs.set){
				covered.addAll(ExampleSetManipulation.intersection(
						ExampleSetManipulation.quickSet(c1.extensional_definition, c2.extensional_definition)));
			}
			sum += ((double) c1.extensional_definition.size() - covered.size())/c1.extensional_definition.size();
			n ++;
		}
		double mean = sum/n;
		for(Concept c1 : set){
			Set<Example> covered = new HashSet<>();
			for(Concept c2 : cs.set){
				covered.addAll(ExampleSetManipulation.intersection(
						ExampleSetManipulation.quickSet(c1.extensional_definition, c2.extensional_definition)));
			}
			var += Math.pow( (((double) c1.extensional_definition.size() - covered.size())/c1.extensional_definition.size()) - mean, 2);
		}
		return new Pair<Double, Double>(sum, var);
	}
	
	public List<String> saveInFile(){
		LinkedList<String> o = new LinkedList<>();
		for(Concept c : set){
			for(Example e : c.extensional_definition){
				o.add(e.example.toString()+'	'+c.sign());
			}
		}
		return o;
	}

}