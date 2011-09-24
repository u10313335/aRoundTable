package tw.jouou.aRoundTable.bean;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public class Notification{
	public static final String COLUMN_READ = "read";
	
	@DatabaseField(generatedId = true)
	public long id;
	
	@DatabaseField(columnName = "server_id")
	public long serverId;
	
	@DatabaseField(columnName = "user_id")
	public long userId;
	
	@DatabaseField
	public String message;
	
	@DatabaseField
	public boolean read;
	
	public Notification(){
	}
	
	public Notification(JSONObject notificationJson) throws JSONException {
		this.message = notificationJson.getString("message");
		this.serverId = notificationJson.getLong("id");
		this.userId = notificationJson.getLong("user_id");
	}
}