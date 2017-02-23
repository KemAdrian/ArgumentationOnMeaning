package interfaces;

import java.util.List;
import java.util.Set;

import containers.ContrastSet;
import csic.iiia.ftl.base.core.FeatureTerm;
import enumerators.Agreement;
import enumerators.Phase;
import messages.Message;

public interface Agent {
	
	// Messages
	/**
	 * Add a list of messages to the mailbox of the {@link Agent}. 
	 * @param mail
	 * The {@link List<Message>} to add to the agent's mailbox.
	 */
	public void getMessages(List<Message> mail);
	/**
	 * Add a list of messages to the mailbox of the interlocutor (using its {@link getMessages(List<Message> mail} method).
	 * @param mail
	 * The {@link List<Message>} to add to the other agent's mailbox.
	 */
	public void sendMessages(List<Message> mail);
	
	// Agreement and semiotic functions
	public Agreement agree(SemioticElement se1, SemioticElement se2);
	public Agreement agree(SemioticElement se1, Set<SemioticElement> set2);
	public Agreement agree(Set<SemioticElement> set1, SemioticElement se2);
	public Agreement agree(Set<SemioticElement> set1, Set<SemioticElement> set2);
	
	// Machine Learning tools
	public Phase turn();
	public boolean initialize(List<FeatureTerm> data_set);
	public ContrastSet learn(List<FeatureTerm> data_set);

}
