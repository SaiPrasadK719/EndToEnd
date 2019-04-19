import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SendMessage extends HttpServlet {
 
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
        String group_name = request.getParameter("group_name");
        String sender = request.getParameter("sender");
        String update_key_message = request.getParameter("update_key_message");
        String cipher_text = request.getParameter("cipher_text");
        String state = request.getParameter("state");
        
      sql = "INSERT INTO `messages` (group_name,sender,update_key_message,cipher_text,state) VALUES (?,?,?,?,?)";

         PreparedStatement stmt = conn.prepareStatement(sql);
         if (group_name!=null && sender!=null && update_key_message!=null  && cipher_text!=null && state!=null) {
           stmt.setString(1,group_name);
           stmt.setString(2,sender);
           stmt.setString(3,update_key_message);
           stmt.setString(4,cipher_text);
           stmt.setString(5,state);
           stmt.executeUpdate();
           out.println("1");
        }

         conn.close();
          try {
              if(stmt!=null)
                  stmt.close();
          } catch(SQLException se2) {
              //se2.printStackTrace(out);
              out.println("-1\n"+se2.getMessage());
          } // nothing we can do
          
          try {
              if(conn!=null)
                  conn.close();
          } catch(SQLException se) {
              //se.printStackTrace(out);
            out.println("-1\n"+se.getMessage());
          } //end finally try

      } catch(SQLException se) {
         //Handle errors for JDBC
          
         //se.printStackTrace(out);
        out.println("-1\n"+se.getMessage());
      } catch(Exception e) {
         //Handle errors for Class.forName
         e.printStackTrace(out);

      }
   }
} 
