package tw.jouou.aRoundTable;

import java.util.List;

import tw.jouou.aRoundTable.bean.TaskEvent;

public class AllItemAdapter extends BaseTaskEventAdapter {

	public AllItemAdapter(List<TaskEvent> taskevents, List<TaskEvent> overDue) {
		super(taskevents, overDue);
	}

}
