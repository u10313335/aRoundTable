package tw.jouou.aRoundTable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ViewFinishedItemsActivity extends Activity {
	
	//TODO:dummy test data, remove them ASAP
	private String itemOwners[] = { "小羽、小熊", "albb", "洞洞", "所有人", "小羽、小熊", "albb", "洞洞", "所有人" };
	private DBUtils dbUtils;
	private Bundle mBundle;
	private Project mProj;
	private static String TAG = "ViewFinishedItemsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_finished_items);
		dbUtils = new DBUtils(this);
		mBundle = this.getIntent().getExtras();
        mProj = (Project)mBundle.get("proj");

        TextView txProjName = (TextView) findViewById(R.id.finished_item_title);
        txProjName.setText("\"" + mProj.getName() + "\"專案下已完成項目：");
        
		ArrayList<HashMap <String, Object>> items = new ArrayList<HashMap <String, Object>> ();
		List<Task> tasks = null;
    	try {
    		tasks = dbUtils.tasksDelegate.getFinished(mProj.getServerId());
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
    	
		ListView finishedItemListView = (ListView) findViewById(R.id.finished_item_list);

	    for (int i=0; i < tasks.size(); i++) {
	    	Date due = tasks.get(i).getDueDate();
	    	HashMap< String, Object > item = new HashMap< String, Object >();
	    	item.put("itemName", tasks.get(i).getName());
	    	item.put("itemOwner", itemOwners[0]);
    		if (due==null) {
				item.put("dueDate", getString(R.string.undetermined));
    		} else {
    			item.put("dueDate", tasks.get(i).getDue());
    		}
	    	items.add(item);
	    }
	    
	    finishedItemListView.setAdapter(new SpecialAdapter(this,items,R.layout.view_finished_items_item,
    			new String[] { "itemName", "itemOwner", "dueDate" },
    			new int[] { R.id.finished_item_name,
						R.id.finished_item_owner, R.id.finished_item_duedate }));

	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (dbUtils != null) {
			dbUtils.close();
			dbUtils = null;
		}
	}
	

	private class SpecialAdapter extends SimpleAdapter {
		ArrayList<HashMap<String, Object>> items;
		public SpecialAdapter(Context context, ArrayList<HashMap<String, Object>> items,
				int resource, String[] from, int[] to) {
			super(context, items, resource, from, to);
			this.items = items;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
		  View view = super.getView(position, convertView, parent);  
		  return view;
		}
	}
	
}
