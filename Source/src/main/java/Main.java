import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public class Main {

	private static Model model;
	
	public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, SQLException, IOException {

		model = Model.getInstance();
		//initializeModel(model);
		View view = new View(model);
		model.registerObserver(view); //connection Model -> View
		view.receiveUsersMessages();

	}
	
	

}