package tw.jouou.aRoundTable.lite;

import java.util.Date;
import java.util.List;

import tw.jouou.aRoundTable.lite.bean.Task;
import tw.jouou.aRoundTable.lite.util.DBUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FinishedTaskAdapter extends BaseAdapter {
	protected DBUtils dbUtils;
	private List<Task> finishedTask;
	private LayoutInflater inflater;
	
	public FinishedTaskAdapter(Context context, List<Task> finishedTask) {
		this.inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.finishedTask = finishedTask;
		this.dbUtils = DBUtils.getInstance(context);
	}
	
	@Override
	public int getCount() {
		return finishedTask.size();
	}

	@Override
	public Object getItem(int position) {
		return finishedTask.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = inflater.inflate(R.layout.view_finished_items_item, parent, false);
		
		fillEntry(convertView, finishedTask.get(position));
		return convertView;
	}

	protected void fillEntry(View view, final Task task){
		
		((TextView) view.findViewById(R.id.finished_item_name)).setText(task.getName());
		Date due = task.getDueDate();
		if(due!=null) {
			((TextView) view.findViewById(R.id.finished_item_duedate)).setText(task.getDue());
		} else {
			((TextView) view.findViewById(R.id.finished_item_duedate)).setText(R.string.undetermined);
		}
		((TextView) view.findViewById(R.id.finished_item_owner)).setText(genUserNames(task.getServerId()));
	}
	
	private String genUserNames(long taskId) {
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
