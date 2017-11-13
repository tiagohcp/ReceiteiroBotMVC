import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.pengrad.telegrambot.model.Update;


public class Model implements Subject{
	
	private List<Observer> observers = new LinkedList<Observer>();
		
	private static Model uniqueInstance;
	
	ConnectionAPI connectionAPI;
	
	ConnectionDB connectionDB;
	
	private Model(){}
	
	public static Model getInstance(){
		if(uniqueInstance == null){
			uniqueInstance = new Model();
		}
		return uniqueInstance;
	}
	
	public void registerObserver(Observer observer){
		observers.add(observer);
	}
	
	public void notifyObservers(long chatId, String answerData){
		for(Observer observer:observers){
			observer.update(chatId, answerData);
		}
	}
	
	public void notifyObserversImage(long chatId, String imageData){
		for(Observer observer:observers){
			observer.updateImage(chatId, imageData);
		}
	}
	
	public void notifyObserversInline(Update update){
		for(Observer observer:observers){
			observer.inline(update);
		}
	}
	
	public void notifyObserversInlineFavorite(Update update, String id, String title){
		for(Observer observer:observers){
			observer.inlineFavorite(update, id, title);
		}
	}
	
	
	public void setConnetionAPI(ConnectionAPI connection){ //Strategy Pattern
		this.connectionAPI = connectionAPI;
	}
	
	public void setConnectionDB(ConnectionDB connection){ //Strategy Pattern
		this.connectionDB = connectionDB;
	}
	
	public int searchRecipe(Update update){
				
		setConnetionAPI(new ConnectionAPI());

		String text  = update.message().text().replaceAll(" ", "+");
		List<Recipe> finalResult = this.connectionAPI.getDataRecipe(text);
		
		int cont = 0;
		
		if(finalResult.size() != 0){
			
			for(Recipe result:finalResult){				
				if(cont<10){
					this.notifyObservers(update.message().chat().id(), result.toString());
					this.notifyObserversImage(update.message().chat().id(), result.getImage());
				}
				else{
					break;
				}
				cont++;
			}
			
			this.notifyObserversInline(update);
			

			
		} else {
			this.notifyObservers(update.message().chat().id(), "Recipes not found for this ingredient");
		}
		
		return cont;		
	}
	
	public int searchRecipePrev(Update update, int pos, String text){
		
		setConnetionAPI(new ConnectionAPI());

		List<Recipe> finalResult = this.connectionAPI.getDataRecipe(text);

		if(finalResult.size() != 0){
			int cont = 0;
			for(Recipe result:finalResult){
				if((cont>=pos-20) && (cont<pos-10)){
					this.notifyObservers(update.callbackQuery().message().chat().id(), result.toString());
					this.notifyObserversImage(update.callbackQuery().message().chat().id(), result.getImage());
				}
				else if(cont == pos-10){
					break;
				}
				cont++;
			}
						
			pos = cont;
			
		} else {
			this.notifyObservers(update.message().chat().id(), "Recipes not found for this ingredient");
		}
		
		return pos;		
	}
	
	public int searchRecipeNext(Update update, int pos, String text){
	
		setConnetionAPI(new ConnectionAPI());

		List<Recipe> finalResult = this.connectionAPI.getDataRecipe(text);

		if(finalResult.size() != 0){
			int cont = 0;
			for(Recipe result:finalResult){	
				if((cont>=pos) && (cont<pos+10)){
					this.notifyObservers(update.callbackQuery().message().chat().id(), result.toString());
					this.notifyObserversImage(update.callbackQuery().message().chat().id(), result.getImage());
				}
				else if(cont == pos+10){
					break;
				}
				cont++;
			}
			
			pos = cont;
			
		} else {
			this.notifyObservers(update.message().chat().id(), "Recipes not found for this ingredient");
		}
		
		return pos;		
	}
	
	public void showFavorites(Update update) throws ClassNotFoundException, FileNotFoundException, IOException, SQLException{
		setConnectionDB(new ConnectionDB());
		System.out.println(this.connectionDB);
		List<Favorite> finalResult = this.connectionDB.showFavorite(update.message().from().id());
		
		if(finalResult.size() != 0){
			this.notifyObservers(update.message().chat().id(), "FAVORITOS:");
			for(Favorite result:finalResult){
				this.notifyObservers(update.message().chat().id(), result.toString());
			}
		} else {
			this.notifyObservers(update.message().chat().id(), "You don't have favorites.");
		}
	}
	
	public void searchIngredient(Update update){
		System.out.println(update);
		
		setConnetionAPI(new ConnectionAPI());
		System.out.println(this.connectionAPI);
		String text  = update.message().text();
		String id = "", title = "";
		List<Ingredient> finalResult = this.connectionAPI.getDataIngredient(text);
		if(finalResult.size() != 0){
			for(Ingredient result:finalResult){
				this.notifyObservers(update.message().chat().id(), result.toString());
				this.notifyObserversImage(update.message().chat().id(), result.getImage());
				id = result.getID();
				title = result.getTitle();
			}
			
			this.notifyObserversInlineFavorite(update, id, title);
			
		} else {
			this.notifyObservers(update.message().chat().id(), "Recipe not found");
		}
		
	}


}
