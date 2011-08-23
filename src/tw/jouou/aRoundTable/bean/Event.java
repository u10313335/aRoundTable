package tw.jouou.aRoundTable.bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import tw.jouou.aRoundTable.util.DBUtils;
import android.content.ContentValues;

public class Event implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private long id;
	private long projId;
	private long serverId;
	private String name;
	private Date start_at, end_at;
	private String location, note;
	private int type = 1;
	
	public Event(long id, long projId, long serverId, String name, Date start_at,
			Date end_at, String location, String note) throws ParseException {
		this.id = id;
		this.projId = projId;
		this.serverId = serverId;
		this.name = name;
		this.start_at = start_at;
		this.end_at = end_at;
		this.location = location;
		this.note = note;
	}
	
	public Event(long projId, long serverId, String name, Date start_at, Date end_at, String location,
			String note) throws ParseException {
		this.projId = projId;
		this.serverId = serverId;
		this.name = name;
		this.start_at = start_at;
		this.end_at = end_at;
		this.location = location;
		this.note = note;
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
	
	public Date getStartAt() {
		return start_at;
	}
	
	public Date getEndAt() {
		return end_at;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String getNote() {
		return note;
	}
	
	public Event(JSONObject json) throws JSONException, ParseException {
		this.name = json.getString("name");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.start_at = sdf.parse(json.getString("start_at"));
		this.end_at = sdf.parse(json.getString("end_at"));
		this.location = json.getString("location");
		this.note = json.getString("note");
	}
	
	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		values.put(DBUtils.FIELD_EVENT_NAME, name);
		values.put(DBUtils.FIELD_EVENT_PROJECTID, projId);
		values.put(DBUtils.FIELD_EVENT_SERVERID, serverId);
		if (start_at==null) {
			values.put(DBUtils.FIELD_EVENT_START, "");
			values.put(DBUtils.FIELD_EVENT_END, "");
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			values.put(DBUtils.FIELD_EVENT_START, sdf.format(start_at));
			values.put(DBUtils.FIELD_EVENT_END, sdf.format(end_at));
		}
		values.put(DBUtils.FIELD_EVENT_LOCATION, location);
		values.put(DBUtils.FIELD_EVENT_NOTE, note);
		values.put(DBUtils.FIELD_EVENT_TYPE, type);
		return values;
	}
}
