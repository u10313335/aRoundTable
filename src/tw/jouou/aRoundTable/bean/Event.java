package tw.jouou.aRoundTable.bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
	private Date updateAt;
	private int type = 1;
	
	public Event(long id, long projId, long serverId, String name, Date start_at,
			Date end_at, String location, String note, Date updateAt) throws ParseException {
		this.id = id;
		this.projId = projId;
		this.serverId = serverId;
		this.name = name;
		this.start_at = start_at;
		this.end_at = end_at;
		this.location = location;
		this.note = note;
		this.updateAt = updateAt;
	}
	
	public Event(long projId, long serverId, String name, Date start_at, Date end_at, String location,
			String note, Date updateAt) {
		this.projId = projId;
		this.serverId = serverId;
		this.name = name;
		this.start_at = start_at;
		this.end_at = end_at;
		this.location = location;
		this.note = note;
		this.updateAt = updateAt;
	}
	
	public Event(JSONObject eventJson) throws JSONException {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			this.name = eventJson.getString("name");
			this.serverId = eventJson.getLong("id");
			this.projId = eventJson.getLong("project_id");
			if(!(eventJson.getString("start_at").equals("null"))) {
				this.start_at = formatter.parse(eventJson.getString("start_at"));
				if(eventJson.getString("end_at").equals("null")) {
					this.end_at = formatter.parse(eventJson.getString("start_at"));
				} else {
					this.end_at = formatter.parse(eventJson.getString("end_at"));
				}
			}
			this.location = eventJson.getString("location");
			this.note = eventJson.getString("note");
			this.updateAt = formatter.parse(eventJson.getString("updated_at"));
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
	
	public void setUpdateAt(Date updateAt) {
		this.updateAt = updateAt;
	}
	
	public Date getUpdateAt() {
		return updateAt;
	}
	
	public ContentValues getValues() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ContentValues values = new ContentValues();
		values.put(DBUtils.FIELD_EVENT_NAME, name);
		values.put(DBUtils.FIELD_EVENT_PROJECTID, projId);
		values.put(DBUtils.FIELD_EVENT_SERVERID, serverId);
		if (start_at==null) {
			values.put(DBUtils.FIELD_EVENT_START, "");
		} else {
			values.put(DBUtils.FIELD_EVENT_START, sdf.format(start_at));
		}
		if (end_at==null) {
			values.put(DBUtils.FIELD_EVENT_END, "");
		} else {
			values.put(DBUtils.FIELD_EVENT_END, sdf.format(end_at));
		}
		values.put(DBUtils.FIELD_EVENT_LOCATION, location);
		values.put(DBUtils.FIELD_EVENT_NOTE, note);
		values.put(DBUtils.FIELD_EVENT_UPDATED_AT, sdf.format(updateAt));
		values.put(DBUtils.FIELD_EVENT_TYPE, type);
		return values;
	}
}
