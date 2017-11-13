import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import com.pengrad.telegrambot.model.Update;

public interface ControllerFavorite {
	
	public void favorite(Update update)throws ClassNotFoundException, FileNotFoundException, IOException, SQLException;

}
