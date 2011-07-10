package tw.jouou.aRoundTable.bean;

public class Project {
	private String name;
	private int id;
	
	public Project(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public int getId(){
		return id;	
	}
}
