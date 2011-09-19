package tw.jouou.aRoundTable;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.TaskEvent;

public class OwnedTaskEventAdapter extends BaseTaskEventAdapter {

	public OwnedTaskEventAdapter(Context context, List<TaskEvent> taskevents, List<TaskEvent> overDue) {
		super(context, taskevents, overDue);
	}
	
	@Override
	protected void fillEntry(View view, TaskEvent taskEvent){
		super.fillEntry(view, taskEvent);
		Project project = dbUtils.projectsDelegate.get(taskEvent.getProjId());
		
		((TextView) view.findViewById(R.id.item_meta)).setText(project.getName());
	}
}
