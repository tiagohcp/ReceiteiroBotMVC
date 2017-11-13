
public class Favorite {
	private String id;
	private String title;

	public Favorite(String id, String title){
		this.id = id;
		this.title = title;
	}
	
	public String getID(){
		return id;
	}
	
	public String getTitle(){
		return title;
	}
	
	public String toString(){
		return "ID: " + this.id + "\n" + "Title: " + this.title;
	}
}
