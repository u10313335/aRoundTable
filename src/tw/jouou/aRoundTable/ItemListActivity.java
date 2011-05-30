package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.*;
import tw.jouou.aRoundTable.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ItemListActivity extends Activity {
    /** Called when the activity is first created. */
	
    private ListView mainList;
    private SimpleAdapter imageItems;
    private String[] project = new String[]{"project1","project2","project3","project4","project5","event1","event2","event3","project6","event4","project7","project8","project9"};
	private DBUtils dbUtils;
	private List<User> users;
	private User user;
	private String token;
	
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
//    	System.out.println(users.isEmpty());
    	
    	if(!users.isEmpty()){
    		token = users.get(0).getToken();
        	dbUtils.close();
    	}else{
    		Builder dialog = new Builder(ItemListActivity.this);
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
    	
/*			HttpGet request = new HttpGet("http://api.hime.loli.tw/projects");
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			try {
				HttpResponse response = new DefaultHttpClient().execute(request);
				if(response.getStatusLine().getStatusCode() == 200){
					String get_json = EntityUtils.toString(response.getEntity());
					JSONObject json1 = new JSONObject(post_json);
					JSONObject json2 = new JSONObject(json1.getString("project"));
					String result = json2.getString("name");
					Toast.makeText(ItemListActivity.this, get_json, Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {

				Toast.makeText(ItemListActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
*/
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

    }

    
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
	 	Uri uri = intent.getData();
	 	token = uri.getQueryParameter("");
	    user = new User();
	    user.setToken(token);
		dbUtils.userDelegate.insert(user);
		dbUtils.close();
		Toast popup =  Toast.makeText(this,R.string.register_finished, Toast.LENGTH_SHORT);
	    popup.show();
		//Log.i("Sol","it's about to finish");
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case MENU_Add_item:
				
				Intent additem_intent= new Intent();
				additem_intent.setClass(ItemListActivity.this,AddItemActivity.class);
		        startActivity(additem_intent);  //remember to add Activity in AndroidManifest
		        
		        break;
			case MENU_Update:
				
				break;
				
			case MENU_Add_group:
				
				Intent addgroup_intent= new Intent();
				addgroup_intent.setClass(ItemListActivity.this,ProjNameActivity.class);
		        startActivity(addgroup_intent);  //remember to add Activity in AndroidManifest
		        
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}