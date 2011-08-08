package tw.jouou.aRoundTable.bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import tw.jouou.aRoundTable.util.DBUtils;
import android.content.ContentValues;

public class Project implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String name;
	private long id;
	private long serverId = 0;
	private int color;
	private Date updateAt;
	
	public Project(long id, String name, int color) {
		this.id = id;
		this.name = name;
		this.color = color;
	}
	
	public Project(long id, String name, long serverId, int color) {
		this.id = id;
		this.name = name;
		this.serverId = serverId;
		this.color = color;
	}
	
	public Project(String name, int color){
		this.name = name;
		this.color = color;
	}
	
	public Project(JSONObject projectJson) throws JSONException, ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.name = projectJson.getString("name");
		this.id = projectJson.getInt("id");
		this.updateAt = sdf.parse(projectJson.getString("updated_at"));
	}
	
	public String getName() {
		return name;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;	
	}
	
	public long getServerId() {
		return serverId;
	}
	
	public void setServerId(long serverId) {
		this.serverId = serverId;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public Date getUpdateAt() {
		return updateAt;
	}
	
	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		values.put(DBUtils.FIELD_PROJECTS_NAME, name);
		values.put(DBUtils.FIELD_PROJECTS_SERVERID, serverId);
		values.put(DBUtils.FIELD_PROJECTS_COLOR, color);
		return values;
	}
}
