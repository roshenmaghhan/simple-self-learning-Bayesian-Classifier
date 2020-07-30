package classifier;

import java.sql.*;
import java.util.*;

public class ClassifierDB {
	Connection c = null;
	Statement stmt = null;
	
	/*
	 * Constructor
	 */
	
	public ClassifierDB(){
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:trainingDB.db");
			this.stmt = c.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS trainingDB (label TEXT NOT NULL,category TEXT NOT NULL, PRIMARY KEY(label) );");
		} catch (Exception e) {
			System.out.println("Database Creation Error ! : " + e.getMessage());
		}
	}

	
	/*
	 * Returns the training data and category from the database, as a map 
	 */

	public HashMap<String, String> listData() {
		HashMap<String, String> dataSet = new HashMap<String,String>();
		try {
			this.stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM trainingDB");
			
			while(rs.next()) {
				String label = rs.getString("label");
				String category = rs.getString("category");
				dataSet.put(label, category);
			}
		} catch(Exception e) {
			System.out.println("List Data Error : " + e.getMessage());
		}
		return dataSet;
	}
	
	
	/*
	 * Closes the database connection
	 */

	public void closeConnection() {
		try {
			c.close();
		} catch (Exception e) {
			System.out.println("Close Connection Error : " + e.getMessage());
		}
	}
	

	/*
	 * Executes a query to the database 
	 */

	public void executeQuery(String query) {
		try {
			this.stmt = c.createStatement();
			stmt.executeUpdate(query);	
		} catch (Exception e) {
			System.out.println("Execute Query Error : " + e.getMessage());
		}
	}
	

	/*
	 * Inserts training data and category into the database 
	 */

	public void insertData(String data, String group) {
		try {
			String query = "INSERT INTO trainingDB(label, category) VALUES ('" + data  + "', '" + group + "')";
			this.stmt = c.createStatement();
			stmt.executeUpdate(query);	
		} catch (Exception e) {
			System.out.println("Execute Query Error : " + e.getMessage());
		}
	}
	

	/*
	 * Checks the database if the training data already exists 
	 */

	public boolean doesDataExist(String data, String category) {
		boolean queryExists = false;
		try {
			this.stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT category FROM trainingDB WHERE label='" + data + "';");
			queryExists = rs.next();
		} catch(Exception e) {
			System.out.println("Check Data Error : " + e.getMessage());
		}

		return queryExists;
	}
	

	/*
	 * Deletes the entire database 
	 */

	public void deleteDB() {
		try {
			String query = "DROP TABLE IF EXISTS trainingDB;";
			this.stmt = c.createStatement();
			stmt.executeUpdate(query);	
		} catch(Exception e) {
			System.out.println("Delete Database Error : " + e.getMessage());
		}
	}
}
