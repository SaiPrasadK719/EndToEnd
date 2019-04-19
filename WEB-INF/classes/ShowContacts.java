import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * Servlet implementation class show_contacts
 */

public class ShowContacts extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// JDBC driver name and database URL
	     String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	     String DB_URL="jdbc:mysql://localhost:3306/test";
	    //  Database credentials
	     String USER = "pyroj";
	     String PASS = "pyroj123";
	      // Set response content type
	     response.setContentType("text/html");
	     PrintWriter out = response.getWriter();
	     
	     try {
	         // Register JDBC driver
	         Class.forName("com.mysql.jdbc.Driver");
	         // Open a connection
	         Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         // Execute SQL query
	         
	         String sql;
	         //String user = request.getParameter("username");
	         sql = "SELECT username, public_ikey, public_ekey FROM `users`";
	         PreparedStatement stmt = conn.prepareStatement(sql);
	         
	         ResultSet result = stmt.executeQuery();
	         
	         //JsonArray result_list_json = Json.createArrayBuilder().build();
	         out.println("{ \"users\":[");
	         int i=0;
	         while (result.next()) {
	        	 String user = result.getString("username");
	        	 String public_ikey = result.getString("public_ikey");
	        	 String public_ekey = result.getString("public_ekey");
	        	 
	        	 //JsonObject result_obj_json = Json.createObjectBuilder().add("user", user).add("public_ikey", public_ikey).add("public_ekey", public_ekey).build();
	        	 
	        	 //result_list_json.add(result_obj_json);
	        	 if (i>0){
	        	 	out.println(",");
	        	 }
	        	 i++;
	        	 out.print("{\"username\":\""+user + "\",\"public_ikey\":\"" + public_ikey + "\",\"public_ekey\":\"" + public_ekey + "\"}");
	        	 
	         }
	         out.println("]}");
	         
	         
	         //out.println(result_list_json);
	         
	         stmt.close();
	         conn.close();
	         
	     } catch (Exception e) {
	    	 e.printStackTrace(out);
	     }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}