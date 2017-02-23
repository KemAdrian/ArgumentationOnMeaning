package csic.iiia.ftl.argumentation.conceptconvergence.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import csic.iiia.ftl.base.core.FeatureTerm;

public class TwoKeyMap {
	
	private static int MAX_VALUE = 1000;
	private int KEY1_VALUE;
	private int KEY2_VALUE;
	private HashMap<String, Integer> key1;
	private HashMap<String, Integer> key2;
	private HashMap<Integer, Set<FeatureTerm>> map;
	
	public TwoKeyMap(){
		this.KEY1_VALUE = 0;
		this.KEY2_VALUE = 0;
		this.key1 = new HashMap<String, Integer>();
		this.key2 = new HashMap<String, Integer>();
		this.map = new HashMap<Integer, Set<FeatureTerm>>();
	}
	
	public boolean put(String k1, String k2, FeatureTerm f){
		if(!key1.containsKey(k1)){
			key1.put(k1, KEY1_VALUE);
			if(KEY1_VALUE < MAX_VALUE*MAX_VALUE){
				System.out.println("creating new key 1 for "+k1+" : "+(KEY1_VALUE));
				KEY1_VALUE ++;
			}
			else return false;
		}
		if(!key2.containsKey(k2)){
			key2.put(k2, KEY2_VALUE);
			if(KEY2_VALUE < MAX_VALUE*MAX_VALUE){
				System.out.println("creating new key 2 for "+k2+" : "+(KEY2_VALUE));
				KEY2_VALUE ++;
			}
			else return false;
		}
		Set<FeatureTerm> E;
		if(!map.containsKey(key1.get(k1)+key2.get(k2)*1000)){
			E = new HashSet<FeatureTerm>();
		}
		else{
			E = map.get(key1.get(k1)+key2.get(k2)*1000);
		}
		E.add(f);
		System.out.println("puting the feature term classified as "+k1+" by agent 1 and as "+k2+" by agent 2 in "+(key1.get(k1)+key2.get(k2)*1000));
		map.put(key1.get(k1)+key2.get(k2)*1000, E);
		return true;
	}
	
	public Set<FeatureTerm> get(Object k1, Object k2){
		return map.get(key1.get(k1)+key2.get(k2)*1000);
	}
	
	public Set<Set<FeatureTerm>> getAll(){
		Set<Set<FeatureTerm>> output = new HashSet<Set<FeatureTerm>>();
		for(Set<FeatureTerm> s : map.values()){
			output.add(s);
		}
		return output;
	}
	
	public int size(){
		return this.map.size();
	}
	
	public List<String> give_Labels(Set<FeatureTerm> E){
		List<String> out = new ArrayList<String>();
		String output = "";
		for(Integer i : map.keySet()){
			if(map.get(i).equals(E)){
				for(String s : key1.keySet()){
					if(key1.get(s) == i%1000){
						output += ("Agent_1 labels it as "+s+" and ");
						out.add(s);
					}
				}
				for(String s : key2.keySet()){
					if(key2.get(s) == i/1000){
						output += ("Agent_2 labels it as "+s);
						out.add(s);
					}
				}
			}
		}
		out.add(output);
		return out;
	}
	
	public boolean give_combinations(){
		int cummul = 0;
		for(Integer i : map.keySet()){
			Integer a = i % 1000;
			Integer b = i / 1000;
			for(String o : key1.keySet()){
				if(key1.get(o).equals(a)){
					System.out.println("agent one calls it : "+o);
				}
			}
			for(String o : key2.keySet()){
				if(key2.get(o).equals(b)){
					System.out.println("agent two calls it : "+o);
				}
			}
			System.out.println("the problem "+i+" concerns "+map.get(i).size()+" examples");
			cummul += map.get(i).size();
		}
		System.out.println("total cumul of examples = "+cummul);
		return true;
	}

}
