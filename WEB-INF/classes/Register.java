import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

public class Register extends HttpServlet {
 

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
         String public_ikey = request.getParameter("public_ikey");
         String public_ekey = request.getParameter("public_ekey");
         sql = "INSERT INTO `users` (username,public_ikey,public_ekey) VALUES (?,?,?)";
         PreparedStatement stmt = conn.prepareStatement(sql);
         if (user!=null && public_ekey!=null && public_ikey!=null) {

           stmt.setString(1,user);
           stmt.setString(2,public_ikey);
           stmt.setString(3,public_ekey);
           stmt.executeUpdate();

           out.println("1");
        }



         
         stmt.close();
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
