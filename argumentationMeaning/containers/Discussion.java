package containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.argumentation.core.AMAIL;
import csic.iiia.ftl.argumentation.core.Argument;
import csic.iiia.ftl.argumentation.core.ArgumentAcceptability;
import csic.iiia.ftl.argumentation.core.ArgumentationBasedLearning;
import csic.iiia.ftl.argumentation.core.LaplaceArgumentAcceptability;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Symbol;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.Rule;
import csic.iiia.ftl.learning.core.RuleHypothesis;
import enumerators.Agreement;
import enumerators.Hierarchy;
import interfaces.Agent;
import interfaces.Container;
import interfaces.SemioticElement;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import semiotic_elements.Sign;
import tools.ExampleSetManipulation;
import tools.LearningPackage;

/**
 * @author kemoadrian
 * The discussion is a {@link Container} that interfaces two {@link Agent}s during an argumentations.
 * The class creates all the variables necessary to lauch and run a successful instance of {@link AMAIL}.
 */

public class Discussion implements Container {

	// Basic variables to define the Discussion (statics)
	public Agreement agreementKind;
	public static AMAIL amail;

	// AMAIL variables
	public static Discussion winner;
	public static boolean disagreement;
	public static List<RuleHypothesis> l_h;
	public static List<FeatureTerm> examples;
	public static Set<Generalization> final_i_def;
	public static List<List<FeatureTerm>> l_examples;
	public static Map<Discussion,Hierarchy> problem;
	public static Map<Discussion,Integer> vote_for_sign;
	
	// New feature terms for temporary concepts
	public static FeatureTerm notSolutionToken;
	public static FeatureTerm solutionToken;
	
	// Global semantic content & variables
	public boolean solved;
	public Set<Example> context;
	public Concept the_solution;
	
	// Semantic content from the point of view of each agent
	boolean pass = false;
	public String toDelet;
	public Concept concept1;
	public Concept concept2;
	public Set<Generalization> myDefinition;
	public Set<Generalization> othersDefinition;
	
	
	/**
	 * Initialize all the static variables of {@link Discussion}.
	 */
	public void Initialize(){
		winner = null;
		disagreement = true;
		l_h = new LinkedList<>();
		examples = new LinkedList<>();
		final_i_def = new HashSet<>();
		problem = new HashMap<Discussion, Hierarchy>();
		l_examples = new ArrayList<List<FeatureTerm>>();
		vote_for_sign = new HashMap<Discussion,Integer>();
	}
	
	
	/**
	 * Creates a new instance of {@link Discussion}
	 * @param a The kind of {@link Agreement} that will be addressed by this {@link Discussion}
	 * @param c1 The name of {@link Concept} discussed that belongs to the Defenser
	 * @param c2 The name of {@link Concept} discussed that belongs to the Attacker
	 * @param h The {@link Hypothesis} that will be used to solve the {@link Discussion}
	 * @param initialize indicates if the static variables of {@link Discussion} should be initialized before the instantiation
	 */
	public Discussion(Agreement a, String c1, String c2, Hypothesis h, boolean initialize){
		// If requested, initialize
		if(initialize){
			Initialize();
			System.out.println("   > The discussion's variables have been initialized");
		}
		
		// Memorize the agreement
		this.agreementKind = a;
		
		// Initialize the feature terms of temporary concepts
		if(solutionToken == null && notSolutionToken == null){
			try {
				notSolutionToken = new TermFeatureTerm(new Symbol("not_the_solution"), LearningPackage.solution_sort());
				LearningPackage.dm().addFT(notSolutionToken);
				solutionToken = new TermFeatureTerm(new Symbol("the_solution"), LearningPackage.solution_sort());
				LearningPackage.dm().addFT(solutionToken);
			} catch (FeatureTermException e) {
				e.printStackTrace();
			}
		}
		
		// Memorize which concepts were initially confronted
		for(Concept c : h.own_concepts){
			if(c.sign().equals(getOwnSign(c1, c2))){
				this.concept1 = c;
				vote_for_sign.put(this, concept1.extensional_definition.size());
			}
		}
		for(Concept c : h.others_concepts){
			if(c.sign().equals(getOthersSign(c1,c2))){
				this.concept2 = c;
			}
		}
		
		// If Correct, evaluate which kind of correct
		if(a == Agreement.Correct){
			problem.put(this, problemKind());
			System.out.println("   > There is a \"Correct\" disagreement and the problem is seen as "+problem.get(this));
		}
		
		// Initialize solved to false (if there is a disagreement)
		switch (a) {
		
		case True:
			if(cleanMark(c1).equals(cleanMark(c2))){
				disagreement = false;
				solved = true;
			}
			break;
		case False:
			if(!cleanMark(c1).equals(cleanMark(c2))){
				disagreement = false;
				solved = true;
			}
			break;
		default:
			solved = false;
			break;
		}
		// Extract the context to the local variable
		this.context = h.getContext();
	}
	
	
	public void extensionalInitialization(){
		
		// Eventually change the name of one sign 
		System.out.println(concept1);
		System.out.println(concept2);
		Map<Hierarchy,Concept> h = new HashMap<>();
		
		// In case of Correct, we need to check which one is the hyponym and which one is the hyperonym
		if(agreementKind == Agreement.Correct){
			Hierarchy m_problem = null;
			Hierarchy o_problem = null;
			for(Entry<Discussion, Hierarchy> e : problem.entrySet()){
				if(e.getKey().equals(this))
					m_problem = e.getValue();
				else
					o_problem = e.getValue();
			}
			h.put(m_problem, concept1);
			h.put(o_problem, concept2);
			
			// In case of equivalence : problem
			if(m_problem == o_problem){
				// If they are both hyperonyms, the argumentation is put as True
				if(m_problem == Hierarchy.Hyperonymy){
					agreementKind = Agreement.True;
					System.out.println("   > The disagreeemnt is in fact a \"True\" type because everyone is seeing the problem as "+ m_problem);
					if(cleanMark(concept1.sign()).equals(cleanMark(concept2.sign()))){
						System.out.println("   > The signs are the same, there is no disagreement anymore");
						disagreement = false;
					}
				}
				// If they are both hyponyms, the argumentation is put as a Incorrect
				else{
					agreementKind = Agreement.Incorrect;
					System.out.println("   > The disagreeemnt is in fact an \"Incorrect\" type because everyone is seeing the problem as "+ m_problem);
				}
			}
			
			// If I am blind, I am what the other is not
			if(m_problem == Hierarchy.Blind){
				pass = true;
				m_problem = reverseHierarchy(o_problem);
				h.put(m_problem, concept1);
				System.out.println("   > This agent is blind to that disagreement and since the other is a "+o_problem+", he will act as the "+reverseHierarchy(o_problem));
				System.out.println(h);
			}
			
			// If the other is bline, he is what I am not
			if(o_problem == Hierarchy.Blind){
				o_problem = reverseHierarchy(m_problem);
				h.put(o_problem, concept2);
				System.out.println("   > Other agent is blind to that disagreement and since this agent is a "+m_problem+", he will act as the "+reverseHierarchy(m_problem));
				System.out.println(h);
			}
			else
				System.out.println("   > Agent considers its concept as the "+m_problem+" and the other as "+o_problem);
			
			// Changing name if they are the same to avoid confusion in deletion / addition during waitagreementphase
			if(cleanMark(concept1.sign()).equals(cleanMark(concept2.sign())) && m_problem != o_problem){
				if(m_problem == Hierarchy.Hyperonymy){
					String oldName = concept1.sign(); 
					concept1.sign = new Sign(concept1.sign.cake, cleanMark(concept1.sign.piece)+"Old*");
					System.out.println("   > The name of "+oldName+" has been changed for "+concept1.sign());
				}
				if(o_problem == Hierarchy.Hyperonymy){
					String oldName = concept2.sign(); 
					concept2.sign = new Sign(concept2.sign.cake, cleanMark(concept2.sign.piece)+"Old°");
					System.out.println("   > The name of "+oldName+" has been changed for "+concept2.sign());
				}
			}
		}
		
		if(agreementKind == Agreement.True || agreementKind == Agreement.False)
			return;
		
		ArrayList<String> sol = new ArrayList<>();
		
		// Creating the extensional basis of the argumentation
		List<FeatureTerm> a_examples = new LinkedList<>();
		Set<Example> theRightExtensionalDefinition = new HashSet<>();
		Set<Example> notTheRightExtensionalDefinition = new HashSet<>();
		switch (agreementKind) {
		case Incorrect:
			System.out.println("   > The agents will try to find a new intensional definition for examples that are both "+concept1.sign()+" and "+concept2.sign());
			for(Example e : context){
				try {
					if(concept1.covers(e) && concept2.covers(e))
						theRightExtensionalDefinition.add(e.clone());
					else
						notTheRightExtensionalDefinition.add(e.clone());
				} catch (FeatureTermException e1) {
					e1.printStackTrace();
				}
			}
			break;
		case Correct:
			System.out.println("   > The agents will try to find a new intensional definition for examples that are "+h.get(Hierarchy.Hyperonymy).sign()+" but not "+h.get(Hierarchy.Hyponymy));
			for(Example e : context){
				try {
					if((h.get(Hierarchy.Hyperonymy).covers(e) && !h.get(Hierarchy.Hyponymy).covers(e)))
						theRightExtensionalDefinition.add(e.clone());
					else
						notTheRightExtensionalDefinition.add(e.clone());
				} catch (FeatureTermException e1) {
					e1.printStackTrace();
				}
			}
			break;
		default:
			break;
		}
		// Mark the extensional definitions with a new Solution
		for (Example e : theRightExtensionalDefinition) {
			e.featureterm = LearningPackage.createFeature(e.featureterm, solutionToken);
			a_examples.add(e.featureterm);
			examples.add(e.featureterm);
			try {
				sol.add(e.featureterm.readPath(LearningPackage.solution_path()).toStringNOOS(LearningPackage.dm()));
			} catch (FeatureTermException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		for (Example e : notTheRightExtensionalDefinition) {
			e.featureterm = LearningPackage.createFeature(e.featureterm, notSolutionToken);
			a_examples.add(e.featureterm);
			examples.add(e.featureterm);
			try {
				sol.add(e.featureterm.readPath(LearningPackage.solution_path()).toStringNOOS(LearningPackage.dm()));
			} catch (FeatureTermException e1) {
				e1.printStackTrace();
			}
		}

		// Learn a new intensional definition from it
		ABUI learner = new ABUI();
		List<FeatureTerm> solution = new LinkedList<FeatureTerm>();
		solution.add(solutionToken);
		solution.add(notSolutionToken);
		RuleHypothesis rh = null;
		ArgumentAcceptability aa = new LaplaceArgumentAcceptability(examples, LearningPackage.solution_path(),
				LearningPackage.description_path(), (float) 0.75);
		try {
			rh = learner.learnConceptABUI(examples, solution, new ArrayList<Argument>(), aa,
					LearningPackage.description_path(), LearningPackage.solution_path(), LearningPackage.ontology(),
					LearningPackage.dm());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		l_examples.add(a_examples);
		l_h.add(rh);
		if(rh.getRules().isEmpty()){
			System.out.println("solutions : ");
			for(FeatureTerm s : solution)
				System.out.println("   "+s.toStringNOOS(LearningPackage.dm()));
			System.out.println("size of ext def : ");
			System.out.println("   right ext  = "+solutionToken.toStringNOOS(LearningPackage.dm())+" : "+theRightExtensionalDefinition.size());
			System.out.println("   wrong ext  = "+notSolutionToken.toStringNOOS(LearningPackage.dm())+" : "+notTheRightExtensionalDefinition.size());
			System.out.println("   intersection : "+ExampleSetManipulation.intersection(ExampleSetManipulation.quickSet(theRightExtensionalDefinition, notTheRightExtensionalDefinition)));
			System.out.println("   "+sol);
		}
	}
	
	public void argumentation(Hypothesis h){
		
		if(solved){
			System.out.println("  > No problem");
			the_solution = null;
			return;
		}
		
		// Find a new intensional definition if needed
		if (final_i_def.isEmpty()) {
			switch (agreementKind) {
			case Correct:
				if(pass)
					break;
			case Incorrect:
				// Solve disagreement
				List<ArgumentAcceptability> l_aa = new ArrayList<>();
				List<ArgumentationBasedLearning> l_l = new ArrayList<>();
				l_aa.add(new LaplaceArgumentAcceptability(l_examples.get(0), LearningPackage.solution_path(), LearningPackage.description_path(), (float)0.7));
				l_aa.add(new LaplaceArgumentAcceptability(l_examples.get(1), LearningPackage.solution_path(), LearningPackage.description_path(), (float)0.7));
				l_l.add(new ArgumentationBasedLearning());
				l_l.add(new ArgumentationBasedLearning());
				amail = new AMAIL(l_h, solutionToken, l_examples, l_aa, l_l, true, LearningPackage.description_path(),
						LearningPackage.solution_path(), LearningPackage.ontology(), LearningPackage.dm());
				while (amail.moreRoundsP()) {
					try {
						amail.round(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(amail.result().isEmpty()){
					System.out.println("IT FAILED BUT A BIT LATER");
					break;
				}
				if(amail.result().get(0).getRules().isEmpty())
					System.out.println("PROBLEM HERE");
				for(Rule r : amail.result().get(0).getRules()){
					if(r.solution.equals(solutionToken)){
						try {
							final_i_def.add(new Generalization(r.pattern));
						} catch (FeatureTermException e1) {
							e1.printStackTrace();
						}
					}
				}
				System.out.println(final_i_def.size());
				break;
			default:
				break;
			}
		}
		
		// Make output concepts (For Correct / Incorrect cases)
		Set<Example> E = new HashSet<>();
		Set<Generalization> I = new HashSet<>();
		Sign s = null;
		// Make output concepts (For True / False cases)
		int my_e_size = 0;
		int other_e_size = 0;
		for(Entry<Discussion, Integer> e : vote_for_sign.entrySet()){
			if(e.getKey().equals(this))
				my_e_size = e.getValue();
			else
				other_e_size = e.getValue();
		}
		switch (agreementKind){
		case Correct:
		case Incorrect:
			if (!final_i_def.isEmpty()) {
				I = final_i_def;
				for (Generalization i : I)
					E.addAll(i.getExtension(this));
				solved = true;
				s = new Sign(LearningPackage.solution_path().toString(), "temp_" + h.getNewSignIndex());
				the_solution = new Concept(s, I, E);
				solved = true;
			}
			else{
				System.out.println("   > not solved!");
			}
			break;
		case False:
			h.getNewSignIndex();
		case True:
			h.getNewSignIndex();
			if(winner == null || my_e_size > other_e_size)
				winner = this;
			solved = true;
			break;
		}
	}
	
	public String toString(){
		return concept1+"__"+concept2;
	}
	
	public boolean consistent() {
		return amail.moreRoundsP();
	}
	
	public Set<Example> getContext() {
		return context;
	}
	
	public Set<Concept> getAllConcepts() {
		Set<Concept> output = new HashSet<>();
		output.add(the_solution);
		return output;
	}
	
	public Set<Concept> getAssociatedConcepts(SemioticElement se) {
		Set<Concept> output = new HashSet<>();
		try {
			if(the_solution.covers(se.getExtension(this)))
				output.add(the_solution);
		} catch (FeatureTermException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public String cleanMark(String s){
		return s.replace("°","").replace("*","");
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
	
	public Hierarchy problemKind(){
		if(ExampleSetManipulation.excluContains(concept1.extensional_definition, concept2.extensional_definition))
			return Hierarchy.Hyperonymy;
		if(ExampleSetManipulation.excluContains(concept2.extensional_definition, concept1.extensional_definition))
			return Hierarchy.Hyponymy;
		return Hierarchy.Blind;
	}
	
	public Hierarchy reverseHierarchy(Hierarchy k){
		switch (k) {
		case Hyperonymy:
			return Hierarchy.Hyponymy;
		case Hyponymy:
			return Hierarchy.Hyperonymy;
		default:
			return null;
		}
	}
	
	public void putLabel(Set<Example> tRED, Set<Example> ntRED){
	}

}
