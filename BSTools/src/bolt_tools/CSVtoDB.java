package bolt_tools;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.text.StringEscapeUtils;

public class CSVtoDB
{
	private String targetFile, objectName, JDBC_CONNECTION_URL;
	
	public String getJDBC_CONNECTION_URL() {return JDBC_CONNECTION_URL;}
	public void setJDBC_CONNECTION_URL(String url) {JDBC_CONNECTION_URL = url;}
	
	public String getTargetFile() { return targetFile; }
	public void setTargetFile(String targetFile) { this.targetFile = targetFile; }
	
	public String getObjectName() { return objectName; }
	public void setObjectName(String objectName) { this.objectName = objectName; }
	
	//static String INTERNAL_CONNECTION_URL = StringEscapeUtils.escapeJava("jdbc:sqlserver://DESKTOP-0P1SU82\\SQLEXPRESS:1433;DatabaseName=KA_DB");
	static String INTERNAL_CONNECTION_URL = StringEscapeUtils.escapeJava("jdbc:sqlserver://192.168.0.2\\SQLEXPRESS:1433;DatabaseName=KA_DB");
	
	public static void runCSVtoDB(String JDBC_CONNECTION_URL, File targetFile) throws IOException
	{	
		try
		{
//			Connection conn = null;
//			try 
//			{
//				//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//				conn = DriverManager.getConnection(INTERNAL_CONNECTION_URL);
//				//conn = DriverManager.getConnection("jdbc:sqlserver://DESKTOP-0P1SU82","","");
//			} 
			
			String connectionUrl = "jdbc:sqlserver://DESKTOP-0P1SU82\\SQLEXPRESS:1433;databaseName=KA_DB;";//integratedSecurity=true;";
			//String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=KA_DB;";//integratedSecurity=true;";
		
			try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
		            String SQL = "SELECT TOP 10 * FROM dbo.SVMX_SVMXC__Installed_Product__c_V";
		            ResultSet rs = stmt.executeQuery(SQL);

		            // Iterate through the data in the result set and display it.
		            while (rs.next()) {
		                System.out.println(rs.getString("Name") + " " + rs.getString("KA_AX_Project__c"));
		            }
		        }
		        // Handle any errors that may have occurred.
		        catch (SQLException e) {
		            e.printStackTrace();
		        }
			
//			catch(ClassNotFoundException e) 
//			{
//				e.printStackTrace();
//			} 
//			catch(SQLException e) 
//			{
//				e.printStackTrace();
//			}
//			CSVLoader loader = new CSVLoader(conn);
//			loader.loadCSV(targetFile, true);
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}
}