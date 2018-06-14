package restAPI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class AdvancedRulesGenerator {
	
	private final static String consumer_key=System.getenv().get("CONSUMER_KEY");
	private final static String consumer_secret=System.getenv().get("CONSUMER_SECRET");
	private final static String access_token_key=System.getenv().get("ACCESS_TOKEN_KEY");
	private final static String access_token_secret=System.getenv().get("ACCESS_TOKEN_SECRET");

	
	public static JsonElement getJson(String json, String key)
	{
		JsonParser parser = new JsonParser();
    	JsonObject jo = parser.parse(json).getAsJsonObject();
    	return (JsonElement) (jo.get(key));
	}
	//this method ommits the "" in the strings
	public static String getValue(String json, String key) 
	{
						
		JsonParser parser = new JsonParser();
    	JsonObject jo = parser.parse(json).getAsJsonObject();
    	return (jo.get(key).toString().replaceAll("\"", ""));
	}
	public static String ruleConstructor(String json) 
	{
		String rule="";
		JsonParser parser = new JsonParser();
		JsonObject jRead = parser.parse(json).getAsJsonObject();
		JsonObject jWrite= new JsonObject();
		JsonArray jActsRead = (JsonArray) jRead.get("action");
		jWrite.addProperty("name", getValue(json, "name"));
		JsonArray jactionsW = new JsonArray();
		for(JsonElement je : jActsRead)
		{
			JsonObject jActRead = (JsonObject)je;
			if(jActRead.get("name").toString().equalsIgnoreCase("\"email\""))
			{
				
				jWrite.addProperty("text", queryConstructor(json));
				JsonObject jaction = new JsonObject();
				JsonObject jprmt = new JsonObject();
				jprmt.add("to", jActRead.get("adressTo"));
				// fiwoo.platform@gmail.com
				jprmt.addProperty("from", "fiwoo.platform@gmail.com");
				jprmt.addProperty("subject", getValue(jActRead.toString(),"subject").toString());
				jaction.addProperty("type", "email");
				
				jaction.addProperty("template", getValue(jActRead.toString(),"template").toString());
				jaction.add("parameters", jprmt);
				//"interval" : "30e3"
				jaction.addProperty("interval","30e3");
				jactionsW.add(jaction);
				
			}
			//if it isn't an email is an update
			else if (jActRead.get("name").toString().equalsIgnoreCase("\"update\""))
			{
				
				JsonObject jus= (JsonObject) jActRead.get("updatedSensor");
				jWrite.addProperty("text", queryConstructor(json));
				JsonObject jaction = new JsonObject();
				JsonObject jprmt = new JsonObject();
				jprmt.addProperty("id", getValue(jus.toString(), "id"));
				//jprmt.addProperty("type", getValue(jus.toString(), "type"));
				JsonArray attributes= new JsonArray();
				for(JsonElement je1 : (JsonArray)jus.get("attributes"))
				{
					JsonObject jor = (JsonObject)je1;
					attributes.add(jor);			
					
				}
				jprmt.add("attributes", attributes);
				jaction.addProperty("type", "update");
				jaction.add("parameters", jprmt);
				jaction.addProperty("interval","30e3");

				jactionsW.add(jaction);
			}
			else if (jActRead.get("name").toString().equalsIgnoreCase("\"command\""))
			{
				
				JsonObject actionCommandW = new JsonObject();
				jWrite.addProperty("text", queryConstructor(json));
				actionCommandW.addProperty("type", "post");
				JsonObject parametersW = new JsonObject();
				parametersW.addProperty("url", "http://us3.fiwoo.eu:5000/rules/statements/sendcommand/");
				JsonObject headersW = new JsonObject();
				headersW.addProperty("Accept", "application/json");
				parametersW.add("headers", headersW);
				JsonObject jsonW = new JsonObject();
				JsonObject updatedSensorR = jActRead.get("updatedSensor").getAsJsonObject();
				JsonObject attributesR = updatedSensorR.get("attributes").getAsJsonArray().get(0).getAsJsonObject();
				jsonW.addProperty("type", updatedSensorR.get("type").toString().replaceAll("\"", ""));
				jsonW.addProperty("id", updatedSensorR.get("id").toString().replaceAll("\"", ""));
				jsonW.addProperty("attributeName", attributesR.get("name").toString().replaceAll("\"", ""));
				jsonW.addProperty("attributeType", attributesR.get("type").toString().replaceAll("\"", ""));
				jsonW.addProperty("attributeValue", attributesR.get("value").getAsString().replaceAll("\"", ""));
				parametersW.add("json", jsonW);
				actionCommandW.add("parameters", parametersW);
				jactionsW.add(actionCommandW);
			}
			else if(jActRead.get("name").toString().equalsIgnoreCase("\"sms\""))
			{
				
				jWrite.addProperty("text", queryConstructor(json));
				JsonObject jaction = new JsonObject();
				JsonObject jprmt = new JsonObject();
				jprmt.add("to", jActRead.get("numberTo"));
				jaction.addProperty("type", "sms");				
				jaction.addProperty("template", getValue(jActRead.toString(),"template").toString());
				jaction.add("parameters", jprmt);
				jaction.addProperty("interval","30e3");

				jactionsW.add(jaction);
				
			}
			else if(jActRead.get("name").toString().equalsIgnoreCase("\"twitter\""))
			{
				
				jWrite.addProperty("text", queryConstructor(json));
				JsonObject jaction = new JsonObject();
				JsonObject jprmt = new JsonObject();
				jprmt.addProperty("consumer_key", consumer_key);
				jprmt.addProperty("consumer_secret", consumer_secret);
				jprmt.addProperty("access_token_key", access_token_key);
				jprmt.addProperty("access_token_secret", access_token_secret);
				jaction.addProperty("type", "twitter");	
				jaction.addProperty("template", getValue(jActRead.toString(),"template").toString());
				jaction.add("parameters", jprmt);
				jaction.addProperty("interval","30e3");

				jactionsW.add(jaction);
				
			}
			
			
		}
		jWrite.add("action", jactionsW);

		
		JsonObject jo1 = new JsonObject();
		jo1.add("rule", jWrite);
		
		return jo1.toString();
	}
	public static String queryConstructor(String jsonRead)
	{
		String query="select*, \""+getValue(jsonRead, "name")+"\" as ruleName *,";
		String text=(String)getJson(jsonRead, "text").toString();
		JsonArray attributes = (JsonArray)getJson(text,"attributes");
		for(JsonElement je : attributes)
		{
			JsonObject jo = (JsonObject)je;
			query.concat(" ev."+getValue(jo.toString(), "name")+"? as"+getValue(jo.toString(), "name")+", ");
		}
		query=query.substring(0, query.length()-2)+" "
				+"from pattern [every ev=iotEvent(";
		for(JsonElement je : attributes)
		{
			JsonObject jo = (JsonObject)je;
//			if(jo.get("type").toString().equalsIgnoreCase("\"String\""))
//			{
//				
//				query=query+"cast("+getValue(jo.toString(), "name")+"?, String)"+getValue(jo.toString(),"operator")
//				+jo.get("value")+" and ";
//			}
//			else
//			{
				query=query+"cast(cast("+getValue(jo.toString(), "name")+"?, String),"+getValue(jo.toString(), "type")+")"+getValue(jo.toString(),"operator")
				+getValue(jo.toString(),"value")+" and ";
//			}
		}
		query=query.substring(0, query.length()-4)+"and type=\""+getValue(text, "sensorType")+"\" and cast(id?, String)"
				+ "=\""+getValue(text, "sensorId")+"\")]";
		return query;
	}
}
