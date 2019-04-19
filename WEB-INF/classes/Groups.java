import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * Servlet implementation class show_contacts
 */

public class Groups extends HttpServlet {
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
           String username = request.getParameter("username");
           if (username==null){
            out.println("-1");
           }
           else{
             String sql;
             //String user = request.getParameter("username");
             sql = "SELECT `groups`.group_name, setup_message, state FROM `groups` JOIN `group_users` ON `groups`.group_name = `group_users`.group_name WHERE `group_users`.username=?";
             PreparedStatement stmt = conn.prepareStatement(sql);
             stmt.setString(1,username);
             ResultSet result = stmt.executeQuery();
             
             //JsonArray result_list_json = Json.createArrayBuilder().build();
             out.println("{ \"groups\":[");
             int i=0;
             while (result.next()) {
               String group_name = result.getString("group_name");
               String setup_message = result.getString("setup_message");
               String state = result.getString("state");
               
               //JsonObject result_obj_json = Json.createObjectBuilder().add("user", user).add("public_ikey", public_ikey).add("public_ekey", public_ekey).build();
               
               //result_list_json.add(result_obj_json);
               if (i>0){
                out.println(",");
               }
               i++;
               out.print("{\"group_name\":\""+group_name + "\",\"setup_message\":\"" + setup_message + "\",\"state\":\"" + state + "\"}");
               
             }
             out.println("]}");
              stmt.close();  
            }
           //out.println(result_list_json);
           
           
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