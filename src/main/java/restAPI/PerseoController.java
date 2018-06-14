package restAPI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpHead;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;

import fiwoo.microservices.rules_External_Actions.fiwoo_rules_External_Actions.Logic;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

@RestController
public class PerseoController {

	private static final String SECRET = System.getenv("SECRET");
	private static final String APPLICATION_JSON_NGSI = "application/ngsi+json";
	private static final String APPLICATION_JSON_LD = "application/ld+json";
	public String orion_host =System.getenv("ORION_HOST");
	public String orion_port = System.getenv("ORION_PORT");
	private static Logic logic;

	@Autowired
	private HttpServletRequest context;

	public PerseoController() {
		logic = new Logic();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/statements", produces = { MediaType.APPLICATION_JSON_VALUE,
			APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
	public ResponseEntity getRules(@RequestHeader("X-Authorization-s4c") String jwtHeader,
			@RequestParam(name="order", defaultValue="Asc") String order, @RequestParam(name="sort",defaultValue="Rule_id" ) String sort, @RequestParam(name="page", defaultValue="0") int page,
			@RequestParam(name="limit",defaultValue="15") int limit) throws IllegalArgumentException, UnsupportedEncodingException {
		// String
		// jwtHeader="eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.eusFgDqmqIg3_c8buW6ohKCKILHI2Q3ImIoEjSr2Ih42RikUorPy-AntxBrtt82Fc1lnJD9HwF5wnuY76Ezehw";
		// **
		// @ResponseHeader("-x-total-count") String count="";
		page++;
		HttpHeaders headers = new HttpHeaders();
		JsonParser parser = new JsonParser();
		JsonArray inArray = (JsonArray) parser.parse(logic.getRulesOfUser(decodeUserIdFromJWT(jwtHeader), order, sort));
		headers.add("x-total-count", inArray.size() + "");

		int n1 = limit * (page - 1);
		int n2 = limit * page - 1;
		if (n1 < 0)
			n1 = 0;
		if (n2 < 0)
			n2 = 0;
		if (n2 > inArray.size())
			n2 = inArray.size();
		if (n1 > inArray.size())
			n1 = inArray.size();
		if (n1 > n2)
			n1 = n2;
		JsonArray outArray = new JsonArray();
		for (int i = n1; i <= n2; i++) {
			if (inArray.size() > i) {
				outArray.add(inArray.get(i));
			}
		}

		String acceptHeader = context.getHeader("Accept");
		String result = outArray.toString();
		ResponseEntity<String> re = new ResponseEntity<String>(result, headers, HttpStatus.OK);

		if (acceptHeader.equals("application/ld+json")) {
			result = transformJsonLd(result);

			return re;
		}
		// **
		return re;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/swagger", produces = { MediaType.APPLICATION_JSON_VALUE,
			APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
	public ResponseEntity getSwagger() {
		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;
		String SwaggerJson = "";

		try {
			// Apertura del fichero y creacion de BufferedReader para poder
			// hacer una lectura comoda (disponer del metodo readLine()).
			String absolutePath = new File("").getAbsolutePath();
			SwaggerJson = "";
			archivo = new File(absolutePath + File.separator + "src" + File.separator + "main" + File.separator
					+ "resources" + File.separator + "Rules_perseo_Swagger_With_Tokens.json");
			fr = new FileReader(archivo);
			br = new BufferedReader(fr);

			// Lectura del fichero
			String linea;
			while ((linea = br.readLine()) != null)
				SwaggerJson = SwaggerJson + "\n" + linea;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// En el finally cerramos el fichero, para asegurarnos
			// que se cierra tanto si todo va bien como si salta
			// una excepcion.
			try {
				if (null != fr) {
					fr.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(SwaggerJson);

	}

	// Post Method
	/*
	 * ENTRY JSON: { "user_id": "user_id", "rule" : {rule_JSON} } *
	 * 
	 */

	@RequestMapping(value = "/statements/advanced/add", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE, APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
	@ResponseBody
	public ResponseEntity addRule(@RequestBody String body, @RequestHeader("X-Authorization-s4c") String jwtHeader)
			throws IllegalArgumentException, UnsupportedEncodingException {
		String acceptHeader = context.getHeader("Accept");
		Gson gson = new GsonBuilder().serializeNulls().create();
		gson.serializeNulls();
		Object body_aux = gson.fromJson(body, Object.class);
		LinkedTreeMap<Object, Object> body_map = (LinkedTreeMap<Object, Object>) body_aux;
		if (body_map.get("rule") == null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"A rule must be sent\"}");
		String ruleJson = gson.toJson(body_map.get("rule"), LinkedTreeMap.class);

		// if (body_map.get("user_id") == null)
		// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"A
		// user_id must be sent\"}");
		// String user_id = body_map.get("user_id").toString();
		String description = "no description";
		if (body_map.get("description") != null)
			description = body_map.get("description").toString();
		String response = logic.parseAdvancedRule(ruleJson, decodeUserIdFromJWT(jwtHeader), description, false);
		// **
		if (acceptHeader.equals("application/ld+json")) {
			response = transformJsonLd(response);
			if (response.contains("\"201\":\"created\""))
				return ResponseEntity.status(HttpStatus.CREATED).body(response);
			else
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		// **
		if (response.contains("\"201\":\"created\"")) {
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} else {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}
	@RequestMapping(value = "/statements/sendcommand", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE, APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
	@ResponseBody
	public ResponseEntity commandRule(@RequestBody String body)
			throws IllegalArgumentException, UnsupportedEncodingException {
		//String acceptHeader = context.getHeader("Accept");
		JsonParser parser = new JsonParser();
		JsonObject bodyJ =(JsonObject) parser.parse(body);
		String type=bodyJ.get("type").getAsString();
		String id=bodyJ.get("id").getAsString();
		String attributeName=bodyJ.get("attributeName").getAsString();
		String attributeType=bodyJ.get("attributeType").getAsString();
		String attributeValue=bodyJ.get("attributeValue").getAsString();
		//String curl ="curl -X POST   ip-172-31-44-93.eu-west-1.compute.internal:1026/v1/updateContext/   -H 'cache-control: no-cache'   -H 'content-type: application/json'   -H 'fiware-service: howtoservice'   -H 'fiware-servicepath: /howto'   -H 'postman-token: 7cff0db0-979a-0cfb-f2b7-620c4a1e763d'   -d '";
		JsonArray contextElements = new JsonArray();
		JsonObject contextElement = new JsonObject();
		contextElement.addProperty("type", type);
		contextElement.addProperty("isPattern", false);
		contextElement.addProperty("id", id);
		JsonArray attributes = new JsonArray();
		JsonObject attribute = new JsonObject();
		attribute.addProperty("name", attributeName);
		attribute.addProperty("type", attributeType);
		attribute.addProperty("value", attributeValue);
		attributes.add(attribute);
		contextElement.add("attributes", attributes);
		contextElements.add(contextElement);
		JsonObject bodyOutput = new JsonObject();//ALBERGA BODY FINAL
		bodyOutput.add("contextElements", contextElements);
		bodyOutput.addProperty("updateAction", "UPDATE");
		
		String stringURL = "http://" + orion_host+":"+orion_port+"/v1/updateContext/";
		
        URL url;
        StringBuilder sb  = new StringBuilder();
        try {
            url = new URL(stringURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("fiware-service", "howtoservice");
            conn.setRequestProperty("fiware-servicepath", "/howto");
            conn.setRequestProperty("cache-value", "application/json");

           
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            
            writer.write(bodyOutput.toString());
            writer.flush();
            writer.close();
            os.close();

           
          //get what the POST request returns

          sb = new StringBuilder();
          int HttpResult = conn.getResponseCode(); 
          // sb.append(HttpResult + " : " + conn.getResponseMessage());
          
          if (HttpResult == HttpURLConnection.HTTP_OK) {
              BufferedReader br = new BufferedReader(
                      new InputStreamReader(conn.getInputStream(), "utf-8"));
              String line = null;  
              while ((line = br.readLine()) != null) {  
                  sb.append(line + "\n");  
              }
              br.close();
              return ResponseEntity.status(HttpStatus.OK).body(sb.toString());
              
          } else {
        	  if (conn.getErrorStream()!=null) {
        		  BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
        		  String line = null;  
                  while ((line = br.readLine()) != null) {  
                      sb.append(line + "\n");  
                  }
                  br.close();
                  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        	  }
          }  
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error");
        }

	}

	@RequestMapping(value = "/statements/basic/add", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE, APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
	@ResponseBody
	public ResponseEntity addBasicRule(@RequestBody String body, @RequestHeader("X-Authorization-s4c") String jwtHeader)
			throws IllegalArgumentException, UnsupportedEncodingException {
		String acceptHeader = context.getHeader("Accept");
		String description = "no description";
		JsonParser jp = new JsonParser();
		JsonObject jo = (JsonObject) jp.parse(body);
		String advancedRule = AdvancedRulesGenerator.ruleConstructor(jo.toString());
		if (jo.get("description") != null) {
			description = jo.get("description").getAsString();
		}
		String response = logic.parseAdvancedRule(AdvancedRulesGenerator.getJson(advancedRule, "rule").toString(),
				decodeUserIdFromJWT(jwtHeader), description, true);
		// **
		if (acceptHeader.equals("application/ld+json")) {
			response = transformJsonLd(response);
			if (response.contains("\"201\":\"created\"")) {
				logic.AddBasicRuleToDB(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), jo.get("name").getAsString()),
						jo.getAsString());
				return ResponseEntity.status(HttpStatus.CREATED).body(response);
			} else
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (response.contains("\"201\":\"created\"")) {
			logic.AddBasicRuleToDB(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), jo.get("name").getAsString()),
					jo.toString());
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} else
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// Delete Methods
	@RequestMapping(value = "/statements", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE,
			APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
	@ResponseBody
	public ResponseEntity deleteRule(@RequestParam("rule_name") String rule_name,
			@RequestHeader("X-Authorization-s4c") String jwtHeader) {
		String response = logic.deleteRuleAndSubscription(decodeUserIdFromJWT(jwtHeader), rule_name);
		String acceptHeader = context.getHeader("Accept");
		if (acceptHeader.equals("application/ld+json")) {
			response = transformJsonLd(response);
			if (response.contains("\"error\" : \"Rule does not exist\""))
				return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
			else {
				if (logic.isBasic(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), rule_name))) {

					logic.deleteBasicRuleFromDB(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), rule_name));
				}
				return new ResponseEntity(response, HttpStatus.OK);
			}
		}
		if (logic.isBasic(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), rule_name))) {

			logic.deleteBasicRuleFromDB(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), rule_name));
		}
		// **
		if (response.contains("\"error\" : \"Rule does not exist\""))
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		else
			return new ResponseEntity(response, HttpStatus.OK);
	}

	// Delete Methods
	@RequestMapping(value = "/statements/Delete/all", method = RequestMethod.DELETE, produces = {
			MediaType.APPLICATION_JSON_VALUE, APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
	@ResponseBody
	public ResponseEntity deleteAllRule(@RequestHeader("X-Authorization-s4c") String jwtHeader) {
		JsonParser parser = new JsonParser();
		JsonArray rules = (JsonArray) parser.parse(logic.getRulesOfUser(decodeUserIdFromJWT(jwtHeader), "", ""));
		int i = 0;
		int nRulesOld = rules.size();
		String response = "";
		for (JsonElement je : rules) {
			JsonObject jo = (JsonObject) je;
			String rule_name = jo.get("rule_name").getAsString();
			response = logic.deleteRuleAndSubscription(decodeUserIdFromJWT(jwtHeader), rule_name);
			String acceptHeader = context.getHeader("Accept");
			if (acceptHeader.equals("application/ld+json")) {
				response = transformJsonLd(response);
				if (response.contains("\"error\" : \"Rule does not exist\""))
					return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
				else {
					if (logic.isBasic(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), rule_name))) {

						logic.deleteBasicRuleFromDB(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), rule_name));
					}
					return new ResponseEntity(response, HttpStatus.OK);
				}
			}
			if (logic.isBasic(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), rule_name))) {

				logic.deleteBasicRuleFromDB(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), rule_name));
			}
			// **
			if (response.contains("\"error\" : \"Rule does not exist\""))
				return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
			else
				i++;
		}
		if (i == nRulesOld)
			return new ResponseEntity(response, HttpStatus.OK);
		else
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);

	}

	@RequestMapping(value = "/statements/advanced/modify", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE, APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
	@ResponseBody
	public ResponseEntity modifyAdvancedRule(@RequestBody String body,
			@RequestParam("old_rule_name") String oldRuleName, @RequestHeader("X-Authorization-s4c") String jwtHeader)
			throws IllegalArgumentException, UnsupportedEncodingException {
		String acceptHeader = context.getHeader("Accept");
		Gson gson = new GsonBuilder().serializeNulls().create();
		gson.serializeNulls();
		JsonParser parser = new JsonParser();
		JsonObject bodyJ = (JsonObject) parser.parse(body);
		if (bodyJ.get("newRule") == null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"A new rule must be sent\"}");
		String ruleJson = bodyJ.get("newRule").toString();
		String description = "no description";
		if (bodyJ.has("description"))
			description = bodyJ.get("description").getAsString();
		if (!logic.existsBasicRule(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), oldRuleName))
				&& (logic.existsRule(oldRuleName, decodeUserIdFromJWT(jwtHeader)))) {
			
				logic.deleteRuleAndSubscription(decodeUserIdFromJWT(jwtHeader), oldRuleName);
			
			String response = logic.parseAdvancedRule(ruleJson, decodeUserIdFromJWT(jwtHeader), description, false);
			if (acceptHeader.equals("application/ld+json")) {
				response = transformJsonLd(response);
				if (response.contains("\"201\":\"created\"")) {
					return ResponseEntity.status(HttpStatus.CREATED).body(response);
				}

				else {
					// deleteRule(oldRuleName, jwtHeader);

					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

				}
			}
			// **
			if (response.contains("\"201\":\"created\"")) {
				//deleteRule(oldRuleName, jwtHeader);
				return ResponseEntity.status(HttpStatus.CREATED).body(response);
			} else {
				// deleteRule(oldRuleName, jwtHeader);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
		}
		JsonObject jo = new JsonObject();
		jo.addProperty("error", "old Rule doesn't exist OR IS BASIC");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jo.toString());
	}

	@RequestMapping(value = "/statements/basic/modify", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE, APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
	@ResponseBody
	public ResponseEntity modifyBasicRule(@RequestBody String body, @RequestParam("old_rule_name") String oldRuleName,
			@RequestHeader("X-Authorization-s4c") String jwtHeader)
			throws IllegalArgumentException, UnsupportedEncodingException {
		String acceptHeader = context.getHeader("Accept");
		Gson gson = new GsonBuilder().serializeNulls().create();
		gson.serializeNulls();
		if (logic.existsBasicRule(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), oldRuleName))) {
			JsonParser parser = new JsonParser();
			JsonObject bodyJ = (JsonObject) parser.parse(body);
			if (!bodyJ.has("newRule"))
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"A new rule must be sent\"}");
			String ruleJson = AdvancedRulesGenerator.ruleConstructor(bodyJ.get("newRule").toString());
			JsonObject ruleJsonJ = (JsonObject) parser.parse(ruleJson);
			ruleJson = ruleJsonJ.get("rule").toString();
			String description = "no description";
			if (((JsonObject) bodyJ.get("newRule")).has("description"))
				description = ((JsonObject) bodyJ.get("newRule")).get("description").toString();
			JsonObject newRule = (JsonObject) bodyJ.get("newRule");

			String response = "";
			if (acceptHeader.equals("application/ld+json")) {
				response = transformJsonLd(response);
				//if (response.contains("\"201\":\"created\"")) {
					logic.deleteBasicRuleFromDB(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), oldRuleName));
					logic.deleteRuleAndSubscription(decodeUserIdFromJWT(jwtHeader), oldRuleName);
					logic.AddBasicRuleToDB(
							logic.createRuleId(decodeUserIdFromJWT(jwtHeader), newRule.get("name").getAsString()),
							bodyJ.get("newRule").toString());
					response=logic.parseAdvancedRule(ruleJson, decodeUserIdFromJWT(jwtHeader), description, true);
					if (response.contains("\"201\":\"created\"")) {
					return ResponseEntity.status(HttpStatus.CREATED).body(response);}
				//}

				else
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
			// **
			logic.deleteBasicRuleFromDB(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), oldRuleName));
			logic.deleteRuleAndSubscription(decodeUserIdFromJWT(jwtHeader), oldRuleName);
			logic.AddBasicRuleToDB(
					logic.createRuleId(decodeUserIdFromJWT(jwtHeader), newRule.get("name").getAsString()),
					bodyJ.get("newRule").toString());
			response=logic.parseAdvancedRule(ruleJson, decodeUserIdFromJWT(jwtHeader), description, true);
			if (response.contains("\"201\":\"created\"")) {
			return ResponseEntity.status(HttpStatus.CREATED).body(response);}
		//}

		else
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		JsonObject jo = new JsonObject();
		jo.addProperty("error", "old basic rule doesn't exist");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jo.toString());

	}

	@RequestMapping(method = RequestMethod.GET, value = "/statements/advanced/get", produces = {
			MediaType.APPLICATION_JSON_VALUE, APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
	public ResponseEntity getAdvancedRule(@RequestHeader("X-Authorization-s4c") String jwtHeader,
			@RequestParam("rule_name") String ruleName) throws IllegalArgumentException, UnsupportedEncodingException {
		// String
		// jwtHeader="eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.eusFgDqmqIg3_c8buW6ohKCKILHI2Q3ImIoEjSr2Ih42RikUorPy-AntxBrtt82Fc1lnJD9HwF5wnuY76Ezehw";
		// **
		String acceptHeader = context.getHeader("Accept");
		String result = logic.getAdvancedRuleFromDB(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), ruleName));
		if (acceptHeader.equals("application/ld+json")) {
			result = transformJsonLd(result);
			return ResponseEntity.status(HttpStatus.OK).body(result);
		}
		// **
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/statements/basic/get", produces = {
			MediaType.APPLICATION_JSON_VALUE, APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
	public ResponseEntity getBasicRule(@RequestHeader("X-Authorization-s4c") String jwtHeader,
			@RequestParam("rule_name") String ruleName) throws IllegalArgumentException, UnsupportedEncodingException {
		// String
		// jwtHeader="eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.eusFgDqmqIg3_c8buW6ohKCKILHI2Q3ImIoEjSr2Ih42RikUorPy-AntxBrtt82Fc1lnJD9HwF5wnuY76Ezehw";
		// **
		String acceptHeader = context.getHeader("Accept");
		String result = logic.getBasicRuleFromDB(logic.createRuleId(decodeUserIdFromJWT(jwtHeader), ruleName));
		if (acceptHeader.equals("application/ld+json")) {
			result = transformJsonLd(result);
			return ResponseEntity.status(HttpStatus.OK).body(result);
		}
		// **
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	private String decodeUserIdFromJWT(String jwtHeader) {
		jwtHeader = jwtHeader.replace("Bearer ", "");
		String user_id = "";
		// HMAC
		try {
			Algorithm algorithmHS = Algorithm.HMAC512(SECRET);
			JWTVerifier verifier = JWT.require(algorithmHS).withIssuer("s4c.microservices.authorization").build(); // Reusable
																													// verifier
																													// instance
			DecodedJWT jwt = verifier.verify(jwtHeader);

			// DecodedJWT jwt = JWT.decode(jwtHeader);
			// user is inside the jwt in the sub field
			String serializedUser = jwt.getSubject();
			Gson gson = new GsonBuilder().serializeNulls().create();
			gson.serializeNulls();
			Object user = gson.fromJson(serializedUser, Object.class);
			LinkedTreeMap<Object, Object> user_map = (LinkedTreeMap<Object, Object>) user;
			user_id = (String) user_map.get("id").toString();
			user_id = base64hash(user_id);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return user_id;
	}

	// transforma un json añadiendole los context por defecto
	private String transformJsonLd(String originalJson) {
		String[] contextos = { "https://www.w3.org/ns/activitystreams" };
		JsonArray jarrcontext = createJarray(contextos);
		JsonParser jp = new JsonParser();
		JsonObject jsonobj = new JsonObject();
		String originalJsonld = formatLd(originalJson);
		jsonobj = jp.parse(originalJsonld).getAsJsonObject();
		jsonobj.add("@context", jarrcontext);
		return (jsonobj.toString());
	}

	// Recibe un array de strings, el cual son los context que se quieren añadir al
	// json
	public JsonArray createJarray(String[] context) {
		JsonArray jarrcontext = new JsonArray();
		for (int i = 0, a = context.length; i < a; i++) {
			jarrcontext.add(context[i]);
		}
		return jarrcontext;
	}

	public String formatLd(String original) {
		String aux = original.substring(1, original.length());
		aux = aux + "\"type\"" + ": \"Event\",";
		// aux = aux + "\"name\"" + ": \"nombreEvento\",";
		aux.replace("\"data\": [", "\"type\"" + ":  \"Collection\"," + "\r\n" + "\"items\": [");
		return aux;
	}

	private String base64hash(String s) {
		byte[] encodedBytes = Base64.encodeBase64(s.getBytes());
		String encoded = new String(encodedBytes);
		encoded = encoded.replaceAll("=", "");
		return encoded;
	}
@RequestMapping(value = "/statements/postOpenTsdb", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE, APPLICATION_JSON_LD, APPLICATION_JSON_NGSI })
			@ResponseBody
			
			public ResponseEntity postOpenTsdb(@RequestBody String body)
			throws IllegalArgumentException, UnsupportedEncodingException {
			//String acceptHeader = context.getHeader("Accept");
		System.out.println("ooooooooooooooooooooooooooooooooooooooooeeeeeeeeeeeeeoooooooooooooeeeeeeeeeeeeeeeeeeeeoooooooooooooeeeeee");	
		JsonParser parser = new JsonParser();
			JsonObject bodyJ =(JsonObject) parser.parse(body);
			String metric=bodyJ.get("metric").getAsString();
			long timestamp=bodyJ.get("timestamp").getAsLong();
			double value=bodyJ.get("value").getAsDouble();
			//String tagHost=bodyJ.get("tagHost").getAsString();
			JsonObject tags= new JsonObject();
			JsonObject bodyOut = new JsonObject();
			bodyOut.addProperty("metric", metric);
			bodyOut.addProperty("timestamp", timestamp);
			bodyOut.addProperty("value", value);
			tags.addProperty("host", "web0");
			bodyOut.add("tags", tags);
			String stringURL = "http://51.144.77.157:4242/api/put?summary";
			        URL url;
			        StringBuilder sb  = new StringBuilder();
			        try {
			            url = new URL(stringURL);

			            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			            conn.setReadTimeout(15000);
			            conn.setConnectTimeout(15000);
			            conn.setRequestMethod("POST");
			            conn.setDoInput(true);
			            conn.setDoOutput(true);
			           
			           
			            OutputStream os = conn.getOutputStream();
			            BufferedWriter writer = new BufferedWriter(
			                    new OutputStreamWriter(os, "UTF-8"));
			            
			            writer.write(bodyOut.toString());
			            writer.flush();
			            writer.close();
			            os.close();

			           
			          //get what the POST request returns

			          sb = new StringBuilder();
			          int HttpResult = conn.getResponseCode(); 
			          // sb.append(HttpResult + " : " + conn.getResponseMessage());
			          
			          if (HttpResult == HttpURLConnection.HTTP_OK) {
			              BufferedReader br = new BufferedReader(
			                      new InputStreamReader(conn.getInputStream(), "utf-8"));
			              String line = null;  
			              while ((line = br.readLine()) != null) {  
			                  sb.append(line + "\n");  
			              }
			              br.close();
			              return ResponseEntity.status(HttpStatus.OK).body(sb.toString());
			              
			          } else {
			          if (conn.getErrorStream()!=null) {
			          BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
			          String line = null;  
			                  while ((line = br.readLine()) != null) {  
			                      sb.append(line + "\n");  
			                  }
			                  br.close();
			                  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
			          }
			          }  
			        } catch (Exception e) {
			            e.printStackTrace();
			        } finally {
			        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error");
			        }

			}


}

