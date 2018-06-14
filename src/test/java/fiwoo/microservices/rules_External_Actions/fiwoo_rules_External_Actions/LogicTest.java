package fiwoo.microservices.rules_External_Actions.fiwoo_rules_External_Actions;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import fiwoo.microservices.rules_External_Actions.model.RuleDB;

public class LogicTest {

	private static Logic logic;
	private static Gson gson;
	
	//It runs before any test
	@BeforeClass
	public static void initialize()
	{
		logic = new Logic();
		gson = new Gson();
		gson.serializeNulls();
	}
	
	@Test
	public void dataBaseTest() {
		// Insertion
		String result = logic.getRuleDAO().insert(new RuleDB("id_test", "id_test", "id_test", "id_test", "id_test", "id_test",true));
		assertTrue("error inserting subscription in db. Check database connection and table", result.equals("{\"201\":\"created\"}"));
		
		// Delete
		int result3 = logic.getRuleDAO().delete("id_test","id_test");
		assertTrue("error deleting rule from db",result3 > 0);
	}
	
	@Test
	public void orionSubscriptionTest() {
		String result = "{";		
		
		// creation
		List<String> attributes = new ArrayList<String>();
		List<Entity> entities = new ArrayList<Entity>();
        attributes.add("BloodPressure");
        entities.add(new Entity("entity_type", true, "entity_id"));
		OrionSubscription s = logic.createOrionSubscription("entity_id", "entity_type", attributes, "", "", "");
		assertTrue(s.getEntities().get(0).getId().equals(entities.get(0).getId()));
		
		// send Subscription
		String subscription_result = logic.sendSubscription(s);
		LinkedTreeMap<Object, Object> subscriptionResultMap = (LinkedTreeMap<Object, Object>) gson.fromJson(subscription_result, Object.class);
		String subscriptionId = "";
		if (subscriptionResultMap.get("subscribeError") == null) {
			// No error
			subscriptionId = ((LinkedTreeMap<Object, Object>)subscriptionResultMap.get("subscribeResponse")).get("subscriptionId").toString();
			result += "\"subscription\" : " + subscription_result + ", \n";
		} else {
			// Error
			assertTrue("subscription id not found",false);
		}
		
		assertTrue("subscription id found",!subscriptionId.equals(""));
		
		// delete subscription
		String result2 = logic.deleteSubscription(subscriptionId);
		assertTrue("subscription deleted", result2.equals("{\"deleted subscription\":\""+ subscriptionId + "\"}"));
	}
	
	@Test
	public void perseoRule() {
	     String rule2 = "{\n" + 
	          		"    \"name\": \"blood_rule_update2\",\n" + 
	          		"    \"text\": \"select *,\\\"blood_rule_update2\\\" as ruleName from pattern [every ev=iotEvent(cast(cast(BloodPressure?,String),float)>1.5 and type=\\\"BloodMeter\\\" )]\",\n" + 
	          		"    \"action\": {\n" + 
	          		"        \"type\": \"update\",\n" + 
	          		"        \"parameters\": {\n" + 
	          		"			 \"id\": \"bloodm1\", \n"+
	          		"			 \"type\": \"BloodMeter\", \n"+
	          		"            \"attributes\": [\n" + 
	          		"                {\n" + 
	          		"                    \"name\": \"abnormal\",\n" + 
	          		"                    \"value\": \"true\",\n" + 
	          		"                    \"type\": \"boolean\"\n" + 
	          		"                },\n" + 
	          		"                {\n" + 
	          		"                    \"name\": \"other\",\n" + 
	          		"                    \"value\": 1.34,\n" + 
	          		"                    \"type\": \"Number\"\n" + 
	          		"                }\n" + 
	          		"            ]\n" + 
	          		"        }\n" + 
	          		"    }\n" + 
	          		"}";
	     //transform rule
	     rule2 = logic.changeRuleName(rule2, "user_id_test", "blood_rule_update2");
	     
	    //String send rule to perseo 
		String result = logic.sendRule(rule2);
		LinkedTreeMap<Object, Object>resultMap = (LinkedTreeMap<Object, Object>) gson.fromJson(result, Object.class);
		
		//Delete rule from perseo
		String result2 = logic.deleteRuleInPerseo(logic.createRuleId("user_id_test", "blood_rule_update2"));
		LinkedTreeMap<Object, Object>resultMap2 = (LinkedTreeMap<Object, Object>) gson.fromJson(result2, Object.class);

		assertTrue("error mesage returned on creation:"+resultMap.get("error"),  resultMap.get("error") == null);
		assertTrue("error mesage returned on delete:"+resultMap2.get("error"), resultMap2.get("error") == null);

	}
	
	@Test
	public void parseCompleteRuleTest() {
		String ruleJson = "{\n" + 
			"    \"name\": \"blood_rule_update2\",\n" + 
			"    \"text\": \"select *,\\\"blood_rule_update2\\\" as ruleName from pattern [every ev=iotEvent(cast(cast(BloodPressure?,String),float)>1.5 and type=\\\"BloodMeter\\\" )]\",\n" + 
			"    \"action\": {\n" + 
			"        \"type\": \"update\",\n" + 
			"        \"parameters\": {\n" + 
			"			 \"id\": \"bloodm1\", \n"+
			"			 \"type\": \"BloodMeter\", \n"+
			"            \"attributes\": [\n" + 
			"                {\n" + 
			"                    \"name\": \"abnormal\",\n" + 
			"                    \"value\": \"true\",\n" + 
			"                    \"type\": \"boolean\"\n" + 
			"                },\n" + 
			"                {\n" + 
			"                    \"name\": \"other\",\n" + 
			"                    \"value\": 1.34,\n" + 
			"                    \"type\": \"Number\"\n" + 
			"                }\n" + 
			"            ]\n" + 
			"        }\n" + 
			"    }\n" + 
			"}";
		String result = logic.parseAdvancedRule(ruleJson, "user_id_test2", "empty description",false);
		LinkedTreeMap<Object, Object>resultMap = (LinkedTreeMap<Object, Object>) gson.fromJson(result, Object.class);
		
		String resultDelete = logic.deleteRuleAndSubscription("user_id_test2", "blood_rule_update2");
		LinkedTreeMap<Object, Object>resultMap2 = (LinkedTreeMap<Object, Object>) gson.fromJson(resultDelete, Object.class);

		assertTrue("subscription not created", resultMap.get("subscription") != null);
		assertTrue("rule in perseo not created", resultMap.get("perseo") != null);
		assertTrue("rule not stored in database", resultMap.get("database") != null);
		
		assertTrue("subscription not deleted", resultMap2.get("subscription") != null);
		assertTrue("rule in perseo not deleted", resultMap2.get("perseo") != null);
		assertTrue("rule not deleted from DB", resultMap2.get("database") != null);

	}
	
	@Test
	public void badStatement() {
		String wrongRuleJson = "{\n" + 
				"    \"name\": \"test__update2\",\n" + 
				"    \"text\": \"selectasdasd *,\\\"test__update2\\\" as ruleName from pattern [every ev=iotEvent(cast(cast(BloodPressure?,String),float)>1.5 and type=\\\"BloodMeter\\\" )]\",\n" + 
				"    \"action\": {\n" + 
				"        \"type\": \"update\",\n" + 
				"        \"parameters\": {\n" + 
				"			 \"id\": \"bloodm1\", \n"+
				"			 \"type\": \"BloodMeter\", \n"+
				"            \"attributes\": [\n" + 
				"                {\n" + 
				"                    \"name\": \"abnormal\",\n" + 
				"                    \"value\": \"true\",\n" + 
				"                    \"type\": \"boolean\"\n" + 
				"                },\n" + 
				"                {\n" + 
				"                    \"name\": \"other\",\n" + 
				"                    \"value\": 1.34,\n" + 
				"                    \"type\": \"Number\"\n" + 
				"                }\n" + 
				"            ]\n" + 
				"        }\n" + 
				"    }\n" + 
				"}";
			String result = logic.parseAdvancedRule(wrongRuleJson, "user_id_test2", "no description",false);
			LinkedTreeMap<Object, Object>resultMap = (LinkedTreeMap<Object, Object>) gson.fromJson(result, Object.class);
			
			// check that nothing was created. There is an error in statement.
			assertTrue("subscription not created", resultMap.get("subscription") == null);
			assertTrue("rule in perseo not created", resultMap.get("perseo") == null);
			assertTrue("rule not stored in database", resultMap.get("database") == null);
	}
}
