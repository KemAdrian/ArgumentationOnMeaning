package tools;

import java.util.HashSet;
import java.util.Set;

import semiotic_elements.Example;

public class ExampleSetManipulation {
	
	public static double error = 0.;
	
	
	public static Set<Example> union(Set<Set<Example>> extensional_definitions){
		Set<Example> o = new HashSet<Example>();
		for(Set<Example> set1 : extensional_definitions){
			for(Example e1 : set1){
				boolean add_it = true;
				for(Example e2 : o){
					if(e1.equivalent(e2)){
						add_it = false;
						break;
					}
				}
				if(add_it)
					o.add(e1);
			}
		}
		return o;
	}
	
	public static Set<Example> intersection(Set<Set<Example>> extensional_definitions){
		Set<Example> o = new HashSet<Example>();
		if(extensional_definitions.size() < 1)
			return o;
		if(extensional_definitions.size() == 1){
			for(Set<Example> set : extensional_definitions)
				return set;
		}
		for(Set<Example> set1 : extensional_definitions){
			for(Set<Example> set2 : extensional_definitions){
				if(set2 != set1){
					for(Example e1 : set1){
						for(Example e2 : set2){
							if(e1.equals(e2) && ! contains(o, e1)){
								o.add(e1);
							}
						}
					}
				}
					
			}
		}
		return o;
	}
	
	public static Set<Example> extrusion(Set<Set<Example>> extensional_definitions){
		Set<Example> o = new HashSet<Example>(union(extensional_definitions));
		o.removeAll(intersection(extensional_definitions));
		return o;
	}
	
	public static boolean disjoint(Set<Set<Example>> extensional_definitions){
		for(Set<Example> set1 : extensional_definitions){
			for(Set<Example> set2 : extensional_definitions){
				Set<Set<Example>> to_test = new HashSet<Set<Example>>();
				to_test.add(set1);
				to_test.add(set2);
				if(set1 != set2 && intersection(to_test).size() > (error * (set1.size() + set2.size()) ) ){
					return false;
				}
			}
		}
		return true;
	}
	
	
	public static boolean equivalent(Set<Set<Example>> extensional_definitions){
		for(Set<Example> set1 : extensional_definitions){
			for(Set<Example> set2 : extensional_definitions){
				if(set1 != set2 && !equals(set1, set2))
					return false;
			}
		}
		return true;
	}
	
	public static boolean included(Set<Set<Example>> extensional_definitions){
		for(Set<Example> set1 : extensional_definitions){
			for(Set<Example> set2 : extensional_definitions){	
				Set<Set<Example>> to_test = new HashSet<Set<Example>>();
				to_test.add(set1);
				to_test.add(set2);
				if(!equals(set1,set2) && !equals(set1, union(to_test)) && !equals(set2, union(to_test)))
					return false;
			}
		}
		return true;
	}
	
	private static boolean equals(Set<Example> s1, Set<Example> s2){
		if(s1.size() - s2.size() > (error * (s1.size() + s2.size())) || s2.size() - s1.size() > (error * (s1.size() + s2.size()))){
			return false;
		}
		int count = 0;
		for(Example e1 : s1){
			boolean has_found_the_same = false;
			for(Example e2 : s2){
				if(e1.equals(e2)){
					has_found_the_same = true;
				}
			}
			if(!has_found_the_same)
				count ++;
		}
		return count <= (error * (s1.size() + s2.size()));
	}
	
	private static boolean contains(Set<Example> set, Example e){
		for(Example ex : set)
			if(ex.equals(e))
				return true;
		return false;
	}
	
	public static boolean contains(Set<Example> set1, Set<Example> set2){
		Set<Set<Example>> u = quickSet(set1, set2);
		if(equals(set2,intersection(u)))
			return true;
		return false;
	}
	
	public static boolean excluContains(Set<Example> set1, Set<Example> set2){
		Set<Set<Example>> u = quickSet(set1, set2);
		if(equals(set2,intersection(u)) && !equals(set1, set2))
			return true;
		return false;
	}
	
	public static Set<Set<Example>> quickSet(Set<Example> s1, Set<Example> s2){
		Set<Set<Example>> o = new HashSet<Set<Example>>();
		o.add(s1);
		o.add(s2);
		return o;
	}
	
	public static Set<Example> duplicate(Set<Example> to_copy){
		Set<Example> output = new HashSet<>();
		for(Example e : to_copy)
			output.add(e.clone());
		return output;
	}

}
