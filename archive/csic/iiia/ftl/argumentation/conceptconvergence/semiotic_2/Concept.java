package csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2;

import java.util.Collection;
import java.util.HashSet;

import csic.iiia.ftl.argumentation.conceptconvergence.tools.LearningPackage;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;

public class Concept {
	
	private Sign s;
	private Collection<FeatureTerm> E;
	private Collection<FeatureTerm> I;
	
	public Concept(String cake, String piece){
		this.s = new Sign(cake, piece);
		this.E = new HashSet<FeatureTerm>();
		this.I = null;
	}
	
	public Concept(Collection<FeatureTerm> E, LearningPackage ln){
		this.s = null;
		this.E = E;
		this.I = null;
	}
	
	public Concept(Sign s, Collection<FeatureTerm> E, LearningPackage ln){
		this.s =s;
		this.E = E;
		this.I = null;
	}
	
	public Concept(Sign s, Collection<FeatureTerm> E, Collection<FeatureTerm> I, LearningPackage ln){
		this.s =s;
		this.E = E;
		this.I = I;
	}
	
	public Sign Sign(){
		return this.s;
	}
	
	// Extensional definition methods
	public Collection<FeatureTerm> ExtensionalDefinition(){
		return this.E;
	}
	
	public void setExtentional(Collection<FeatureTerm> extensional_definition, LearningPackage ln) throws FeatureTermException{
		this.E = new HashSet<FeatureTerm>();
		for(FeatureTerm e : extensional_definition){
			this.E.add(e.clone(ln.ontology()));
		}
	}
	
	public FeatureTerm addExtensional(FeatureTerm e, LearningPackage ln) throws FeatureTermException{
		if(this.E == null)
			this.E = new HashSet<FeatureTerm>();
		FeatureTerm f = e.clone(ln.ontology());
		this.E.add(f);
		return f;
	}
	
	// Intensional definition methods
	public Collection<FeatureTerm> IntensionalDefinition(){
		return this.I;
	}
	
	public void setIntensional(Collection<FeatureTerm> intensional_definition, LearningPackage ln) throws Exception{
		this.I = new HashSet<FeatureTerm>();
		for(FeatureTerm r : intensional_definition){
			this.I.add(r.clone(ln.ontology()));
		}
	}
	
	public void addIntensional(FeatureTerm r, LearningPackage ln) throws FeatureTermException{
		if(this.I == null)
			this.I = new HashSet<FeatureTerm>();
		this.I.add(r.clone(ln.ontology()));
	}
	
	public boolean covers(FeatureTerm e, LearningPackage ln) throws FeatureTermException{
		for(FeatureTerm r : this.I){
			if(r.subsumes(e)){
				return true;
			}
		}
		return false;
	}
	
	public Concept clone(LearningPackage ln){
		Sign s = new Sign(this.Sign().getCake(), this.Sign().getPiece());
		Collection<FeatureTerm> E = new HashSet<FeatureTerm>();
		Collection<FeatureTerm> I = new HashSet<FeatureTerm>();
		E.addAll(this.E);
		I.addAll(this.I);
		Concept c = new Concept(s, E, I, ln);
		return c;
	}

}
