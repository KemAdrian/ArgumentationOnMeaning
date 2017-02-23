package tools;

import interfaces.Agent;

public class Token {
	
	private static Agent defender;
	private static Agent attacker;
	
	public static void initialize(Agent a, Agent d){
		Token.attacker = a;
		Token.defender = d;
	}
	
	public static void switchRoles(){
		Agent temp = Token.attacker;
		Token.attacker = Token.defender;
		Token.defender = temp;
	}
	
	public static Agent defender(){
		return Token.defender;
	}
	
	public static Agent attacker(){
		return Token.attacker;
	}

}
