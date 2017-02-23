package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import containers.ContrastSet;
import containers.Discussion;
import containers.Hypothesis;
import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.argumentation.core.AMAIL;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import enumerators.Agreement;
import enumerators.Hierarchy;
import enumerators.Performative;
import enumerators.Phase;
import interfaces.Agent;
import interfaces.SemioticElement;
import messages.Assert;
import messages.Debate;
import messages.Elect;
import messages.Evaluate;
import messages.Forget;
import messages.Message;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import semiotic_elements.Sign;
import tools.ExampleSetManipulation;
import tools.Pair;
import tools.Token;

/***
 *  The {@link Agent_simple} is a first version of the agent in the new implementation of the protocol.
 *  The aim of {@link Agent_simple} is to argument over and resolve all kinds of disagreement arrising on one level of meaning.
 *  When we refer to "disagreement on one level of meaning", we mean on an inconsistant distribution of term symbols for one of the agents' features.
 *  The rest of the agents' features have to be consistent (i.e they should name the same things the same way).
 *  A consequence of this is the necessity to have a shared ontology on anything different than one set of symbols that will be discussed.
 * 
 * @author kemoadrian
 *
 */

public class Agent_simple implements Agent{
	
	// Communication
	public String nick;
	public Phase current_phase;
	public List<Message> mail;
	
	// Semiotic informations
	public ContrastSet Ki;
	public ContrastSet Kc;
	public Hypothesis H;
	public Discussion D;

	// See Parent class Javadoc
	public void sendMessages(List<Message> mail) {
		Token.attacker().getMessages(mail);
	}

	// See Parent class Javadoc
	public void getMessages(List<Message> mail) {
		this.mail.addAll(mail);
		
	}
	
	// See Parent class Javadoc
	public boolean initialize(List<FeatureTerm> data_set) {
		// Initializeing instnace variables
		this.Ki = learn(data_set);
		this.Kc = Ki;
		this.H = new Hypothesis(Kc.context);
		this.D = null;
		this.mail = new LinkedList<>();
		// Set the initial phase
		current_phase = Phase.Initial;
		return (Kc != null);
	}
	
	// See Parent class Javadoc
	public ContrastSet learn(List<FeatureTerm> data_set) {
		ABUI.ABUI_VERSION = 2;
		ABUI learner = new ABUI();
		try {
			return learner.makeContrastSet(data_set);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Create a Contrast Set from the Hypothesis
	public ContrastSet CreateMyCsetFromHypothesis(){
		ContrastSet cs = new ContrastSet(H.own_concepts, H.context);
		for(Concept c : cs.set){
			c.sign = new Sign(cleanMark(c.sign()));
		}
		return cs;
	}
	
	// Create a Contrast Set from the Hypothesis
		public ContrastSet CreateOtherCsetFromHypothesis(){
			ContrastSet cs = new ContrastSet(H.others_concepts, H.context);
			for(Concept c : cs.set){
				c.sign = new Sign(cleanMark(c.sign()));
			}
			return cs;
		}

	// See implemented Agent interface Javadoc
	public Phase turn(){
		switch (current_phase) {
		case Initial:
			this.current_phase = initialPhase();
			break;
		case BuildHypothesisState:
			this.current_phase = buildHypothesisPhase();
			break;
		case ExpressAgreementState:
			this.current_phase = expressAgreementState();
			break;
		case ModifyAgreementState:
			this.current_phase = modifyAgreementStatePhase();
			break;
		case ArgumentationStartState:
			this.current_phase = argumentationStartPhase();
			break;
		case ArgumentationInitializeExtension:
			this.current_phase = argumentationInitializeExtension();
			break;
		case ArgumentationCoreState:
			this.current_phase = argumentationCorePhase();
			break;
		case WaitingAgreementState:
			this.current_phase = waitAgreementPhase();
			break;
		case VoteForSignState:
			this.current_phase = voteForSignPhase();
			break;
		case ChangeSignState:
			this.current_phase = changeSignPhase();
			break;
		case Stop:
			this.current_phase = stop();
			break;
		default:
			return this.current_phase;
		}
		return this.current_phase;
	}
	
	
	/**
	 * First phase of an argumentation, sends the intensional definitions of all the {@link Concept}s to the other {@link Agent}.
	 * @return the {@link Phase.BuildHypothesisState}.
	 */
	public Phase initialPhase(){
		List<Message> to_send = new ArrayList<>();
		// Get all our concepts and create a new {@link Assert} to send 
		for(Concept c : Kc.getAllConcepts()){
			to_send.add(new Assert(c.sign, c.intensional_definition));
			System.out.println("   > The concept "+c.sign()+" has been sent to the attacker");
		}
		// Send all the messages created and move on
		sendMessages(to_send);
		return Phase.BuildHypothesisState;
	}
	
	/**
	 * Second phase of an argumenation, build a {@link Hypothesis} with the intensional definitions of this {@link Agent} (marked *) and the other {@link Agent}'s ones (marked °),
	 * thanks to the {@link Message}s received during the initial phase.
	 * @return the {@link Phase.ExpressAgreementState}.
	 */
	@SuppressWarnings("unchecked")
	public Phase buildHypothesisPhase(){
		// Adding concepts of the other
		for(Message m : mail){
			if(m.readPerformative().equals(Performative.Assert)){
				m = (Assert) m;
				Set<Generalization> intension = (Set<Generalization>) m.getElement();
				Set<Example> extension = new HashSet<>();
				for(Generalization g : intension){
					extension.addAll(g.getExtension(H));
				}
				H.others_concepts.add(new Concept(new Sign(setStringMarktoOther(m.getSign())), intension, extension));
				System.out.println("   > Concept for "+m.getSign()+" added to the other agent concepts in hypothesis");
			}
		}
		// Adding our concepts
		for(Concept c : Kc.getAllConcepts()){
			Concept h = c.clone();
			h.sign = new Sign(setStringMarktoSelf(c.sign()));
			H.own_concepts.add(h);
			System.out.println("   > Concept for "+c.sign()+" added to our own concepts in hypothesis");
		}
		// Creating agreement table
		for(Concept c : H.own_concepts){
			for(Concept c2 : H.others_concepts){
				H.agreementTable.put(new Pair<Concept, Concept>(c, c2), agree(c, c2));
			}
		}
		// Reset Mailbox and end
		System.out.println("   > Table of agreements has been created");
		this.mail = new LinkedList<>();
		return Phase.ExpressAgreementState;
	}
	
	/**
	 * Third phase of an argumentation, the {@link Agreement} values for each couple of {@link Concept}s between this {@link Agent}'s {@link Concept}s,
	 * and the other {code Agent}'s {@link Concept}s in the {@link Hypotheses} is sent to the other {@link Agent}. 
	 * @return the {@link Phase.ModifyAgreement}.
	 */
	public Phase expressAgreementState(){
		List<Message> to_send = new ArrayList<>();
		// For each couple of concepts in the argument table, send the agree() value found to the other agent
		for(Pair<Concept,Concept> p : H.agreementTable.keySet()){
			to_send.add(new Evaluate(p.getLeft().sign()+"__"+p.getRight().sign(), H.agreementTable.get(p)));
			System.out.println("   > The agreement : "+p.getLeft().sign()+" is a "+p.getRight().sign()+" is "+H.agreementTable.get(p)+" - has been sent");
		}
		sendMessages(to_send);
		return Phase.ModifyAgreementState;
	}
	
	/**
	 * Fourth phase of an argumentation, the {@link Agreement} values for each couple of the {@link Concept}s evaluated during a {@link expressAgreementState} is modified,
	 * according to the values received from the other {@link Agent}. 
	 * @return the {@link Phase.ArgumentationStartState}.
	 */
	public Phase modifyAgreementStatePhase(){
		boolean changeMade = false;
		for(Message m : mail){
			// For each agreement value on a couple sent by the other
			if(m.readPerformative().equals(Performative.Evaluate)){
				m = (Evaluate) m;
				Agreement other = (Agreement) m.getElement();
				// Addapt the marks on signs to our own frame of reference
				String s1 = switchStringMark(m.getSign().split("__")[0]);
				String s2 = switchStringMark(m.getSign().split("__")[1]);
				Pair<String,String> ss = new Pair<String, String>(s1,s2);
				Map<Pair<Concept,Concept>,Agreement> toChange = new HashMap<>();
				// Change our agreement value according to the "hidden disagreement" table of modifications (c.f paper)
				for(Pair<Concept,Concept> p : H.agreementTable.keySet()){
					if(ss.equals(p)){
						Agreement myself = H.agreementTable.get(p);
						// If agent sees it as Incorrect, do nothing
						if(myself != Agreement.Incorrect){
							// Case 1
							if(other == Agreement.Incorrect)
								toChange.put(p, Agreement.Incorrect);
							// Case 2
							if((other == Agreement.Correct && myself == Agreement.False) || (other == Agreement.False && myself == Agreement.Correct))
								toChange.put(p, Agreement.Incorrect);
							// Case 3
							if((other == Agreement.Correct && myself == Agreement.True) || (other == Agreement.True && myself == Agreement.Correct))
								toChange.put(p, Agreement.Correct);
							// Case 4
							if((other == Agreement.False && myself == Agreement.True) || (other == Agreement.True && myself == Agreement.False))
								toChange.put(p, Agreement.Incorrect);
						}
					}
				}
				// Do the actual change
				for(Pair<Concept,Concept> p : toChange.keySet()){
					if(!changeMade)
						changeMade = true;
					H.agreementTable.put(p, toChange.get(p));
					System.out.println("   > The agreement : "+p.getLeft().sign()+" is a "+p.getRight().sign()+" has been changed to "+H.agreementTable.get(p));
				}
			}
			// For each Concept to delet from the hypothesis
			if(m.readPerformative().equals(Performative.Forget)){
				Concept to_remove = new Concept(new Sign(switchStringMark(m.getSign())), new HashSet<>(), new HashSet<>());
				H.removeOthersConcept(to_remove);
				// Remove the arguments linked to the deleted Concept
				List<Pair<Concept,Concept>> to_delet = new LinkedList<>();
				for(Entry<Pair<Concept,Concept>, Agreement> e : H.agreementTable.entrySet()){
					if(e.getKey().getLeft().equals(to_remove) || e.getKey().getRight().equals(to_remove)){
						if(!changeMade)
							changeMade = true;
						to_delet.add(e.getKey());
						System.out.println("   > The Concept "+to_remove.sign()+" has been removed");
					}
				}
				for(Pair<Concept, Concept> p : to_delet){
					H.agreementTable.remove(p);
					System.out.println("   > The agreement between "+p.getLeft().sign()+" and "+p.getRight().sign()+" has been removed");
				}
			}
		}
		// Clean mailbox and move on
		if(!changeMade)
			System.out.println("   > No agreement has been changed");
		System.out.println(H.displayMyConcepts());
		System.out.println(H.displayOthersConcepts());
		this.mail = new LinkedList<>();
		//System.out.println(H.displayArgumentTable());
		return Phase.ArgumentationStartState;
	}
	
	/**
	 * Fifth phase of an argumentation, the {@link Discussion} is created in order to reduce the number of disagreements.
	 * The priority order of disagreement solving is respected.
	 * Where no disagreement is found the agent stops the discussion and go decide a final vocabulary for the {@link ContrastSet}.
	 * @return {@link Phase.VoteForSignState} if there is no more disagreement, {@link Phase.ArgumentationInitializeExtension} otherwise.
	 */
	public Phase argumentationStartPhase(){
		// If pair of Concepts suggested by other agent, start discussion
		for(Message m : this.mail){
			if(m.readPerformative() == Performative.Debate){
				m = (Debate) m;
				String request = switchStringMark(m.getSign());
				String s1 = request.split("__")[0];
				String s2 = request.split("__")[1];
				D = new Discussion((Agreement) m.getElement(), getOwnSign(s1, s2), getOthersSign(s1, s2), H, false);
				System.out.println("   > A new discussion about "+getOwnSign(s1, s2)+" and "+getOthersSign(s1, s2)+" has been created");
				System.out.println("   > The relation between the two concepts is seen as "+(Agreement) m.getElement());
			}
		}
		// If not, suggest own discussion
		if(D == null){
			if(H.getPairByAgreement().get(Agreement.Incorrect) != null){
				Pair<Concept,Concept> p = H.getPairByAgreement().get(Agreement.Incorrect).getLast();
				D = new Discussion(Agreement.Incorrect, p.getLeft().sign(), p.getRight().sign(), H, true);
				System.out.println("   > A new discussion about "+getOwnSign(p.getLeft().sign(), p.getRight().sign())+" and "+getOthersSign(p.getLeft().sign(), p.getRight().sign())+" has been created");
				System.out.println("   > The relation between the two concepts is seen as "+D.agreementKind);
			}
			else if(H.getPairByAgreement().get(Agreement.Correct) != null){
				Pair<Concept,Concept> p = H.getPairByAgreement().get(Agreement.Correct).getLast();
				D = new Discussion(Agreement.Correct, p.getLeft().sign(), p.getRight().sign(), H, true);
				System.out.println("   > A new discussion about "+getOwnSign(p.getLeft().sign(), p.getRight().sign())+" and "+getOthersSign(p.getLeft().sign(), p.getRight().sign())+" has been created");
				System.out.println("   > The relation between the two concepts is seen as "+D.agreementKind);
			}
			else if(H.getPairByAgreement().get(Agreement.False) != null){
				Pair<Concept,Concept> p = H.getPairByAgreement().get(Agreement.False).getLast();
				D = new Discussion(Agreement.False, p.getLeft().sign(), p.getRight().sign(), H, true);
				System.out.println("   > A new discussion about "+getOwnSign(p.getLeft().sign(), p.getRight().sign())+" and "+getOthersSign(p.getLeft().sign(), p.getRight().sign())+" has been created");
				System.out.println("   > The relation between the two concepts is seen as "+D.agreementKind);
			}
			else if(H.getPairByAgreement().get(Agreement.True) != null){
				Pair<Concept,Concept> p = H.getPairByAgreement().get(Agreement.True).getLast();
				D = new Discussion(Agreement.True, p.getLeft().sign(), p.getRight().sign(), H, true);
				System.out.println("   > A new discussion about "+getOwnSign(p.getLeft().sign(), p.getRight().sign())+" and "+getOthersSign(p.getLeft().sign(), p.getRight().sign())+" has been created");
				System.out.println("   > The relation between the two concepts is seen as "+D.agreementKind);
			}
			else{
				System.out.println(H.displayMyConcepts());
				System.out.println(H.displayOthersConcepts());
				return Phase.VoteForSignState;
			}
			// Send the discussion
			List<Message> toSend = new LinkedList<>();
			toSend.add(new Debate(D.toString(), D.agreementKind));
			sendMessages(toSend);
		}
		// Go to the core of Argumentation
		this.mail = new LinkedList<>();
		return Phase.ArgumentationInitializeExtension;
	}
	
	/**
	 * Sixth phase of an argumentation, the {@link Agent} creates the Extensional definitions for a new {@link Concept} that would lead to an agreement on the meaning.
	 * According to the relation of {@link Hierarchy} detected between the two {@link Concept} during the conversation's initialization, the {@link Agreement} of the {@link Discussion} can change.
	 * An Intensional definition is created by inductive learning using {@link ABUI} in order to produce a base for the {@link AMAIL} argumentation during the next phase.
	 * @return the {@link Phase.ArgumentationCoreState}.
	 */
	public Phase argumentationInitializeExtension(){
		D.extensionalInitialization();
		return Phase.ArgumentationCoreState;
	}
	

	/**
	 * Seventh phase of an argumentation, the {@link Agent} uses {@link AMAIL} to generate a new Intensional definition for a new {@link Concept} that would satisfy him and the Attacker.
	 * @return {@link Phase.WaitingAgreementState} if the {@link Discussion} is solved, {@link Phase.WaitingAgreementState} otherwise.
	 */
	public Phase argumentationCorePhase(){
		// Do the agent's part of the argumentation
		D.argumentation(H);
		// If no agreement has been found, continue argumentation
		if(!D.solved)
			return Phase.ArgumentationCoreState;
		return Phase.WaitingAgreementState;
	}
	
	
	/**
	 * Eighth phase of an argumentation, the {@link Agent} adds/delets {@link Concept}s from its current {@link ContrastSet}.
	 * The {@link Concept}s are added/deleted according to the {@link Concept} obtained after the Argumentation Core Phase, the {@link Agreement} of the {@link Discussion} and the rest of the {@link ContrastSet}.
	 * For each {@link Concept} added, an {@link Evaluate} is sent to the Attacker. For each {@link Concept} deleted, a {@link Forget} is sent to the Attacker.
	 * @return the {@link Phase.ModifyAgreementState}.
	 */
	public Phase waitAgreementPhase(){
		
		// Prepare a list of messages to send in order to evaluate the agreements between old concepts and evaluation
		List<Message> to_send = new ArrayList<>();
		
		// If one of the concept is not known by an agent, it is learned by this agent
		boolean learn_o_concept = true;
		boolean learn_m_concept = true;
		if(D.agreementKind == Agreement.False){
			for(Entry<Pair<Concept, Concept>, Agreement> e : H.agreementTable.entrySet()){
				if(e.getKey().getLeft().equals(D.concept1) && e.getValue() != Agreement.False)
					learn_m_concept = false;
				if(e.getKey().getRight().equals(D.concept2) && e.getValue() != Agreement.False)
					learn_o_concept = false;
			}
		}
		
		// If there is no disagreement, just start an other discussion
		if(!Discussion.disagreement){
			// Remove the solved disagreement
			if(!learn_m_concept && !learn_o_concept){
				System.out.println(" > No changes to make");
				System.out.println(D.concept1);
				System.out.println(D.concept2);
				System.out.println(H.displayMyConcepts());
				System.out.println(H.displayOthersConcepts());
			}
			if(learn_m_concept){
				System.out.println("   > The other agent does not know the concept "+D.concept1.sign()+" so it learns it ");
				Concept add_other = D.concept1.clone();
				add_other.sign = new Sign(switchStringMark(add_other.sign()));
				to_send.addAll(H.putOthersConcept(add_other, this));
			}
			if(learn_o_concept){
				System.out.println("   > This gent does not know the concept "+D.concept2.sign()+" so it learns it ");
				Concept add_own = D.concept2.clone();
				add_own.sign = new Sign(switchStringMark(add_own.sign()));
				to_send.addAll(H.putOwnConcept(add_own, this));
			}
			H.removeFromTable(new Pair<Concept, Concept>(D.concept1, D.concept2));
			// Move on
			D = null;
			return Phase.ModifyAgreementState;
		}
		
		
		// Upload new concepts in the current hypothesis
		switch(D.agreementKind){
		
		case Correct:
			Concept add_old;
			Concept add_new_m;
			Concept add_new_o;
			Concept delet_old;
			// Duplicate the solution to solve the resulting Correct issue later
			add_new_m = D.the_solution.clone();
			add_new_o = D.the_solution.clone();
			// Mark their signs the right way
			add_new_m.sign = new Sign(setStringMarktoSelf(add_new_m.sign()));
			add_new_o.sign = new Sign(setStringMarktoOther(add_new_o.sign()));
			
			// If this agent has the Hyponym
			if(ExampleSetManipulation.contains(D.concept1.extensional_definition, D.concept2.extensional_definition)){
				System.out.println("   > "+D.concept1.sign()+" contains "+D.concept2.sign());
				add_old = D.concept2.clone();
				delet_old = D.concept1.clone();
				// Change signs
				add_old.sign = new Sign(switchStringMark(add_old.sign()));
				// Update Hypothesis
				to_send.addAll(H.putOwnConcept(add_old, this));
				H.removeOwnConcept(delet_old);
			}
			
			// If this agent has the Hypernym
			else{
				System.out.println("   > "+D.concept2.sign()+" contains "+D.concept1.sign());
				add_old = D.concept1.clone();
				delet_old = D.concept2.clone();
				// Change signs
				add_old.sign = new Sign(switchStringMark(add_old.sign()));
				// Update Hypothesis
				to_send.addAll(H.putOthersConcept(add_old, this));
				H.removeOthersConcept(delet_old);
			}
			
			// Upload the new concepts in the Hypothesis
			to_send.addAll(H.putOwnConcept(add_new_m, this));
			to_send.addAll(H.putOthersConcept(add_new_o, this));
			
			// Remove the arguments linked to the deleted Concept
			List<Pair<Concept,Concept>> to_delet = new LinkedList<>();
			for(Entry<Pair<Concept,Concept>, Agreement> e : H.agreementTable.entrySet()){
				if(e.getKey().getLeft().equals(delet_old) || e.getKey().getRight().equals(delet_old)){
					to_delet.add(e.getKey());
				}
			}
			for(Pair<Concept, Concept> p : to_delet){
				H.agreementTable.remove(p);
			}
			// Display
			System.out.println("   > Concept "+add_new_m.sign()+", Concept "+add_new_o+" and Concept "+add_old.sign()+" has been added");
			System.out.println("   > Concept "+delet_old.sign()+" has been deleted");
			break;
			
		case Incorrect:
			// Duplicate the solution to solve the resulting Correct issue later
			Concept c1 = D.the_solution.clone();
			Concept c2 = D.the_solution.clone();
			// Mark their signs the right way
			c1.sign = new Sign(setStringMarktoSelf(c1.sign()));
			c2.sign = new Sign(setStringMarktoOther(c2.sign()));
			// Upload them in the Hypothesis
			to_send.addAll(H.putOwnConcept(c1, this));
			to_send.addAll(H.putOthersConcept(c2, this));
			// Display
			System.out.println("   > Concept "+c1.sign()+" and Concept "+c2.sign()+" has been added");
			break;
			
		case True:
			if ((Discussion.winner == D && H.getOthersSigns().contains(switchStringMark(D.concept1.sign())))
					|| (Discussion.winner != D && H.getOwnSigns().contains(switchStringMark(D.concept2.sign())))){
				String s = D.concept1.sign.cake+":temp_"+H.index_of_created_signs;
				System.out.println("   > "+D.concept1.sign()+" changed into "+setStringMarktoSelf(s));
				System.out.println("   > "+D.concept2.sign()+" changed into "+setStringMarktoOther(s));
				D.concept1.sign = new Sign(setStringMarktoSelf(s));
				D.concept2.sign = new Sign(setStringMarktoOther(s));
			}
			else if(Discussion.winner == D){
				System.out.println("   > "+D.concept2.sign()+" changed into "+switchStringMark(D.concept1.sign()));
				D.concept2.sign = new Sign(switchStringMark(D.concept1.sign()));
			}
			else{
				System.out.println("   > "+D.concept1.sign()+" changed into "+switchStringMark(D.concept2.sign()));
				D.concept1.sign = new Sign(switchStringMark(D.concept2.sign()));
			}
			break;
			
		case False:
			String cake = D.concept1.sign.cake;
			if(Discussion.winner == D){
				// Add the modificated other's concept to our own concepts
				Concept add_to_own = new Concept(new Sign(cake,"temp_"+H.index_of_created_signs), D.concept2.copy_intensional_definition(), new HashSet<>());
				for(Generalization g : add_to_own.intensional_definition){
					add_to_own.extensional_definition.addAll(g.getExtension(H));
				}
				add_to_own.sign = new Sign(setStringMarktoSelf(add_to_own.sign()));
				H.own_concepts.add(add_to_own);
				// Add our own concept to the other's concepts
				Concept add_to_other = new Concept(new Sign(switchStringMark(D.concept1.sign())), D.concept1.copy_intensional_definition(), new HashSet<>());
				for(Generalization g : add_to_other.intensional_definition){
					add_to_other.extensional_definition.addAll(g.getExtension(H));
				}
				H.others_concepts.add(add_to_other);
				// Change the sign of the other concept in the other's concepts
				System.out.println("   > Concept "+add_to_own+" has been added to our concepts");
				System.out.println("   > Concept "+add_to_other+" has been added to the other's concepts");
				D.concept2.sign = new Sign(switchStringMark(add_to_own.sign()));
			}
			else{
				// Add the modificated other's concept to our own concepts
				Concept add_to_own = new Concept(new Sign(switchStringMark(D.concept2.sign())), D.concept2.copy_intensional_definition(), new HashSet<>());
				for(Generalization g : add_to_own.intensional_definition){
					add_to_own.extensional_definition.addAll(g.getExtension(H));
				}
				H.own_concepts.add(add_to_own);
				// Add our own concept to the other's concepts
				Concept add_to_other = new Concept(new Sign(cake,"temp_"+H.index_of_created_signs), D.concept1.copy_intensional_definition(), new HashSet<>());
				for(Generalization g : add_to_other.intensional_definition){
					add_to_other.extensional_definition.addAll(g.getExtension(H));
				}
				add_to_other.sign = new Sign(setStringMarktoOther(add_to_other.sign()));
				H.others_concepts.add(add_to_other);
				// Change the sign of the other concept in the other's concepts
				System.out.println("   > Concept "+add_to_own+" has been added to our concepts");
				System.out.println("   > Concept "+add_to_other+" has been added to the other's concepts");
				D.concept1.sign = new Sign(switchStringMark(add_to_other.sign()));
			}
			break;
			
		default:
			break;
		}
		
		// Remove the solved disagreement
		H.removeFromTable(new Pair<Concept, Concept>(D.concept1, D.concept2));
		System.out.println(H.displayArgumentTable());
		System.out.println(H.displayMyConcepts());
		System.out.println(H.displayOthersConcepts());
		// Move on
		D = null;
		sendMessages(to_send);
		return Phase.ModifyAgreementState;
	}
	

	/**
	 * Ninth phase of an argumentation, the {@link Agent} votes for the {@link Sign}s of each final {@link Concept} present in the {@link ContrastSet}.
	 * @return the {@link Phase.ChangeSignState}.
	 */
	public Phase voteForSignPhase(){
		List<Message> to_send = new ArrayList<>();
		// Find which signs can be used for temp concepts
		Set<Concept> can_rename = new HashSet<>();
		for(Concept oldC : Kc.set){
			boolean add = true;
			for(Concept newC : H.own_concepts){
				if(oldC.sign().equals(cleanMark(newC.sign()))){
					add = false;
					break;
				}
			}
			if(add)
				can_rename.add(oldC);
		}
		// For each temp concepts, send a message to the other agent AND to yourself
		for(Concept newC : H.own_concepts){
			if(newC.sign().contains("temp_")){
				for(Concept oldC : can_rename){
					Set<Example> E = new HashSet<>();
					for(Example e : newC.extensional_definition){
						try {
							if(newC.covers(e)){
								E.add(e);
							}
						} catch (FeatureTermException e1) {
							e1.printStackTrace();
						}
					}
					System.out.println("  > "+newC.sign()+" should be named "+oldC.sign()+" ("+E.size()+")");
					to_send.add(new Elect(newC.sign,new Pair<>(oldC.sign, E.size())));
					mail.add(new Elect(newC.sign,new Pair<>(oldC.sign, E.size())));
				}
			}
		}
		sendMessages(to_send);
		return Phase.ChangeSignState;
	}
	
	
	/**
	 * Tenth phase of an argumentation, the {@link Agent} might change the {@link Sign}s of its {@link Concept}s according to the results of the vote that occured in the {@link Phase.VoteForSignState}.
	 * @return the {@link Phase.Stop}.
	 */
	public Phase changeSignPhase(){
		// Create a table for the results of the votes
		HashMap<String, Pair<String, Integer>> winners = new HashMap<String,Pair<String,Integer>>();
		// For each vote received by mail
		for(Message m : mail){
			if(m.readPerformative() == Performative.Elect){
				Elect e = (Elect) m;
				// if an entry for this vote already exists, try to update it
				if(winners.get(cleanMark(e.getSign())) != null){
					// If the sign in the mail has more vote than the previous one, the winner is updated
					if(winners.get(cleanMark(e.getSign())).getRight() < e.i){
						winners.put(cleanMark(e.getSign()), new Pair<String, Integer>(e.s2.toString(), e.i));
						System.out.println("   > The winner for the vote on "+cleanMark(e.getSign())+" has been updated with answer "+e.s2.toString()+" ("+e.i+")");
					}
					// Otherwise change nothing
					else{
						System.out.println("   > The winner for the vote on "+cleanMark(e.getSign())+" has not been updated  ("+e.i+")");
					}
				}
				// if an entry for this vote does not exit, create it
				else{
					System.out.println("   > A new vote started for "+e.getSign()+" with answer "+e.s2.toString()+" ("+e.i+")");
					winners.put(cleanMark(e.getSign()), new Pair<String, Integer>(e.s2.toString(), e.i));
				}
			}
		}
		// For all the concepts in the hypothesis, update the signs according to the winners
		for(String s : winners.keySet()){
			for(Concept c : H.own_concepts){
				if(cleanMark(c.sign()).equals(cleanMark(s))){
					System.out.println("   >"+c.sign()+" has been changed for "+setStringMarktoSelf(cleanMark(winners.get(s).getLeft())));
					c.sign = new Sign(setStringMarktoSelf(cleanMark(winners.get(s).getLeft())));
				}
			}
			for(Concept c : H.others_concepts){
				if(cleanMark(c.sign()).equals(cleanMark(s))){
					System.out.println("   >"+c.sign()+" has been changed for "+setStringMarktoOther(cleanMark(winners.get(s).getLeft())));
					c.sign = new Sign(setStringMarktoOther(cleanMark(winners.get(s).getLeft())));
				}
			}
		}
		// Put the baby to sleep
		System.out.println("   > Stop");
		System.out.println(H.displayArgumentTable());
		System.out.println(H.displayMyConcepts());
		System.out.println(H.displayOthersConcepts());
		mail = new LinkedList<>();
		return Phase.Stop;
	}

	
	/**
	 * The final {@link Phase} of the argumentation. The script that instantiates the two {@link Agent}s can now terminate.
	 * @return
	 */
	public Phase stop(){
		return Phase.Stop;
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
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return nick;
	}

}
