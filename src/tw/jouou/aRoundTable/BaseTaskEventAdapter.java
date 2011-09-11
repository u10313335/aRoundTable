package tw.jouou.aRoundTable;

import java.util.List;

import tw.jouou.aRoundTable.bean.TaskEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

public class BaseTaskEventAdapter extends BaseExpandableListAdapter {
	private List<TaskEvent> taskevents;
	private List<TaskEvent> overDue;
	public BaseTaskEventAdapter(List<TaskEvent> taskevents, List<TaskEvent> overDue) {
		this.taskevents = taskevents;
		this.overDue = overDue;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if(groupPosition > 1)
			return null;
		
		return overDue.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(groupPosition == 0)
			return overDue.size();
		
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		if(groupPosition > 1)
			return taskevents.get(groupPosition -1);
		
		return null;
	}

	@Override
	public int getGroupCount() {
		return 1 + taskevents.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
}
