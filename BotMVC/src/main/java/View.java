import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;


public class View implements Observer{

	
	TelegramBot bot = TelegramBotAdapter.build("303634900:AAHrdAURD9WM1HC3TzxzEi3jsBiTQabMgFQ");

	//Object that receives messages
	GetUpdatesResponse updatesResponse;
	//Object that send responses
	SendResponse sendResponse;
	//Object that manage chat actions like "typing action"
	BaseResponse baseResponse;
			
	int pos = 0;
	
	String text;
	
	String id;
	
	String title;
	
	int queuesIndex=0;
	
	ControllerSearch controllerSearch; //Strategy Pattern -- connection View -> Controller
	
	ControllerFavorite controllerFavorite; //Strategy Pattern -- connection View -> Controller
	
	boolean searchBehaviour = false;
;
	
	private Model model;
	
	public View(Model model){
		this.model = model; 
	}
	
	public void setControllerSearch(ControllerSearch controllerSearch){ //Strategy Pattern
		this.controllerSearch = controllerSearch;
	}
	
	public void setControllerFavorite(ControllerFavorite controllerFavorite){ //Strategy Pattern
		this.controllerFavorite = controllerFavorite;
	}
		
	public void receiveUsersMessages() throws ClassNotFoundException, FileNotFoundException, SQLException, IOException {
		
		//infinity loop
		while (true){
		
			//taking the Queue of Messages
			updatesResponse =  bot.execute(new GetUpdates().limit(100).offset(queuesIndex));
			
			//Queue of messages
			List<Update> updates = updatesResponse.updates();

			
			//taking each message in the Queue
			for (Update update : updates) {
				
				if(update.callbackQuery() != null){
					
					queuesIndex = update.updateId()+1;
					
					if(this.searchBehaviour==true){
						this.callController(update);
					}
					

					this.incomingMessageInline(update);
				}
				
				else{
					//updating queue's index
					queuesIndex = update.updateId()+1;
					
					if(this.searchBehaviour==true){
						this.callController(update);
						
					}else if(update.message().text().toLowerCase().equals("recipe")){
						setControllerSearch(new ControllerSearchIngredient(model, this));
						sendResponse = bot.execute(new SendMessage(update.message().chat().id(),"what's the ID number?"));
						//sendResponse = bot.execute(new SendPhoto(update.message().chat().id(),"http://static.food2fork.com/ArtichokeBread1500a7ce3ccd.jpg"));
						this.searchBehaviour = true;
						
					} else if(update.message().text().toLowerCase().equals("ingredient")){
						setControllerSearch(new ControllerSearchRecipe(model, this));
						sendResponse = bot.execute(new SendMessage(update.message().chat().id(),"what's the ingredient name?"));
						this.searchBehaviour = true;
					
					} else if(update.message().text().toLowerCase().equals("favorites")){
						setControllerFavorite(new ControllerFavoriteShow(model, this));
						//sendResponse = bot.execute(new SendMessage(update.message().chat().id(),"what's the ingredient name?"));
						this.callControllerDB(update);
						System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEE");
					} else {
						sendResponse = bot.execute(new SendMessage(update.message().chat().id(),"Type recipe, ingredient or favorites:"));
					}
				}
				
				
			}

		}
		
		
	}
	
	
	public void callController(Update update){
		this.controllerSearch.search(update);
	}
	
	public void callControllerDB(Update update) throws ClassNotFoundException, FileNotFoundException, IOException, SQLException{
		this.controllerFavorite.favorite(update);
	}
	
	public void update(long chatId, String answerData){
		sendResponse = bot.execute(new SendMessage(chatId, answerData));
		this.searchBehaviour = false;
	}
	
	public void updateImage(long chatId, String answerData){
		sendResponse = bot.execute(new SendPhoto(chatId, answerData));
		this.searchBehaviour = false;
	}
	
	public void inline(Update update){
		InlineKeyboardButton inlineKey1 = new InlineKeyboardButton("Previous").callbackData("1");
	    InlineKeyboardButton inlineKey2 = new InlineKeyboardButton("Next").callbackData("2");
	    InlineKeyboardButton[] inlineKeyraw = { inlineKey1, inlineKey2 };
	    InlineKeyboardMarkup inlineKeyboards = new InlineKeyboardMarkup(inlineKeyraw);
	    this.text = update.message().text().replaceAll(" ", "+");
	    System.out.println(update);
	    SendMessage editMessageText = new SendMessage(update.message().chat().id(), "Enter 'recipe' to choose the desired recipe or choose an option:").parseMode(ParseMode.HTML).disableWebPagePreview(false).replyMarkup(inlineKeyboards);
	    System.out.println(editMessageText);
	    bot.execute(editMessageText);
	    this.searchBehaviour = false;
	}

	public void inlineFavorite(Update update, String id, String title){
		InlineKeyboardButton inlineKey3 = new InlineKeyboardButton("Favorite").callbackData("3");
		InlineKeyboardButton inlineKey4 = new InlineKeyboardButton("Remove").callbackData("4");
	    InlineKeyboardButton[] inlineKeyraw = { inlineKey3, inlineKey4 }; //, 
	    InlineKeyboardMarkup inlineKeyboards = new InlineKeyboardMarkup(inlineKeyraw);
	    this.id = id;
	    this.title = title;
	    System.out.println(update);
	    SendMessage editMessageText = new SendMessage(update.message().chat().id(), "Choose an option to update your favorites:").parseMode(ParseMode.HTML).disableWebPagePreview(false).replyMarkup(inlineKeyboards);
	    System.out.println(editMessageText);
	    bot.execute(editMessageText);
	    this.searchBehaviour = false;
	}
	
	public void incomingMessageInline(Update update) throws SQLException, ClassNotFoundException, FileNotFoundException, IOException {
		int ini = this.pos;
		if (update.callbackQuery().data().equals("1")) {
			this.pos = model.searchRecipePrev(update, this.pos, this.text);
			
			if(this.pos != 0){
				InlineKeyboardButton inlineKey1 = new InlineKeyboardButton("Previous").callbackData("1");
			    InlineKeyboardButton inlineKey2 = new InlineKeyboardButton("Next").callbackData("2");
			    InlineKeyboardButton[] inlineKeyraw = { inlineKey1, inlineKey2 };
			    InlineKeyboardMarkup inlineKeyboards = new InlineKeyboardMarkup(inlineKeyraw);
			    
			    SendMessage editMessageText = new SendMessage(update.callbackQuery().message().chat().id(), "Enter 'recipe' to choose the desired recipe or choose an option:").parseMode(ParseMode.HTML).disableWebPagePreview(true).replyMarkup(inlineKeyboards);
				bot.execute(editMessageText);
				//this.sendTypingMessage(update);
			}
			else{
				this.pos = 10;
			}
		} else if (update.callbackQuery().data().equals("2")) {
			this.pos = model.searchRecipeNext(update, this.pos, this.text);
			
			if(ini != this.pos){
				InlineKeyboardButton inlineKey1 = new InlineKeyboardButton("Previous").callbackData("1");
			    InlineKeyboardButton inlineKey2 = new InlineKeyboardButton("Next").callbackData("2");
			    InlineKeyboardButton[] inlineKeyraw = { inlineKey1, inlineKey2 };
			    InlineKeyboardMarkup inlineKeyboards = new InlineKeyboardMarkup(inlineKeyraw);
			    
			    SendMessage editMessageText = new SendMessage(update.callbackQuery().message().chat().id(), "Enter 'recipe' to choose the desired recipe or choose an option:").parseMode(ParseMode.HTML).disableWebPagePreview(true).replyMarkup(inlineKeyboards);
				bot.execute(editMessageText);
				//this.sendTypingMessage(update);
			}
		} else if (update.callbackQuery().data().equals("3")) {
			ConnectionDB connectionDB = new ConnectionDB();
			//connectionDB.open();
			System.out.println(update.callbackQuery().message().text());
			connectionDB.saveFavorite(update, this.id, this.title);
			//connectionDB.close();
			
			bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()).text("Recipe Saved in Favorites!").showAlert(true).cacheTime(1));
			
			/*
			InlineKeyboardButton inlineKey4 = new InlineKeyboardButton("Remove").callbackData("4");
			InlineKeyboardButton[] inlineKeyraw = { inlineKey4 };
			InlineKeyboardMarkup inlineKeyboards = new InlineKeyboardMarkup(inlineKeyraw);
			    
			SendMessage editMessageText = new SendMessage(update.callbackQuery().message().chat().id(), "Click 'Remove' to remove this recipe of your favorites:").parseMode(ParseMode.HTML).disableWebPagePreview(true).replyMarkup(inlineKeyboards);
			bot.execute(editMessageText);
			*/
		} else if (update.callbackQuery().data().equals("4")) {
			ConnectionDB connectionDB = new ConnectionDB();
			//connectionDB.open();
			System.out.println(update.callbackQuery().message().text());
			connectionDB.deleteFavorite(update, this.id);
			//connectionDB.close();
			
			bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()).text("Recipe Removed From Favorites!").showAlert(true).cacheTime(1));
			
		}
	}
	
	public void sendTypingMessage(Update update){
		baseResponse = bot.execute(new SendChatAction(update.message().chat().id(), ChatAction.typing.name()));
	}
	
	
	
	

}
