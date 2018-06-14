package fiwoo.microservices.rules_External_Actions.model;

public class RuleDB {

	private String rule_id;
	private String user_id;
	private String rule_name;
	private String rule_description;
	private String rule;
	private String orion_id;
	private boolean basic;
	
	
	
	public RuleDB(String rule_id,String user_id, String rule_name, String rule_description, String rule,
			String orion_id, boolean basic) {
		super();
		this.user_id = user_id;
		this.rule_id = rule_id;
		this.rule_name = rule_name;
		this.rule_description = rule_description;
		this.rule = rule;
		this.orion_id = orion_id;
		this.basic=basic;
		
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getRule_id() {
		return rule_id;
	}
	public void setRule_id(String rule_id) {
		this.rule_id = rule_id;
	}
	public String getRule_name() {
		return rule_name;
	}
	public void setRule_name(String rule_name) {
		this.rule_name = rule_name;
	}
	public String getRule_description() {
		return rule_description;
	}
	public void setRule_description(String rule_description) {
		this.rule_description = rule_description;
	}
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	public String getOrion_id() {
		return orion_id;
	}
	public void setOrion_id(String orion_id) {
		this.orion_id = orion_id;
	}
	public boolean isBasic() {
		return basic;
	}
	public void setBasic(boolean basic) {
		this.basic = basic;
	}
	
}
