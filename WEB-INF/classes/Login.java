import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

public class Login extends HttpServlet {
 public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      doPost(request,response);
      
      }
 public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

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

         String user = request.getParameter("username");
         //sql = "INSERT INTO `users` (username,public_ikey,public_ekey) VALUES (?,?,?)";
         sql = "SELECT uid FROM `users` WHERE username=?";
         PreparedStatement stmt = conn.prepareStatement(sql);
         if (user != null) {
        	 stmt.setString(1, user);
        	 ResultSet rs = stmt.executeQuery();
        	 if(!rs.next()) {
        		 out.println("-1\nLogin Failed");
        	 }
        	 else {
        		 out.println(rs.getInt("uid"));
        	 }
        	 
         }
	         









         
         stmt.close();
         conn.close();
          try {
              if(stmt!=null)
                  stmt.close();
          } catch(SQLException se2) {
              se2.printStackTrace(out);
          } // nothing we can do
          try {
              if(conn!=null)
                  conn.close();
          } catch(SQLException se) {
              se.printStackTrace(out);
          } //end finally try

      } catch(SQLException se) {
         //Handle errors for JDBC
          
         se.printStackTrace(out);
      } catch(Exception e) {
         //Handle errors for Class.forName
         e.printStackTrace(out);

      }
   }
} 