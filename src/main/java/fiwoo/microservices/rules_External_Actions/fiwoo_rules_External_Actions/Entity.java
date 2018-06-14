package fiwoo.microservices.rules_External_Actions.fiwoo_rules_External_Actions;

public class Entity {

	public String type;
	public boolean isPattern;
	public String id;
	public Entity(String type, boolean isPattern, String id) {
		super();
		this.type = type;
		this.isPattern = isPattern;
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean getIsPattern() {
		return isPattern;
	}
	public void setIsPattern(boolean isPattern) {
		this.isPattern = isPattern;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
}
