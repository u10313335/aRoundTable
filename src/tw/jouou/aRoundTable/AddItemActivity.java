package tw.jouou.aRoundTable;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class AddItemActivity extends TabActivity{
	
	private Bundle bundle;
	private String projName;
	private long projId;
	private long projserverid;
	private TabHost tabHost;
	private TabHost.TabSpec spec;
	private Resources res;
	
	
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
    setContentView(R.layout.additem);
    
    bundle = this.getIntent().getExtras();
    projName = bundle.getString("projname");
    projId = bundle.getLong("projid");
    projserverid = bundle.getLong("projserverid");
    
    res = getResources(); // Resource object to get Drawables
    tabHost = getTabHost();  // The activity TabHost
    
    
    addSingleActivity();  
    addBatchActivity();  
    addEventActivity();  
     
    tabHost.setCurrentTab(0);
	 
 
}
	
	 public void addSingleActivity(){  
	        Intent intent1 = new Intent();  
	        intent1.setClass(AddItemActivity.this, AddSingleActivity.class);  
	           
	        spec = tabHost.newTabSpec("tab1");  
	        spec.setIndicator("Single", res.getDrawable(R.drawable.icon));  
	        spec.setContent(intent1);          
	        tabHost.addTab(spec);  
	    }  
	       
	    public void addBatchActivity(){  
	    	Intent intent2 = new Intent();  
	        intent2.setClass(AddItemActivity.this, AddBatchActivity.class);  
	           
	        spec = tabHost.newTabSpec("tab2");  
	        spec.setIndicator("Batch",res.getDrawable(R.drawable.icon));  
	        spec.setContent(intent2);          
	        tabHost.addTab(spec);  
	    }  
	    public void addEventActivity(){  
	    	Intent intent3 = new Intent();  
	        intent3.setClass(AddItemActivity.this, AddEventActivity.class);  
	           
	        spec = tabHost.newTabSpec("tab3");  
	        spec.setIndicator("Event", res.getDrawable(R.drawable.icon));  
	        spec.setContent(intent3);          
	        tabHost.addTab(spec);  
	    }  
  	  
}