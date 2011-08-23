package tw.jouou.aRoundTable.bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import tw.jouou.aRoundTable.util.DBUtils;
import android.content.ContentValues;

public class Task implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private long id;
	private long projId;
	private long serverId;
	private String name;
	private Date due;
	private String note;
	private int done;
	private int type = 0;
	
	public Task(long id, long projId, long serverId, String name, Date due,
			String note, int done) throws ParseException {
		this.id = id;
		this.projId = projId;
		this.serverId = serverId;
		this.name = name;
		this.due = due;
		this.note = note;
		this.done = done;
	}
	
	public Task(long projId, long serverId, String name, Date due,
			String note, int done) throws ParseException {
		this.projId = projId;
		this.serverId = serverId;
		this.name = name;
		this.due = due;
		this.note = note;
		this.done = done;
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
	
	public void setDone(int done) {
		this.done = done;
	}
	
	public int getType() {
		return type;
	}
	
	public Task(JSONObject json) throws JSONException, ParseException {
		this.name = json.getString("name");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		this.due = sdf.parse(json.getString("due"));
		
		this.note = json.getString("note");
	}
	
	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		values.put(DBUtils.FIELD_TASK_NAME, name);
		values.put(DBUtils.FIELD_TASK_PROJECTID, projId);
		values.put(DBUtils.FIELD_TASK_SERVERID, serverId);
		if (due==null) {
			values.put(DBUtils.FIELD_TASK_DUEDATE, "");
		} else {
			due.setHours(23);
			due.setMinutes(59);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			values.put(DBUtils.FIELD_TASK_DUEDATE, sdf.format(due));
		}
		values.put(DBUtils.FIELD_TASK_NOTE, note);
		values.put(DBUtils.FIELD_TASK_FINISHED, done);
		values.put(DBUtils.FIELD_TASK_TYPE, type);
		return values;
	}
}
