package tw.jouou.aRoundTable.bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.jouou.aRoundTable.util.DBUtils;
import android.content.ContentValues;
import android.util.Log;

public class Task implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private long id;
	private long projId;
	private long serverId;
	private String name;
	private Date due;
	private String note;
	private boolean done;
	private Date updateAt;
	private int type = 0;
	private int user_ids[];
	
	public Task(long id, long projId, long serverId, String name, Date due,
			String note, boolean done, Date updateAt) {
		this.id = id;
		this.projId = projId;
		this.serverId = serverId;
		this.name = name;
		this.due = due;
		this.note = note;
		this.done = done;
		this.updateAt = updateAt;
	}
	
	public Task(long projId, long serverId, String name, Date due,
			String note, boolean done, Date updateAt) {
		this.projId = projId;
		this.serverId = serverId;
		this.name = name;
		this.due = due;
		this.note = note;
		this.done = done;
		this.updateAt = updateAt;
	}
	
	public Task(JSONObject taskJson) throws JSONException {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			this.name = taskJson.getString("name");
			this.serverId = taskJson.getLong("id");
			this.projId = taskJson.getLong("project_id");
			if(!(taskJson.getString("due").equals("null"))) {
				this.due = formatter.parse(taskJson.getString("due"));
			}
			this.note = taskJson.getString("note");
			this.done = (taskJson.getString("finished").equals("1")) ? true : false;
			this.updateAt = formatter.parse(taskJson.getString("updated_at"));
			JSONArray jsonArray = taskJson.getJSONArray("user_ids");
			this.user_ids = new int[jsonArray.length()];
			for(int i=0; i<this.user_ids.length; i++){
				this.user_ids[i] = jsonArray.getInt(i);
				Log.v("Task", "task_id: " + this.serverId + " user_ids: " + jsonArray.getInt(i));
			}
		} catch (ParseException e) {
			System.out.println("Parse error");
		}
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
	
	public long getProjId() {
		return projId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDue() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		return formatter.format(due);
	}
	
	public Date getDueDate() {
		return due;
	}
	
	public String getNote() {
		return note;
	}
	
	public void setDone(boolean done) {
		this.done = done;
	}
	
	public boolean getDone() {
		return done;
	}
	
	public void setUpdateAt(Date updateAt) {
		this.updateAt = updateAt;
	}
	
	public Date getUpdateAt() {
		return updateAt;
	}
	
	public int getType() {
		return type;
	}
	
	public ContentValues getValues() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ContentValues values = new ContentValues();
		values.put(DBUtils.FIELD_TASK_NAME, name);
		values.put(DBUtils.FIELD_TASK_PROJECTID, projId);
		values.put(DBUtils.FIELD_TASK_SERVERID, serverId);
		if (due==null) {
			values.put(DBUtils.FIELD_TASK_DUEDATE, "");
		} else {
			due.setHours(23);
			due.setMinutes(59);
			values.put(DBUtils.FIELD_TASK_DUEDATE, sdf.format(due));
		}
		values.put(DBUtils.FIELD_TASK_NOTE, note);
		values.put(DBUtils.FIELD_TASK_FINISHED, (done)? "1" : "0");
		values.put(DBUtils.FIELD_TASK_UPDATED_AT, sdf.format(updateAt));
		values.put(DBUtils.FIELD_TASK_TYPE, type);
		return values;
	}
	
	public ContentValues getMembersValues() {
		ContentValues members_values = new ContentValues();
		for(int i=0; i<user_ids.length; i++){
			members_values.put(DBUtils.FIELD_TASKS_MEMBERS_TASKID, serverId);
			members_values.put(DBUtils.FIELD_TASKS_MEMBERS_PROJECTID, projId);
			members_values.put(DBUtils.FIELD_TASKS_MEMBERS_MEMBERID, user_ids[i]);
		}
		return members_values;
	}
}
