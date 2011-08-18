package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

public class AddItemActivity extends TabActivity{
	
	private Bundle bundle;
	private TabHost tabHost;
	private TabHost.TabSpec spec;
	private int addOrEdit; // 0 add, 1 edit
	private Task mTask;
	private static String TAG = "AddItemActivity";
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.additem);
		tabHost = getTabHost();  // The activity TabHost
		tabHost.setCurrentTab(0);
		bundle = this.getIntent().getExtras();
		addOrEdit = bundle.getInt("addOrEdit");
		if (addOrEdit==0) {
			addSingleActivity();
			addBatchActivity();
			addEventActivity();
		} else {
			mTask = (Task)bundle.get("task");
			switch (bundle.getInt("type")) {
				case 0:
					addSingleActivity();
					break;
				case 1:
					addEventActivity();
					break;
			}
		}
	}
	
	public void addSingleActivity() {  
		Intent intent = new Intent();  
	    intent.setClass(AddItemActivity.this, AddSingleTaskActivity.class);
	    intent.putExtra("addOrEdit", addOrEdit);
    	intent.putExtra("proj", (Project)bundle.get("proj"));
	    if (addOrEdit == 1) {
        	intent.putExtra("task", mTask);
        }
	    spec = tabHost.newTabSpec("tab1");
	    spec.setIndicator(createTabView(tabHost.getContext(), getString(R.string.add_task)));  
	    spec.setContent(intent);
	    tabHost.addTab(spec);
	}
	       
	public void addBatchActivity() {  
		Intent intent = new Intent();  
	    intent.setClass(AddItemActivity.this, AddBatchTaskActivity.class);
	    intent.putExtra("addOrEdit", addOrEdit);
	    if (addOrEdit == 0) {
	    	intent.putExtra("proj", (Project)bundle.get("proj"));
        } else {
        	
        }
	    spec = tabHost.newTabSpec("tab2");  
	    spec.setIndicator(createTabView(tabHost.getContext(), getString(R.string.add_batch)));  
	    spec.setContent(intent);          
	    tabHost.addTab(spec);  
	}  
	
	public void addEventActivity(){  
		Intent intent = new Intent();  
		intent.setClass(AddItemActivity.this, AddEventActivity.class);
	    intent.putExtra("addOrEdit", addOrEdit);
	    if (addOrEdit == 0) {
	    	intent.putExtra("proj", (Project)bundle.get("proj"));
        } else {
        	intent.putExtra("taskevent", mTask);
        	intent.putExtra("projname", bundle.getString("projname"));
        }
		spec = tabHost.newTabSpec("tab3");  
		spec.setIndicator(createTabView(tabHost.getContext(), getString(R.string.add_event)));  
		spec.setContent(intent);          
		tabHost.addTab(spec);  
	}
	
	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}
  	  
}