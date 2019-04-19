import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateGroup extends HttpServlet {
 
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
         
         String sql,sql2;


        String group_name = request.getParameter("group_name");
        String creator = request.getParameter("creator");
        String setup_message = request.getParameter("setup_message");
        String state = request.getParameter("state");
        String users = request.getParameter("users");
        JSONArray jsonarray = new JSONArray(users);
        

        sql2 = "INSERT INTO `group_users` (`group_name`,`username`) VALUES ('"+group_name + "','"+creator+"')";
                      for(Object o: jsonarray){
                          if ( o instanceof String ) {
                            sql2 = sql2 + ", ('" + group_name+ "','" + (String) o + "')";
                        }
                      }
      sql = "INSERT INTO `groups` (group_name,creator,setup_message,state) VALUES (?,?,?,?)";

         PreparedStatement stmt = conn.prepareStatement(sql);
         PreparedStatement stmt2 = conn.prepareStatement(sql2);
         if (group_name!=null && creator!=null && setup_message!=null  && state!=null) {
           stmt.setString(1,group_name);
           stmt.setString(2,creator);
           stmt.setString(3,setup_message);
           stmt.setString(4,state);
           stmt.executeUpdate();
           stmt2.executeUpdate();
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
              if(stmt2!=null)
                  stmt2.close();
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
