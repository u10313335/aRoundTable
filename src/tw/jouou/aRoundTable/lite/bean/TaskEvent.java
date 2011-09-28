package tw.jouou.aRoundTable.lite.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private long serverId;
	private String name;
	private long projId;
	private Date due;
	private int done;
	private int type;

	public TaskEvent(long serverId, String name, long projId, Date due, int done, int type) {
		this.serverId = serverId;
		this.name = name;
		this.projId = projId;
		this.due = due;
		this.done = done;
		this.type = type;
	}
	
	public long getServerId() {
		return serverId;
	}
	
	public String getName() {
		return name;
	}
	
	public long getProjId() {
		return projId;
	}
	
	public String getDue() {
		String date;
		if(type==0) {
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd");
			date = formatter.format(due);
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm");
			date = formatter.format(due);
		}
		return date;
	}
	
	public Date getDueDate() {
		return due;
	}
	
	public int getDone() {
		return done;
	}
	
	public int getType(){
		return type;	
	}
	
}
