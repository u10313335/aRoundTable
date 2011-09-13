package tw.jouou.aRoundTable;

import java.util.List;

import tw.jouou.aRoundTable.bean.TaskEvent;

public class ProjectTaskEventAdapter extends BaseTaskEventAdapter {

	public ProjectTaskEventAdapter(List<TaskEvent> taskevents,
			List<TaskEvent> overDue) {
		super(taskevents, overDue);
	}

}
