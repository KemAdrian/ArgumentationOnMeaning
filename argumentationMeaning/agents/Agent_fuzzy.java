package agents;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import containers.ContrastSet;
import containers.Discussion;
import containers.Hypothesis;
import csic.iiia.ftl.base.core.FeatureTerm;
import enumerators.Agreement;
import enumerators.Hierarchy;
import enumerators.Phase;
import interfaces.Agent;
import interfaces.SemioticElement;
import messages.Message;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Sign;
import tools.ExampleSetManipulation;
import tools.Token;

public class Agent_fuzzy implements Agent {
	
	// Communication
		public String nick;
		public Phase current_phase;
		public List<Message> mail;
		
		// Semiotic informations
		public ContrastSet Ki;
		public ContrastSet Kc;
		public Hypothesis H;
		public Discussion D;

	@Override
	public void getMessages(List<Message> mail) {
		this.mail.addAll(mail);
		
	}

	@Override
	public void sendMessages(List<Message> mail) {
		Token.attacker().getMessages(mail);
		
	}

	// Different agreement fonctions (see paper for more information)
		//
		
		public Agreement agree(SemioticElement se1, SemioticElement se2){
			HashSet<Set<Example>> to_test = new HashSet<Set<Example>>();
			to_test.add(se1.getExtension(Kc));
			to_test.add(se2.getExtension(Kc));
			if(ExampleSetManipulation.equivalent(to_test))
				return Agreement.True;
			if(ExampleSetManipulation.disjoint(to_test))
				return Agreement.False;
			if(ExampleSetManipulation.included(to_test))
				return Agreement.Correct;
			return Agreement.Incorrect;
		}

		public Agreement agree(SemioticElement se1, Set<SemioticElement> set2) {
			HashSet<Set<Example>> to_test = new HashSet<Set<Example>>();
			HashSet<Example> tt = new HashSet<>();
			for(SemioticElement se2 : set2)
				tt.addAll(se2.getExtension(Kc));
			to_test.add(se1.getExtension(Kc));
			to_test.add(tt);
			if(ExampleSetManipulation.equivalent(to_test))
				return Agreement.True;
			if(ExampleSetManipulation.disjoint(to_test))
				return Agreement.False;
			if(ExampleSetManipulation.included(to_test))
				return Agreement.Correct;
			return Agreement.Incorrect;
		}

		public Agreement agree(Set<SemioticElement> set1, SemioticElement se2) {
			return agree(se2, set1);
		}

		public Agreement agree(Set<SemioticElement> set1, Set<SemioticElement> set2) {
			HashSet<Set<Example>> to_test = new HashSet<Set<Example>>();
			HashSet<Example> tt1 = new HashSet<>();
			HashSet<Example> tt2 = new HashSet<>();
			for(SemioticElement se1 : set1)
				tt1.addAll(se1.getExtension(Kc));
			for(SemioticElement se2 : set2)
				tt2.addAll(se2.getExtension(Kc));
			to_test.add(tt1);
			to_test.add(tt2);
			if(ExampleSetManipulation.equivalent(to_test))
				return Agreement.True;
			if(ExampleSetManipulation.disjoint(to_test))
				return Agreement.False;
			if(ExampleSetManipulation.included(to_test))
				return Agreement.Correct;
			return Agreement.Incorrect;
		}
		
		public boolean disagreement(SemioticElement se1, SemioticElement se2){
			switch(agree(se1,se2)){
			case True:
			case Correct :
				return false;
			case False:
			case Incorrect:
				return true;
			}
			return false;
		}
		
		public Hierarchy problemKind(Concept mC, Concept oC){
			if(ExampleSetManipulation.contains(mC.extensional_definition, oC.extensional_definition))
				return Hierarchy.Hyperonymy;
			if(ExampleSetManipulation.contains(oC.extensional_definition, mC.extensional_definition))
				return Hierarchy.Hyponymy;
			return Hierarchy.Blind;
		}
		
		// Methods to mark concepts' signs
		//
		
		/**
		 * Mark a {@link String} as the {@link Sign} of one of this {@link Agent}'s {@link Concept}
		 * @param s The {@link String} to mark
		 * @return The {@link String} marked
		 */
		public String setStringMarktoSelf(String s){
			return s+"*";
		}
		
		/**
		 * Mark a {@link String} as the {@link Sign} of one of an other {@link Agent}'s {@link Concept}
		 * @param s The {@link String} to mark
		 * @return The {@link String} marked
		 */
		public String setStringMarktoOther(String s){
			return s+"°";
		}
		
		/**
		 * Switch the mark of a {@link String}
		 * @param s A marked {@link String}
		 * @return a {@link String} marked with the opposit mark than its initial one
		 */
		public String switchStringMark(String s){
			return s.replaceAll("°","=").replaceAll("\\*","°").replaceAll("=","\\*");
		}
		
		/**
		 * Select the {@link String} corresponding to the {@link Concept} of this {@link Agent} between two {@link String}s.
		 * @param s1 First {@link String} to test
		 * @param s2 Second {@link String} to test
		 * @return {@link String} from parameters marked as ours
		 */
		public String getOwnSign(String s1, String s2){
			if(s1.contains("*"))
				return s1;
			return s2;
		}
		
		/**
		 * Select the {@link String} corresponding to the {@link Concept} of the other {@link Agent} between two {@link String}s.
		 * @param s1 First {@link String} to test
		 * @param s2 Second {@link String} to test
		 * @return {@link String} from parameters marked as the other's
		 */
		public String getOthersSign(String s1, String s2){
			if(s1.contains("°"))
				return s1;
			return s2;
		}
		
		public String cleanMark(String s){
			return s.replace("°","").replace("*","");
		}

	@Override
	public Phase turn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean initialize(List<FeatureTerm> data_set) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ContrastSet learn(List<FeatureTerm> data_set) {
		// TODO Auto-generated method stub
		return null;
	}

}
