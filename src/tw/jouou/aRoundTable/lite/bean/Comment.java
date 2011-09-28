package tw.jouou.aRoundTable.lite.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class Comment {
	public int id;
	public int userId;
	public String content;
	public String commentableType;
	public int commentableId;
	public Date createdAt;
	public Date updatedAt;
	
	public Comment(JSONObject commentJson) throws JSONException, ParseException{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		this.id = commentJson.getInt("id");
		this.userId = commentJson.getInt("user_id");
		this.content = commentJson.getString("content");
		this.commentableType = commentJson.getString("commentable_type");
		this.commentableId = commentJson.getInt("commentable_id");
		this.createdAt = formatter.parse(commentJson.getString("created_at"));
		this.updatedAt = formatter.parse(commentJson.getString("updated_at"));
	}
}
