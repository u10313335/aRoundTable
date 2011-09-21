package tw.jouou.aRoundTable;

import java.util.List;

import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.util.DBUtils;
import tw.jouou.aRoundTable.util.DBUtils.TaskEventDelegate;
import android.content.Context;
import android.view.View;
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
		if(taskEvent.getType() == TaskEventDelegate.TYPE_EVENT){
			metaTextView.setText("");
		}else{
			metaTextView.setText(genUserNames(taskEvent.getServerId()));
		}
	}

	private String genUserNames(long taskId){
	     String nameList = "";
	     String[] names = dbUtils.tasksUsersDelegate.getUsers(taskId);
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
