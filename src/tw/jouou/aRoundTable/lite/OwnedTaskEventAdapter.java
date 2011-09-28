package tw.jouou.aRoundTable.lite;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import tw.jouou.aRoundTable.lite.bean.Project;
import tw.jouou.aRoundTable.lite.bean.TaskEvent;
import tw.jouou.aRoundTable.lite.util.DBUtils.TaskEventDelegate;

public class OwnedTaskEventAdapter extends BaseTaskEventAdapter {

	public OwnedTaskEventAdapter(Context context, List<TaskEvent> taskevents, List<TaskEvent> overDue) {
		super(context, taskevents, overDue);
	}
	
	@Override
	protected void fillEntry(View view, TaskEvent taskEvent){
		super.fillEntry(view, taskEvent);
		Project project = dbUtils.projectsDelegate.get(taskEvent.getProjId());
		CheckBox metaCheckBox = (CheckBox) view.findViewById(R.id.item_done);
		if(taskEvent.getType() == TaskEventDelegate.TYPE_EVENT){
			metaCheckBox.setVisibility(View.INVISIBLE);
		}
		((TextView) view.findViewById(R.id.item_meta)).setText(project.getName());
	}
}
