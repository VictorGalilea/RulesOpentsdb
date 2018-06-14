package fiwoo.microservices.rules_External_Actions.fiwoo_rules_External_Actions;

import java.util.List;

public class NotifyCondition {
	private String type;
	private List<String> condValues;
	public NotifyCondition(String type, List<String> condValues) {
		super();
		this.type = type;
		this.condValues = condValues;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getCondValues() {
		return condValues;
	}
	public void setCondValues(List<String> condValues) {
		this.condValues = condValues;
	}
	
	
}
