package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.TaskEvent;

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
	private Resources res;
	private int type;
	private TaskEvent taskEvent;
	private static String TAG = "AddItemActivity";
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.additem);
		//res = getResources(); // Resource object to get drawables
		tabHost = getTabHost();  // The activity TabHost
		tabHost.setCurrentTab(0);
		bundle = this.getIntent().getExtras();
		type = bundle.getInt("type");
		if (type==0) {
			addSingleActivity();
			addBatchActivity();
			addEventActivity();
		} else {
			taskEvent = (TaskEvent)bundle.get("taskevent");
			switch (taskEvent.getType()) {
				case 0:
					addSingleActivity();
					break;
				case 1:
					addBatchActivity();
					break;
				case 2:
					addEventActivity();
			}
		}
	}
	
	public void addSingleActivity() {  
		Intent intent = new Intent();  
	    intent.setClass(AddItemActivity.this, AddSingleTaskActivity.class);
	    intent.putExtra("type", type);
    	intent.putExtra("proj", (Project)bundle.get("proj"));
	    if (type == 1) {
        	intent.putExtra("taskevent", taskEvent);
        }
	    spec = tabHost.newTabSpec("tab1");
	    spec.setIndicator(createTabView(tabHost.getContext(), "單一工作"));  
	    spec.setContent(intent);
	    tabHost.addTab(spec);
	}
	       
	public void addBatchActivity() {  
		Intent intent = new Intent();  
	    intent.setClass(AddItemActivity.this, AddBatchTaskActivity.class);
	    intent.putExtra("type", type);
	    if (type == 0) {
	    	intent.putExtra("proj", (Project)bundle.get("proj"));
        } else {
        	
        }
	    spec = tabHost.newTabSpec("tab2");  
	    spec.setIndicator(createTabView(tabHost.getContext(), "批次新增"));  
	    spec.setContent(intent);          
	    tabHost.addTab(spec);  
	}  
	
	public void addEventActivity(){  
		Intent intent = new Intent();  
		intent.setClass(AddItemActivity.this, AddEventActivity.class);
	    intent.putExtra("type", type);
	    if (type == 0) {
	    	intent.putExtra("proj", (Project)bundle.get("proj"));
        } else {
        	intent.putExtra("taskevent", taskEvent);
        	intent.putExtra("projname", bundle.getString("projname"));
        }
		spec = tabHost.newTabSpec("tab3");  
		spec.setIndicator(createTabView(tabHost.getContext(), "事件"));  
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