package tw.jouou.aRoundTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.lib.SyncService;
import tw.jouou.aRoundTable.util.DBUtils;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class BaseTaskEventAdapter extends BaseExpandableListAdapter {
	protected DBUtils dbUtils;
	private List<TaskEvent> taskevents;
	private List<TaskEvent> overDue;
	private Context context;
	private LayoutInflater inflater;
	private TypedArray colors;
	private static final int[] EMPTY_STATE_SET = {};
    private static final int[] GROUP_EXPANDED_STATE_SET = {android.R.attr.state_expanded};
	
	public BaseTaskEventAdapter(Context context, List<TaskEvent> taskevents, List<TaskEvent> overDue) {
		this.context = context;
		this.inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.colors = context.getResources().obtainTypedArray(R.array.project_colors);
		this.taskevents = taskevents;
		this.overDue = overDue;
		this.dbUtils = DBUtils.getInstance(context);
	}
	
	public void replaceDataSet(List<TaskEvent> taskevents, List<TaskEvent> overDue){
		this.taskevents = taskevents;
		this.overDue = overDue;
		notifyDataSetChanged();
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
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		if(convertView == null)
			convertView = inflater.inflate(R.layout.taskevent_list_item, parent, false);
		
		fillEntry(convertView, overDue.get(childPosition));
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(groupPosition == 0)
			return overDue.size();
		
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		if(groupPosition > 0)
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
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View v;
		if(groupPosition == 0){
			v = inflater.inflate(R.layout.taskevent_collapse_group, parent, false);
			((TextView) v.findViewById(R.id.group_text)).setText(context.getString(R.string.item_overdue, overDue.size()));
			((ImageView) v.findViewById(R.id.group_indicator)).getDrawable().setState((isExpanded)? GROUP_EXPANDED_STATE_SET: EMPTY_STATE_SET);
		}else{
			TaskEvent taskEvent = (TaskEvent) getGroup(groupPosition);
			
			if(convertView != null && convertView.getId() == R.id.taskevent_list_item)
				v = convertView;
			else
				v =  inflater.inflate(R.layout.taskevent_list_item, parent, false);
			
			fillEntry(v, taskEvent);
		}
		return v;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	protected void fillEntry(View view, final TaskEvent taskEvent){
		Project project = dbUtils.projectsDelegate.get(taskEvent.getProjId());
		
		// Project indicator
		view.findViewById(R.id.item_color).setBackgroundColor(colors.getColor(project.getColor(), 0));
		
		// Finish checkbox
		CheckBox done = (CheckBox) view.findViewById(R.id.item_done);
		done.setChecked(taskEvent.getDone() == 1);
		done.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			  @Override
			  public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
				  try {
					  	Task task;
      					task = dbUtils.tasksDelegate.getTask(taskEvent.getServerId());
      					task.setDone(true);
      					task.setUpdateAt(new Date());
      					dbUtils.tasksDelegate.update(task);
      					//TODO: This should not be here
      					((MainActivity)context).update();
    		    		Intent syncIntent = new Intent(context, SyncService.class);
    		    		context.startService(syncIntent);
				  } catch (ParseException e) {
      					e.printStackTrace();
				  }
			  }
		  });
		
		((TextView) view.findViewById(R.id.item_name)).setText(taskEvent.getName());
		
		TextView dueCountdown = ((TextView) view.findViewById(R.id.item_due_countdown));
		TextView dueDate = (TextView) view.findViewById(R.id.item_duedate);
		if(taskEvent.getDueDate() == null){
			dueCountdown.setTextColor(Color.WHITE);
			dueCountdown.setText(context.getString(R.string.undetermined));
			dueDate.setVisibility(View.INVISIBLE);
		}else{
			int distance = calcDateDistance(taskEvent.getDueDate());
			if(distance == 0){
				dueCountdown.setTextColor(Color.RED);
				dueCountdown.setText(context.getString(R.string.due_today));
				dueDate.setText(taskEvent.getDue());
				dueDate.setVisibility(View.INVISIBLE);
			}else{
				dueCountdown.setTextColor(Color.WHITE);
				//FIXME: use sprintf style instead
				dueCountdown.setText(distance + context.getString(R.string.dayafter));
				dueDate.setText(taskEvent.getDue());
				dueDate.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private static int calcDateDistance(Date due) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date today = new Date();
		long DAY = 24L * 60L * 60L * 1000L;
		GregorianCalendar dueCal = new GregorianCalendar();
		GregorianCalendar todayCal = new GregorianCalendar();
		dueCal.setTime(due);
		try {
			todayCal.setTime(formatter.parse(formatter.format(today)));
			dueCal.setTime(formatter.parse(formatter.format(due)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return Math.round((dueCal.getTime().getTime()-todayCal.getTime().getTime()) /DAY);
	}
}
