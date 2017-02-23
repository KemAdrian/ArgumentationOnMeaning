package csic.iiia.ftl.argumentation.conceptconvergence.Interfaces;

import java.util.Collection;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Agreement;
import csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2.Sign;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;

public interface Agent {
	
	public boolean get(Message mail);
	public boolean read_message(Message mail) throws FeatureTermException;
	public boolean send_message(Message mail);
	public boolean give_token();
	public LearningPackage getLearningPackage();
	public Agreement agree(FeatureTerm e, FeatureTerm e1) throws FeatureTermException;
	public Agreement agree(FeatureTerm featureValue, Collection<FeatureTerm> extensionalDefinition) throws FeatureTermException;
	public Agreement agree(Collection<FeatureTerm> extensionalDefinition, Collection<FeatureTerm> extensionalDefinition2) throws FeatureTermException;
	public Agreement agree(Sign s1, Sign s2) throws FeatureTermException;
	public boolean isContext(Collection<FeatureTerm> c) throws FeatureTermException;

	

}
