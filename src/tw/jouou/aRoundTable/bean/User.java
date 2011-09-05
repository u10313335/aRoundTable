package tw.jouou.aRoundTable.bean;

import java.io.Serializable;
import tw.jouou.aRoundTable.util.DBUtils;
import android.content.ContentValues;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	private long id;
	private String token;

	public User(long id, String token){
		this.id = id;
		this.token = token;
	}
	
	public User(String token){
		this.token = token;
	}
	
	public long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		values.put(DBUtils.FIELD_USER_TOKEN, token);
		return values;
	}

}