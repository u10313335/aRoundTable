package tw.jouou.aRoundTable.bean;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import tw.jouou.aRoundTable.lib.GravatarApi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User {
	@DatabaseField(generatedId = true)
	public int id;
	
	@DatabaseField(columnName = "server_id")
	public int serverId;
	
	@DatabaseField(columnName = "project_id")
	public int projectId;
	
	@DatabaseField
	public String name;
	
	@DatabaseField
	public String email;
	
	public User(){
		
	}
	
	public User(int projectId, JSONObject jsonObject) throws JSONException{
		this.serverId = jsonObject.getInt("id");
		this.projectId = projectId;
		this.name = jsonObject.getString("name");
		this.email = jsonObject.getString("email");
	}
	
	/**
	 * Retrieve user's gravatar
	 * 
	 * This method WILL BLCOK, do not call in main thread
	 */
	public Bitmap getGravatar(){
		try {
			return BitmapFactory.decodeStream((InputStream)new URL(GravatarApi.getAvatarURL(email)).getContent());
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		return null;
	}
}
