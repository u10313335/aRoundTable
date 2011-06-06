package tw.jouou.aRoundTable;

import java.util.List;

import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Main Activity of aRound Table
 * 
 * Shows all tasks and can switch to different views
 */
public class MainActivity extends Activity {
	private DBUtils dbUtils;
	private List<User> users;
	private User user;
	private String token;
	private static String TAG = "MainActivity";
	
    protected static final int MENU_Add_item = Menu.FIRST;
    protected static final int MENU_Update = Menu.FIRST+1;
    protected static final int MENU_Add_group = Menu.FIRST+2;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    	if (dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
    	
    	users = dbUtils.userDelegate.get();
    	
    	if(!users.isEmpty()){
    		token = users.get(0).getToken();
        	dbUtils.close();
    	}else{
    		Builder dialog = new Builder(MainActivity.this);
    	    dialog.setTitle(R.string.welcome_message_title);
    	    dialog.setMessage(R.string.welcome_message);
        	dialog.setPositiveButton(R.string.confirm,
        		new DialogInterface.OnClickListener(){
        	    	public void onClick(DialogInterface dialoginterface, int i){
        	    		initAcc();
        	    	}
        	    }
        	);
    	    dialog.show();
    	}
  	  	/*
  	  	 * TODO: Dummy code for a fake list, should be removed ASAP.
  	  	ArrayList<HashMap<String,Object>> projects = new ArrayList<HashMap<String, Object>>();
  	  

  	  	for (int i = 0; i < project.length; i++) {
            HashMap<String, Object> projectname = new HashMap<String, Object>();
            projectname.put("img", R.drawable.project);
            projectname.put("projectname", project[i]);
            projects.add(projectname);
        }
        
        imageItems = new SimpleAdapter(this,projects,R.layout.project,
        	new String[] {"img", "projectname"},new int[] {R.id.img, R.id.name});
  	  	 
        
        mainList = (ListView)findViewById(R.id.mainlist);
        mainList.setAdapter(imageItems); 
        mainList.setOnItemClickListener(
        	new OnItemClickListener(){  
        		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3){
        			setTitle(getResources().getString(R.string.title) + ": " + project[arg2]);
        		}
        	}
        );
  	  	 */
    }

    
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
	 	Uri uri = intent.getData();
	 	token = uri.getQueryParameter("");
	    user = new User();
	    user.setToken(token);
		dbUtils.userDelegate.insert(user);
		dbUtils.close();
		Toast.makeText(this, R.string.register_finished, Toast.LENGTH_SHORT).show();
	    Log.v(TAG, "[onNewIntent] Token back: "+token);
	}
 
    
    public void initAcc(){
		Uri uri = Uri.parse("http://api.hime.loli.tw/login");
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }

    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,MENU_Add_item,0,"").setIcon(R.drawable.add);
		menu.add(0,MENU_Update,0,"").setIcon(R.drawable.update);
		menu.add(0,MENU_Add_group,0,"").setIcon(R.drawable.group);
		
		this.openOptionsMenu();
		
		return super.onCreateOptionsMenu(menu);
	}
	

	@Override
	/*
	 * TODO: Move option menu to real menu
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case MENU_Add_item:
                break;

			case MENU_Update:
				
				break;
				
			case MENU_Add_group:
		        startActivity(new Intent(this, CreateProjectActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}