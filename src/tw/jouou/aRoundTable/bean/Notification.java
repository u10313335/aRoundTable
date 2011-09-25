package tw.jouou.aRoundTable.bean;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public class Notification{
	public static final String COLUMN_READ = "read";
	
	@DatabaseField(id = true)
	public int id;
	
	@DatabaseField(columnName = "user_id")
	public int userId;
	
	@DatabaseField(columnName = "notifiable_type")
	public String notifiableType;
	
	@DatabaseField(columnName = "notifiable_id")
	public int notifiableId;
	
	@DatabaseField
	public String message;
	
	@DatabaseField
	public boolean read;
	
	public Notification(){
	}
	
	public Notification(JSONObject notificationJson) throws JSONException {
		this.id = notificationJson.getInt("id");
		this.userId = notificationJson.getInt("user_id");
		this.notifiableType = notificationJson.getString("notifiable_type");
		this.notifiableId = notificationJson.getInt("notifiable_id");
		this.message = notificationJson.getString("message");
	}
}