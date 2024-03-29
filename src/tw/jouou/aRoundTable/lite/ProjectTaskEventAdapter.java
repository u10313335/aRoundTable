package tw.jouou.aRoundTable.lite;

import java.util.List;

import tw.jouou.aRoundTable.lite.bean.TaskEvent;
import tw.jouou.aRoundTable.lite.util.DBUtils.TaskEventDelegate;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class ProjectTaskEventAdapter extends BaseTaskEventAdapter {

	public ProjectTaskEventAdapter(Context context, List<TaskEvent> taskevents,
			List<TaskEvent> overDue) {
		super(context, taskevents, overDue);
		
	}
	
	@Override
	protected void fillEntry(View view, TaskEvent taskEvent){
		super.fillEntry(view, taskEvent);
		
		TextView metaTextView = (TextView) view.findViewById(R.id.item_meta);
		CheckBox metaCheckBox = (CheckBox) view.findViewById(R.id.item_done);
		if(taskEvent.getType() == TaskEventDelegate.TYPE_EVENT){
			metaTextView.setText("");
			metaCheckBox.setVisibility(View.INVISIBLE);
		}else{
			metaTextView.setText(genUserNames(taskEvent.getServerId()));
			metaCheckBox.setVisibility(View.VISIBLE);
		}
	}

	private String genUserNames(long taskId){
	     String nameList = "";
	     String[] names = dbUtils.tasksUsersDelegate.getUsersName(taskId);
	     if(names != null) {
	    	 nameList = names[0];
	    	 int i = 1;
	    	 while(i < names.length) {
	    		 nameList = names[i] + ", " + nameList;
	    		 i++;
	    	 }
	     }
	     return nameList;
	}
}
