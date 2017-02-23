package csic.iiia.ftl.argumentation.conceptconvergence.semiotic_2;

import csic.iiia.ftl.argumentation.conceptconvergence.Enum.Problem;

public class Case {
	
	private Problem problem;
	private Concept concept1;
	private Concept concept2;
	
	public Case(Problem p, Concept c1, Concept c2){
		this.setProblem(p);
		this.setConcept1(c1);
		this.setConcept2(c2);
	}

	public Problem getProblem() {
		return problem;
	}

	public void setProblem(Problem problem) {
		this.problem = problem;
	}

	public Concept getConcept1() {
		return concept1;
	}

	public void setConcept1(Concept concept1) {
		this.concept1 = concept1;
	}

	public Concept getConcept2() {
		return concept2;
	}

	public void setConcept2(Concept concept2) {
		this.concept2 = concept2;
	}
	
	

}
