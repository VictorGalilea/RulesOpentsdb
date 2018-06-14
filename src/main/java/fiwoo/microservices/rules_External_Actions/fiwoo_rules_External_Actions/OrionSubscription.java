package fiwoo.microservices.rules_External_Actions.fiwoo_rules_External_Actions;

import java.util.List;

public class OrionSubscription {

	private List<Entity> entities;
	private List<String> attributes;
	private String reference;
	private String duration;
	private List<NotifyCondition> notifyConditions;
	private String throttling;
	
	public OrionSubscription(List<Entity> entities, List<String> attributes, String reference, String duration,
			List<NotifyCondition> notifyConditions, String throttling) {
		super();
		this.entities = entities;
		this.attributes = attributes;
		this.reference = reference;
		this.duration = duration;
		this.notifyConditions = notifyConditions;
		this.throttling = throttling;
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}
	public List<String> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public List<NotifyCondition> getNotifyConditions() {
		return notifyConditions;
	}
	public void setNotifyConditions(List<NotifyCondition> notifyConditions) {
		this.notifyConditions = notifyConditions;
	}
	public String getThrottling() {
		return throttling;
	}
	public void setThrottling(String throttling) {
		this.throttling = throttling;
	}
}
