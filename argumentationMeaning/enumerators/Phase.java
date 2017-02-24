package enumerators;

import interfaces.Agent;

/**
 *  The different phases of an argumentation, that defines the {@link Agent}'s behaviour when he uses the method {@link #Agent.turn()}.
 * 
 * @author kemoadrian
 *
 */
public enum Phase {
	Initial, BuildHypothesisState, ExpressAgreementState, ModifyAgreementState, Stop, ArgumentationStartState, ArgumentationInitializeExtension, ArgumentationCoreState, WaitingAgreementState, UpdateAgreementState, VoteForSignState, ChangeSignState, DecideForSignState;
}
