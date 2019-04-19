import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

public class TestingServlet extends HttpServlet {

 
 public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
        
    String user = request.getParameter("user");
    // JDBC driver name and database URL
     String JDBC_DRIVER = "com.mysql.jdbc.Driver";
     String DB_URL="jdbc:mysql://localhost:3306/test";

    //  Database credentials
     String USER = "pyroj";
     String PASS = "pyroj123";

      // Set response content type
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      String title = "Database Result";
      
      String docType =
         "<!doctype html public \"-//w3c//dtd html 4.0 " + "transitional//en\">\n";
      
      out.println(docType +
         "<html>\n" +
         "<head><title>" + title + "</title></head>\n" +
         "<body bgcolor = \"#f0f0f0\">\n" +
         "<h1 align = \"center\">" + title + "</h1>\n");
      out.println(user);

      try {
         // Register JDBC driver
         Class.forName("com.mysql.jdbc.Driver");

         // Open a connection
         Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

         // Execute SQL query
         Statement stmt = conn.createStatement();
         String sql;
         sql = "SELECT id, first, last, age FROM Employees";
         ResultSet rs = stmt.executeQuery(sql);

         // Extract data from result set
         while(rs.next()){
            //Retrieve by column name
            int id  = rs.getInt("id");
            int age = rs.getInt("age");
            String first = rs.getString("first");
            String last = rs.getString("last");

            //Display values
            out.println("ID: " + id + "<br>");
            out.println(", Age: " + age + "<br>");
            out.println(", First: " + first + "<br>");
            out.println(", Last: " + last + "<br>");
         }
         out.println("</body></html>");

         // Clean-up environment
         rs.close();
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
          out.println("SQLException");
         se.printStackTrace(out);
      } catch(Exception e) {
         //Handle errors for Class.forName
          out.println("Exception");

         e.printStackTrace(out);

      }
   }
} 