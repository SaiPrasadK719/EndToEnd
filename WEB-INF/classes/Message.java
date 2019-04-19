import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * Servlet implementation class show_contacts
 */

public class Message extends HttpServlet {
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
           String group_name = request.getParameter("group_name");
           if (group_name==null){
            out.println("-1");
           }
           else{
             String sql;
             PreparedStatement stmt;
             
             sql = "SELECT setup_message, state, creator FROM `groups` WHERE `group_name` = ?";;
             stmt = conn.prepareStatement(sql);
             stmt.setString(1,group_name);
             ResultSet result = stmt.executeQuery();
             out.println("{");
             int i;
             while (result.next()) {
               String setup_message = result.getString("setup_message");
               String state = result.getString("state");
               String creator = result.getString("creator");
               out.println("\"setup_message\":\"" + setup_message + "\",\n\"state\":\"" + state + "\",\n\"creator\":\"" + creator + "\",");
             }
             
             stmt.close();  


             sql = "SELECT username FROM `group_users` WHERE `group_name` = ?";;
             stmt = conn.prepareStatement(sql);
             stmt.setString(1,group_name);
              result = stmt.executeQuery();
             out.print(" \"users\":[");
             i=0;
             while (result.next()) {
               String username = result.getString("username");
               if (i>0){
                out.print(",");
               }
               i=1;
               out.print("\""+username+"\"");
             }
             out.println("],");
             stmt.close();  

            sql = "SELECT sender,send_time,update_key_message,cipher_text,state FROM `messages` WHERE `group_name` = ? ORDER BY message_id ASC";
             stmt = conn.prepareStatement(sql);
             stmt.setString(1,group_name);
              result = stmt.executeQuery();
             out.println(" \"messages\":[");
             i=0;
             while (result.next()) {
               String sender = result.getString("sender");
               String send_time = result.getString("send_time");
               String update_key_message = result.getString("update_key_message");
               String cipher_text = result.getString("cipher_text");
               String state = result.getString("state");

               if (i>0){
                out.print(",");
               }
               i=1;
               out.println("{\"sender\":\""+sender + "\",\"send_time\":\"" + send_time + "\",\"update_key_message\":\"" + update_key_message + "\",\"cipher_text\":\"" + cipher_text +"\",\"state\":\"" + state + "\"}");
               
             }
             out.println("]}");
            stmt.close();  
            


            }
           
           
           
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