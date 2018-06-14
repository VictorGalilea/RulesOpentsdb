package fiwoo.microservices.rules_External_Actions.fiwoo_rules_External_Actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import fiwoo.microservices.rules_External_Actions.dao.RuleDBDAO;
import fiwoo.microservices.rules_External_Actions.model.RuleDB;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
       String rule = "{\n" + 
       		"    \"name\": \"blood_rule_update\",\n" + 
       		"    \"text\": \"select *,\\\"blood_rule_update\\\" as ruleName from pattern [every ev=iotEvent(cast(cast(BloodPressure?,String),float)>1.5 and type=\\\"BloodMeter\\\" )]\",\n" + 
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
       //Logic logic = new Logic();

       
       //logic.sendRule(rule);
       //System.out.println(logic.parseAdvancedRule(rule, "user1"));
      //System.out.println(logic.parseAdvancedRule(rule2, "user1"));

      //System.out.println(logic.deleteRuleAndSubscription("user1", "blood_rule_update2"));
       //System.out.println(logic.getRulesOfUser("user1"));
       
//       logic.changeRuleName(rule, "user", "blood_rule_update");
//       
       
//       System.out.println(logic.deleteRuleInPerseo("user1_blood_rule_update"));
//       List<String> attributes = new ArrayList<String>();
//       attributes.add("BloodPressure");
//       
       
       //OrionSubscription o= logic.createOrionSubscription("bloodm1","BloodMeter", attributes, "", "", "");
       //logic.sendSubscription(o);
       //logic.deleteSubscription("5a84079ba657924f458f8899");
    	
    	/*Comment/uncomment to test data base storage*/

//    	ApplicationContext context =
//    		new ClassPathXmlApplicationContext("Spring-Module.xml");
//
//    	RuleDBDAO ruleDAO = (RuleDBDAO) context.getBean("ruleDBDAO");
//    	
//        ruleDAO.delete("rule_id", "user_id1");
//
//    	RuleDB rule = new RuleDB("rule_id", "user_id1", "rule_name", "rule_description", "rule", "orion_id");
//        ruleDAO.insert(rule);
//        
//        ruleDAO.delete("rule_id2","user_id2");
//
//    	RuleDB rule2 = new RuleDB("rule_id2", "user_id2", "rule_name", "rule_description", "rule", "orion_id");
//        ruleDAO.insert(rule2);
//
//        RuleDB rule1 = ruleDAO.findByUser("user_id1");
//        System.out.println(rule1);

    	
    }
}
