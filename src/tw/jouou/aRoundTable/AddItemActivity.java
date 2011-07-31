package tw.jouou.aRoundTable;

import java.util.Date;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.TaskEvent;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

public class AddItemActivity extends TabActivity{
	
	private Bundle bundle;
	private TabHost tabHost;
	private TabHost.TabSpec spec;
	private Resources res;
	private static String TAG = "AddItemActivity";
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.additem);
		bundle = this.getIntent().getExtras();
    
		res = getResources(); // Resource object to get drawables
		tabHost = getTabHost();  // The activity TabHost
    
		addSingleActivity();  
		addBatchActivity();  
		addEventActivity();
		
		tabHost.setCurrentTab(0);
	}
	
	public void addSingleActivity() {  
		Intent intent = new Intent();  
	    intent.setClass(AddItemActivity.this, AddSingleTaskActivity.class);
	    int type = bundle.getInt("type");
	    intent.putExtra("type", type);
	    if (type == 0) {
	    	intent.putExtra("proj", (Project)bundle.get("proj"));
        } else {
        	intent.putExtra("taskevent", (TaskEvent)bundle.get("taskevent"));
        	intent.putExtra("projname", bundle.getString("projname"));
        }
	    
	    spec = tabHost.newTabSpec("tab1");  
	    spec.setIndicator("Single", res.getDrawable(R.drawable.icon));  
	    spec.setContent(intent);
	    tabHost.addTab(spec);
	}
	       
	public void addBatchActivity() {  
		Intent intent = new Intent();  
	    intent.setClass(AddItemActivity.this, AddBatchTaskActivity.class);
	    int type = bundle.getInt("type");
	    intent.putExtra("type", type);
	    if (type == 0) {
	    	intent.putExtra("proj", (Project)bundle.get("proj"));
        } else {
        	
        }
	    
	    spec = tabHost.newTabSpec("tab2");  
	    spec.setIndicator("Batch",res.getDrawable(R.drawable.icon));  
	    spec.setContent(intent);          
	    tabHost.addTab(spec);  
	}  
	
	public void addEventActivity(){  
		Intent intent = new Intent();  
		intent.setClass(AddItemActivity.this, AddEventActivity.class);
	    int type = bundle.getInt("type");
	    intent.putExtra("type", type);
	    if (type == 0) {
	    	intent.putExtra("proj", (Project)bundle.get("proj"));
        } else {
        	intent.putExtra("taskevent", (TaskEvent)bundle.get("taskevent"));
        	intent.putExtra("projname", bundle.getString("projname"));
        }
	    
		spec = tabHost.newTabSpec("tab3");  
		spec.setIndicator("Event", res.getDrawable(R.drawable.icon));  
		spec.setContent(intent);          
		tabHost.addTab(spec);  
	}  
  	  
}