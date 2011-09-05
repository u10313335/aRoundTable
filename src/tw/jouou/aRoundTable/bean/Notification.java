package tw.jouou.aRoundTable.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import tw.jouou.aRoundTable.util.DBUtils;
import android.content.ContentValues;

public class Notification implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private long id;
	private long serverId;
	private long userId;
	private String message;
	private boolean read;
	
	public Notification(long id, long userId, long serverId, String message, boolean read) {
		this.id = id;
		this.userId = userId;
		this.serverId = serverId;
		this.message = message;
		this.read = read;
	}
	
	public Notification(long userId, long serverId, String message, boolean read) {
		this.userId = userId;
		this.serverId = serverId;
		this.message = message;
		this.read = read;
	}
	
	public Notification(JSONObject notificationJson) throws JSONException {
		this.message = notificationJson.getString("message");
		this.serverId = notificationJson.getLong("id");
		this.userId = notificationJson.getLong("user_id");
	}
	
	public long getId(){
		return id;	
	}
	
	public void setId(long id) {
		this.id = id;
	}
		
	public long getServerId() {
		return serverId;
	}
	
	public void setServerId(long serverId) {
		this.serverId = serverId;
	}
	
	public long getUserId(){
		return userId;	
	}
	
	public String getMessage() {
		return message;
	}
	
	public boolean getRead() {
		return read;
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		values.put(DBUtils.FIELD_NOTIFICATION_MESSAGE, message);
		values.put(DBUtils.FIELD_NOTIFICATION_MEMBERID, 1);
		values.put(DBUtils.FIELD_NOTIFICATION_SERVERID, serverId);
		values.put(DBUtils.FIELD_NOTIFICATION_READ, (read)? "1" : "0");
		return values;
	}
}