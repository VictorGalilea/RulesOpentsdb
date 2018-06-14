package fiwoo.microservices.rules_External_Actions.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.*;

import javax.sql.DataSource;

import fiwoo.microservices.rules_External_Actions.model.RuleDB;


public class JdbcRuleDAO implements RuleDBDAO{
	private DataSource dataSource;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public String insert(RuleDB rule) {
		String sql = "INSERT INTO perseo_rules" +
				"(RULE_ID, USER_ID, RULE_NAME, RULE_DESCRIPTION, RULE, ORION_ID, BASIC) "
				+ "VALUES (?,?,?,?,?,?,?)";
		
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, rule.getRule_id());
			ps.setString(2, rule.getUser_id());
			ps.setString(3, rule.getRule_name());
			ps.setString(4, rule.getRule_description());
			ps.setString(5, rule.getRule());
			ps.setString(6, rule.getOrion_id());
			ps.setBoolean(7, rule.isBasic());
			ps.executeUpdate();

			ps.close();
			return "{\"201\":\"created\"}";
		} catch (SQLException e) {
			return "{\""+ e.getErrorCode() + "\":" + "\"" +e.getMessage()+ "\"}";
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}
	}
	
	public List<RuleDB> findByUser(String user_id, String order, String sort) {

		String sql = "SELECT * FROM perseo_rules WHERE USER_ID = ? ";

		Connection conn = null;
		List<RuleDB> rules = new ArrayList<RuleDB>();
		try {
			conn = dataSource.getConnection();
			if(sort.equals("")||sort==null)
			{
				sql=sql+" ORDER BY RULE_ID ASC";

			}
			else
			{
				sql=sql+"ORDER BY "+sort+" "+order;
			}
			PreparedStatement ps = conn.prepareStatement(sql);

			

			ps.setString(1, user_id);

			RuleDB rule = null;
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				rule = new RuleDB(
					rs.getString("RULE_ID"),
					rs.getString("USER_ID"),
					rs.getString("RULE_NAME"),
					rs.getString("RULE_DESCRIPTION"),
					rs.getString("RULE"),
					rs.getString("ORION_ID"),
					rs.getBoolean("BASIC")
				);
				
				rules.add(rule);
			}
			rs.close();
			ps.close();
			return rules;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {}
			}
		}
	}

	@Override
	public int delete(String rule_id, String user_id) {
		String sql = "DELETE FROM perseo_rules WHERE USER_ID = ? and RULE_ID = ?";

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, user_id);
			ps.setString(2, rule_id);
			int rs = ps.executeUpdate();
			ps.close();
			return rs;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {}
			}
		}
		
	}

	@Override
	public boolean existsRule(String rule_id, String user_id) {
		String sql = "SELECT * FROM perseo_rules WHERE RULE_ID = ? and USER_ID = ?";

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, rule_id);
			ps.setString(2, user_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				rs.close();
				ps.close();
				return true;
			}
			rs.close();
			ps.close();
			return false;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {}
			}
		}
	}

	@Override
	public String getSubscriptionId(String rule_id) {
			String sql = "SELECT ORION_ID FROM perseo_rules WHERE RULE_ID = ?";

			Connection conn = null;

			try {
				conn = dataSource.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, rule_id);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					String id = rs.getString("ORION_ID");
					rs.close();
					ps.close();
					return id;
				}
				rs.close();
				ps.close();
				return "";
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				if (conn != null) {
					try {
					conn.close();
					} catch (SQLException e) {}
				}
			}
	}
	
	@Override
	public void createTable() {
		Connection conn = null;
	    String sqlCreate = "CREATE TABLE IF NOT EXISTS perseo_rules"
	            + "( RULE_ID varchar(100) NOT NULL,"
	            + " USER_ID varchar(100) NOT NULL,"
	            + " RULE_NAME varchar(100),"
	            + " RULE_DESCRIPTION varchar(100),"
	            + " RULE varchar(5000) NOT NULL,"
	            + " ORION_ID varchar(100) NOT NULL,"
	            + "BASIC bool NOT NULL,"
	            + " PRIMARY KEY (RULE_ID))";
	    
		try {
			conn = dataSource.getConnection();
		    Statement stmt;
			stmt = conn.createStatement();
		    stmt.execute(sqlCreate);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {}
			}
		}
	}
	public void createTableBasic() {
		Connection conn = null;
	    String sqlCreate = "CREATE TABLE IF NOT EXISTS perseo_basic_rules"
	            + "( RULE_ID varchar(100) NOT NULL,"
	            + " RULE varchar(5000) NOT NULL,"
	            + " PRIMARY KEY (RULE_ID))";
	    
		try {
			conn = dataSource.getConnection();
		    Statement stmt;
			stmt = conn.createStatement();
		    stmt.execute(sqlCreate);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {}
			}
		}
	}
	public String getAdvancedRule(String rule_id) {
		String sql = "SELECT RULE, RULE_DESCRIPTION FROM perseo_rules WHERE RULE_ID = ?";
		JsonObject output = new JsonObject();
		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, rule_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				String rule = rs.getString("RULE");
				String description = rs.getString("RULE_DESCRIPTION");
				JsonParser parser = new JsonParser();
				JsonObject jo = (JsonObject) parser.parse(rule);
				output.add("rule", jo);
				output.addProperty("description", description);
				rs.close();
				ps.close();
				return output.toString();
			}
			rs.close();
			ps.close();
			JsonObject jo = new JsonObject();
			jo.addProperty("error", "rule doesn't exist");
			return jo.toString();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {}
			}
		}
	}
	
	
	public String getDescriptionOfARule(String rule_id)
	{
		JsonParser parser = new JsonParser();
		JsonObject jo=(JsonObject) parser.parse(getAdvancedRule(rule_id));
		return jo.get("description").getAsString();
	}
	
	
	public String getBasicRule(String rule_id) {
		String sql = "SELECT RULE FROM perseo_basic_rules WHERE RULE_ID = ?";

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, rule_id);
			ResultSet rs = ps.executeQuery();
			JsonObject jo = new JsonObject();
			if (rs.next()) {
				String rule = rs.getString("RULE");
				JsonParser parser = new JsonParser();
				JsonObject jo1 = (JsonObject)parser.parse(rule);
				String description = getDescriptionOfARule(rule_id);
				jo = new JsonObject();
				jo.add("rule", jo1);
				description.replaceAll("\"", "");
				jo.addProperty("description", description);
				rs.close();
				ps.close();
				return jo.toString();
			}
			jo.addProperty("error", "basic rule doesn't exist");
			rs.close();
			ps.close();
			String output=jo.toString();
			return output;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {}
			}
		}
	}
	public int deleteBasic(String rule_id) {
		String sql = "DELETE FROM perseo_basic_rules WHERE RULE_ID = ?";

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, rule_id);
			int rs = ps.executeUpdate();
			ps.close();
			return rs;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {}
			}
		}
		
	}
	public String insertBasic(RuleDB rule) {
		String sql = "INSERT INTO perseo_basic_rules" +
				"(RULE_ID, RULE) "
				+ "VALUES (?,?)";
		
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, rule.getRule_id());
			ps.setString(2, rule.getRule());
			ps.executeUpdate();
			ps.close();
			return "{\"201\":\"created\"}";
		} catch (SQLException e) {
			return "{\""+ e.getErrorCode() + "\":" + "\"" +e.getMessage()+ "\"}";
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}
	}
	public boolean existsBasicRule(String rule_id) {
		String sql = "SELECT * FROM perseo_basic_rules WHERE RULE_ID = ?";

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, rule_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				rs.close();
				ps.close();
				return true;
			}
			rs.close();
			ps.close();
			return false;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {}
			}
		}
	}
}
