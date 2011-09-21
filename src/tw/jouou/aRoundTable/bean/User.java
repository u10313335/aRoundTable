package tw.jouou.aRoundTable.bean;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User {
	@DatabaseField(generatedId = true)
	public int id;
	
	@DatabaseField(columnName = "server_id")
	public long serverId;
	
	@DatabaseField(columnName = "project_id")
	public long projectId;
	
	@DatabaseField
	public String name;
	
	@DatabaseField
	public String email;
	
	public User(){
		
	}
	
	public User(long projectId, JSONObject jsonObject) throws JSONException{
		this.serverId = jsonObject.getLong("id");
		this.projectId = projectId;
		this.name = jsonObject.getString("name");
		this.email = jsonObject.getString("email");
	}
}
