package tw.jouou.aRoundTable;

import java.util.List;

import tw.jouou.aRoundTable.bean.TaskEvent;

public class OwnedTaskEventAdapter extends BaseTaskEventAdapter {

	public OwnedTaskEventAdapter(List<TaskEvent> taskevents, List<TaskEvent> overDue) {
		super(taskevents, overDue);
	}

}
