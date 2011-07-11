package tw.jouou.aRoundTable.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class TaskEvent {
	private int type;
	private String name;
	private Date due;
	private String note;
	public static int TYPE_TASK = 0;
	public static int TYPE_EVENT = 1;
	
	public TaskEvent(JSONObject json) throws JSONException, ParseException{
		this.type = json.getInt("type");
		this.name = json.getString("name");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		this.due = sdf.parse(json.getString("due"));
		
		this.note = json.getString("ntoe");
	}
}
