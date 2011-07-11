package tw.jouou.aRoundTable.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import tw.jouou.aRoundTable.util.DBUtils;
import android.content.ContentValues;

public class Project {
	private String name;
	private long id;
	private Date updateAt;
	
	public Project(long id, String name){
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
	
	public long getId(){
		return id;	
	}
	
	public Date getUpdateAt(){
		return updateAt;
	}
	
	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		values.put(DBUtils.FIELD_PROJECTS_NAME, name);
		return values;
	}
}
