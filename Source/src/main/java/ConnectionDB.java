import java.io.*;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import com.pengrad.telegrambot.model.Update;

public class ConnectionDB {
	static Connection db;  // Conexão com o servidor de banco de dados
	static Statement st;   // Declaração para executar os comandos

    
    public static List<Favorite> showFavorite(int id) throws ClassNotFoundException, FileNotFoundException, IOException, SQLException, NullPointerException
    {
    	String url = "jdbc:oracle:thin:@localhost:1521:xe";
		String usr = "SYSTEM";
		String pwd = "system";
		
		// Carregar o driver
		Class.forName("oracle.jdbc.driver.OracleDriver");
		
		// Conectar com o servidor de banco de dados
		db = DriverManager.getConnection(url, usr, pwd);
		
		st = db.createStatement();

    	
    	ResultSet rs = st.executeQuery("select * from FAVORITE where ID_USER = "+id+"");
        
    	List<Favorite> favorites = new LinkedList<Favorite>();
    	
    	if (rs != null)
        {
            while (rs.next())
            {
                String recipe_id = rs.getString(2);
                String recipe_title = rs.getString(3);
                Favorite favorite = new Favorite(recipe_id, recipe_title);
                favorites.add(favorite);
            }
            rs.close(); 
        }
    	
    	st.close();
        db.close();

    	return favorites;    	
    }
    
    public void saveFavorite(Update update, String recipe_id, String recipe_name) throws ClassNotFoundException, FileNotFoundException, IOException, SQLException
    {
    	String url = "jdbc:oracle:thin:@localhost:1521:xe";
		String usr = "SYSTEM";
		String pwd = "system";
		
		// Carregar o driver
		Class.forName("oracle.jdbc.driver.OracleDriver");
		
		// Conectar com o servidor de banco de dados
		db = DriverManager.getConnection(url, usr, pwd);
		
		st = db.createStatement();
    	int id = update.callbackQuery().from().id();
    	String name = update.callbackQuery().from().firstName() + " " + update.callbackQuery().from().lastName();
    	
    	ResultSet rs = st.executeQuery("select * from BOT_USER where ID_USER = "+id+"");
            	
    	if (rs.next() != false){

        }
        else{
	    	PreparedStatement ps = db.prepareStatement("insert into BOT_USER values (?,?)");
	    	ps.setInt(1, id);     
	    	ps.setString(2, name);     
	    	ps.executeUpdate();  
	    	ps.close(); 
        }

    	ResultSet rsF = st.executeQuery("select * from FAVORITE where ID_REC_FAV = "+recipe_id+" and ID_USER = "+id+"");
    	
        if (rsF.next() != false)
        {
        	
        }
        else{
	    	PreparedStatement psF = db.prepareStatement("insert into FAVORITE values (?,?,?)");
	    	psF.setInt(1, id);    
	    	psF.setString(2, recipe_id);     
	    	psF.setString(3, recipe_name);
	    	psF.executeUpdate();  
	    	psF.close();
        }
        	
    	st.close();
        db.close();
    }
    
    public void deleteFavorite(Update update, String recipe_id) throws ClassNotFoundException, FileNotFoundException, IOException, SQLException
    {
    	String url = "jdbc:oracle:thin:@localhost:1521:xe";
		String usr = "SYSTEM";
		String pwd = "system";
		
		int id = update.callbackQuery().from().id();
		
		// Carregar o driver
		Class.forName("oracle.jdbc.driver.OracleDriver");
		
		// Conectar com o servidor de banco de dados
		db = DriverManager.getConnection(url, usr, pwd);
		
		st = db.createStatement();
    	
		st.executeUpdate("delete from FAVORITE where ID_REC_FAV = "+recipe_id+" AND ID_USER ="+id+"");
    	
    	st.close();
        db.close();
    }
    
   
}
