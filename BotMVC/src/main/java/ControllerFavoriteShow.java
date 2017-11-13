import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import com.pengrad.telegrambot.model.Update;

public class ControllerFavoriteShow implements ControllerFavorite{
	private Model model;
	private View view;
	
	public ControllerFavoriteShow(Model model, View view){
		this.model = model; //connection Controller -> Model
		this.view = view; //connection Controller -> View
	}
	
	public void favorite(Update update) throws ClassNotFoundException, FileNotFoundException, IOException, SQLException{
		view.sendTypingMessage(update);
		model.showFavorites(update);
	}
	
}
