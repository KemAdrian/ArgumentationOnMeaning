package interfaces;

import java.util.List;
import java.util.Set;

import containers.ContrastSet;
import csic.iiia.ftl.base.core.FeatureTerm;
import enumerators.Agreement;
import enumerators.Phase;
import messages.Message;

/**
 * An {@link Agent} is the component of the multi agent system that has a non shared vocabulary, based on a specific {@link ContrastSet}.
 * It tries to discuss with an other {@link Agent} in order to create a new {@link ContrastSet} that allows mutual intelligibility.
 * 
 * @author kemoadrian
 *
 */
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
	
	// Agreement functions
	
	/**
	 * Agreement and semiotic functions (gives the relation between two concepts with a modality of {@link Agreement})
	 * @param se1 a {@link SemioticElement}
	 * @param se2 a {@link SemioticElement}
	 * @return the {@link Agreement} modality of the agent over these semiotic elements.
	 */
	public Agreement agree(SemioticElement se1, SemioticElement se2);

	/**
	 * @param se1 a {@link SemioticElement}
	 * @param set2 a {@link Set} of {@link SemioticElement}
	 * @return the {@link Agreement} modality of the agent over these semiotic elements.
	 */
	public Agreement agree(SemioticElement se1, Set<SemioticElement> set2);
	
	/**
	 * @param set1 a {@link Set} of {@link SemioticElement}
	 * @param se2 a {@link SemioticElement}
	 * @return the {@link Agreement} modality of the agent over the semiotic element and the set of semiotic elements.
	 */
	public Agreement agree(Set<SemioticElement> set1, SemioticElement se2);
	
	/**
	 * @param set1 a {@link Set} of {@link SemioticElement}
	 * @param set2 a {@link Set} of {@link SemioticElement}
	 * @return the {@link Agreement} modality of the agent over the semiotic element and the set of semiotic elements.
	 */
	public Agreement agree(Set<SemioticElement> set1, Set<SemioticElement> set2);
	
	// Machine Learning tools
	
	/**
	 * Execute the turn of the {@link Agent}
	 * @return the {@link Phase} of the {@link Agent} for the next turn
	 */
	public Phase turn();
	
	/**
	 * Initialize the {@link Agent}. 
	 * @param data_set the {@link List<FeatureTerm>} used for the initial training.
	 * @return <tt>true</tt> if the {@link ContrastSet} has been correctly initialized.
	 */
	public boolean initialize(List<FeatureTerm> data_set);
	
	/**
	 * Use a data set to create the initial {@link ContrastSet} of the {@link Agent}.
	 * @param data_set the {@link List<FeatureTerm>} used for the learning.
	 * @return a new {@link ContrastSet} that partitions that data set.
	 */
	public ContrastSet learn(List<FeatureTerm> data_set);

}
