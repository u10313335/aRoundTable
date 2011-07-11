package tw.jouou.aRoundTable.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class Project {
	private String name;
	private int id;
	private Date updateAt;
	
	public Project(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public Project(JSONObject projectJson) throws JSONException, ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.name = projectJson.getString("name");
		this.id = projectJson.getInt("id");		
		this.updateAt = sdf.parse(projectJson.getString("updated_at"));
	}
	
	public String getName(){
		return name;
	}
	
	public int getId(){
		return id;	
	}
	
	public Date getUpdateAt(){
		return updateAt;
	}
}
