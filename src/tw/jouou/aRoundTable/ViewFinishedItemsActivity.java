package tw.jouou.aRoundTable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
	private final String colors[] = { "#00B0CF", "#A2CA30", "#F2E423",
			"#CA4483", "#E99314", "#C02B20", "#F7F7CF", "#225DAB" };
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
		List<TaskEvent> taskevents = null;
    	try {
    		taskevents = dbUtils.taskEventDelegate.getFinished(mProj.getServerId());
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
    	
		ListView finishedItemListView = (ListView) findViewById(R.id.finished_item_list);

	    for (int i=0; i < taskevents.size(); i++) {
	    	Date due = taskevents.get(i).getDueDate();
	    	HashMap< String, Object > item = new HashMap< String, Object >();
	    	item.put("itemName", taskevents.get(i).getName());
	    	item.put("itemOwner", itemOwners[0]);
    		if (due==null) {
				item.put("dueDate", "");
    		} else {
    			item.put("dueDate", taskevents.get(i).getDue());
    		}
    		item.put("type", taskevents.get(i).getType());
	    	item.put("color", mProj.getColor());
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
		  TextView txOwner = (TextView) view.findViewById(R.id.finished_item_owner);
		  TextView color = (TextView) view.findViewById(R.id.finished_item_color);
		  if((Integer)items.get(position).get("type") == 1) {
			  color.setVisibility(View.INVISIBLE);
			  if(txOwner!=null) {
				  txOwner.setVisibility(View.INVISIBLE);
			  }
		  }
		  color.setBackgroundColor(Color.parseColor(colors[(Integer)items.get(position).get("color")])); 		  
		  return view;
		}
	}
	
}
