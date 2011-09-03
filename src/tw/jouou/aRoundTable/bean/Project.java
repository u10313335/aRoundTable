package tw.jouou.aRoundTable.bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import tw.jouou.aRoundTable.util.DBUtils;
import android.content.ContentValues;

public class Project implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String name;
	private long id;
	private long serverId;
	private int color;
	private Date updateAt;
	
	public Project(long id, String name, int color, Date updateAt) {
		this.id = id;
		this.name = name;
		this.color = color;
		this.updateAt = updateAt;
	}
	
	public Project(long id, String name, long serverId, int color, Date updateAt) {
		this.id = id;
		this.name = name;
		this.serverId = serverId;
		this.color = color;
		this.updateAt = updateAt;
	}
	
	public Project(long id, String name, long serverId, int color) {
		this.id = id;
		this.name = name;
		this.serverId = serverId;
		this.color = color;
	}
	
	public Project(String name, long serverId, int color, Date updateAt){
		this.serverId = serverId;
		this.name = name;
		this.color = color;
		this.updateAt = updateAt;
	}
	
	public Project(JSONObject projectJson) throws JSONException {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			this.name = projectJson.getString("name");
			this.serverId = projectJson.getLong("id");
			this.updateAt = formatter.parse(projectJson.getString("updated_at"));
			this.color = Integer.parseInt(projectJson.getString("color"));
		} catch (ParseException e) {
			System.out.println("Parse error");
		}
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
	
	public void setUpdateAt(Date updateAt) {
		this.updateAt = updateAt;
	}
	
	public Date getUpdateAt() {
		return updateAt;
	}
	
	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		values.put(DBUtils.FIELD_PROJECT_NAME, name);
		values.put(DBUtils.FIELD_PROJECT_SERVERID, serverId);
		values.put(DBUtils.FIELD_PROJECT_COLOR, color);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		values.put(DBUtils.FIELD_PROJECT_UPDATED_AT, sdf.format(updateAt));
		return values;
	}
}
