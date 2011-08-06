package tw.jouou.aRoundTable.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import tw.jouou.aRoundTable.util.DBUtils;
import android.content.ContentValues;

public class TaskEvent {
	private long id;
	private long projId;
	private long serverId = 0;
	private int type;
	private String name;
	private Date due;
	private String note;
	private int done;
	public static int TYPE_TASK = 0;
	public static int TYPE_EVENT = 1;
	
	public TaskEvent(long id, long projId, long serverId, int type, String name, String due,
			String note, int done) throws ParseException{
		this.id = id;
		this.projId = projId;
		this.serverId = serverId;
		this.type = type;
		this.name = name;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.due = sdf.parse(due);
		this.note = note;
		this.done = done;
	}
	
	public TaskEvent(long projId, int type, String name, String due,
			String note, int done) throws ParseException{
		this.projId = projId;
		this.type = type;
		this.name = name;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.due = sdf.parse(due);
		this.note = note;
		this.done = done;
	}
	
	public long getId(){
		return id;	
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public long getServerId(){
		return serverId;
	}
	
	public void setServerId(long serverId){
		this.serverId = serverId;
	}
	
	public TaskEvent(JSONObject json) throws JSONException, ParseException{
		this.type = json.getInt("type");
		this.name = json.getString("name");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		this.due = sdf.parse(json.getString("due"));
		
		this.note = json.getString("note");
	}
	
	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		values.put(DBUtils.FIELD_TASKEVENT_NAME, name);
		values.put(DBUtils.FIELD_TASKEVENT_PROJECTID, projId);
		values.put(DBUtils.FIELD_TASKEVENT_SERVERID, serverId);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		values.put(DBUtils.FIELD_TASKEVENT_DUEDATE, sdf.format(due));
		values.put(DBUtils.FIELD_TASKEVENT_NOTE, note);
		values.put(DBUtils.FIELD_TASKEVENT_TYPE, type);
		values.put(DBUtils.FIELD_TASKEVENT_FINISHED, done);
		return values;
	}
}
