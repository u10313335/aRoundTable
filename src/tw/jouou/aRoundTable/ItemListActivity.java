package tw.jouou.aRoundTable;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ItemListActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        findViews();
        
        
    	  ArrayList<HashMap< String,Object>> projects = new ArrayList<HashMap<String, Object>>();
    	  
          for (int i = 0; i < project.length; i++) {
              HashMap<String, Object> projectname = new HashMap<String, Object>();
              projectname.put("img", R.drawable.project);
              projectname.put("projectname", project[i]);
              projects.add(projectname);
          }
          
          SimpleAdapter ImageItems = new SimpleAdapter(
        		  this,projects,
                  R.layout.project,
                  new String[] { "img", "projectname"},
                  new int[] { R.id.img, R.id.name });
      
          
          Mainlist.setAdapter(ImageItems); 
    	
    	
        setListeners();
       
    }
    
    private ListView Mainlist;

    private String[] project = new String[]{"project1","project2","project3","project4","project5","event1","event2","event3","project6","event4","project7","project8","project9"};
    
    protected static final int MENU_Add_item = Menu.FIRST;
    
    protected static final int MENU_Update = Menu.FIRST+1;
    
    protected static final int MENU_Add_group = Menu.FIRST+2;
    
    
    private void findViews(){
    	
    	Mainlist = (ListView)findViewById(R.id.mainlist);

    	
    	 
        
    }
    
    private void setListeners(){
    	
    	Mainlist.setOnItemClickListener( new OnItemClickListener(){
            
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                // TODO Auto-generated method stub
                setTitle( getResources().getString(R.string.title) + ": " + project[arg2]);
            }
        });
    	
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
		
		menu.add(0,MENU_Add_item,0,"").setIcon(R.drawable.add);
		menu.add(0,MENU_Update,0,"").setIcon(R.drawable.update);
		menu.add(0,MENU_Add_group,0,"").setIcon(R.drawable.group);
		
		
		this.openOptionsMenu();
		
		return super.onCreateOptionsMenu(menu);
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
			switch(item.getItemId())
			{
			case MENU_Add_item:
				
				Intent intent= new Intent();
				intent.setClass(ItemListActivity.this,ProjNameActivity.class);
		        startActivity(intent);  //remember to add Activity in AndroidManifest
                
                break;
                
			case MENU_Update:
				
				break;
				
			case MENU_Add_group:
				
				break;
			
			}
			return super.onOptionsItemSelected(item);
		
	}
	
	
	
    
}