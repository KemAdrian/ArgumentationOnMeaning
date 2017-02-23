package csic.iiia.ftl.argumentation.conceptconvergence.semiotic;

import java.util.ArrayList;
import java.util.HashSet;

import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.argumentation.conceptconvergence.tools.SetCast;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;

public class Concept {
	
	public static boolean DEBUG = false; 
	
	private Sign sign;
	private LearningPackage infos;
	private IntensionalDefinition intensionalDefinition;
	private ExtensionalDefinition extensionalDefinition;
	private Concept brother;
	private Concept old_attacker;
	
	public Concept(Sign s, LearningPackage infos){
		
		this.sign = s;
		s.associate(this);
		
		if(Concept.DEBUG == true){
			System.out.println("creating a concept for "+this.sign.sign());
		}
		
		this.infos = infos;
		this.intensionalDefinition = new IntensionalDefinition(this);
		this.extensionalDefinition = new ExtensionalDefinition(infos, this);
		this.brother = null;
		this.old_attacker = null;
	}
	
	public Sign sign(){
		return sign;
	}
	
	public IntensionalDefinition intensionalDefinition(){
		return intensionalDefinition;
	}
	
	public ExtensionalDefinition extensionalDefinition(){
		return extensionalDefinition;
	}

	public void extend_solutions(FeatureTerm s) {
		this.extensionalDefinition().get_informations().different_solutions().add(s);
		
	}
	
	public LearningPackage getInfos(){
		return this.infos;
	}
	
	public boolean isMeaningful(){
		return !this.intensionalDefinition.getAllGeneralizations().getRules().isEmpty();
	}
	
	public Concept getBrother(){
		return this.brother;
	}
	
	public boolean setBrother(Concept brother){
		this.brother = brother;
		return true;
	}
	
	public boolean hasBrother(){
		if(this.brother == null){
			return false;
		}
		return true;
	}
	
	public boolean hasInsignificantBrother(){
		if(this.hasBrother()){
			if(!this.getBrother().isMeaningful()){
				return true;
			}
		}
		return false;
	}
	
	public Concept getOldAttacker(){
		return this.old_attacker;
	}
	
	
	public boolean setOldAttacker(Concept attacker){
		this.old_attacker = attacker;
		return true;
	}
	
	public boolean hasOldAttacker(){
		if(this.old_attacker != null){
			return true;
		}
		return false;
	}
	
	public boolean isOldAttacker(Concept attacker){
		if(this.hasOldAttacker()){
			if(this.getOldAttacker().equals(attacker)){
				return true;
			}
		}
		return false;
	}
	
	public boolean equivalentToConcept(Concept c) throws FeatureTermException{
		if(c == null){
			return false;
		}
		ArrayList<FeatureTerm> buffer = SetCast.cast(c.extensionalDefinition.get_examples());
		for(FeatureTerm f1 : this.extensionalDefinition.get_examples()){	
			boolean found = false;
			FeatureTerm test_1 = f1.featureValue(infos.description_path().features.get(0));
			HashSet<FeatureTerm> rejected = new HashSet<FeatureTerm>();
			while(!buffer.isEmpty()){
				FeatureTerm f2 = buffer.remove(0);
				FeatureTerm test_2 = f2.featureValue(infos.description_path().features.get(0));
				if(test_1.equivalents(test_2)){
					found = true;
					break;
				}
				rejected.add(f2);
			}
			buffer.addAll(rejected);
			if(!found){
				return false;
			}
		}	
		if(!buffer.isEmpty()){
			return false;
		}	
		return true;
	}

}
