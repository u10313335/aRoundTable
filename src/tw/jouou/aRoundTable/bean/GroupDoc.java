package tw.jouou.aRoundTable.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import tw.jouou.aRoundTable.util.DBUtils;
import android.content.ContentValues;

public class GroupDoc implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private long id;
	private long projId;
	private long serverId;
	private String content;
	private Date updateAt;
	
	public GroupDoc(long id, long projId, long serverId, String content, Date updateAt) {
		this.id = id;
		this.projId = projId;
		this.serverId = serverId;
		this.content = content;
		this.updateAt = updateAt;
	}
	
	public GroupDoc(long projId, long serverId, String content, Date updateAt) {
		this.projId = projId;
		this.serverId = serverId;
		this.content = content;
		this.updateAt = updateAt;
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
	
	public String getContent() {
		return content;
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
		values.put(DBUtils.FIELD_GROUPDOC_CONTENT, content);
		values.put(DBUtils.FIELD_GROUPDOC_PROJECTID, projId);
		values.put(DBUtils.FIELD_GROUPDOC_SERVERID, serverId);
		values.put(DBUtils.FIELD_GROUPDOC_UPDATED_AT, sdf.format(updateAt));
		return values;
	}
}


