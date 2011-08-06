package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.util.DBUtils;
import tw.jouou.aRoundTable.view.WorkspaceView;
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Main Activity of aRound Table
 * 
 * Shows all tasks and can switch to different views
 */
public class MainActivity extends Activity {
	
	private ArrayList<HashMap<String, Object>> items;
	private String projName = "SA Project";
	private String itemNames[] = { "Introduction", "Class Diagram", "SA ppt", "Demo", "Introduction", "Class Diagram", "SA ppt", "Demo" };
	private String itemOwners[] = { "小羽、小熊", "albb", "洞洞", "所有人", "小羽、小熊", "albb", "洞洞", "所有人" };
	private String dueRelateDays[] = { "今天", "二天後", "十天後", "十七天後", "今天", "二天後", "十天後", "十七天後" };
	private String dueDates[] = { "2011/03/05", "2011/03/07", "2011/03/14", "2011/03/21", "2011/03/05", "2011/03/07", "2011/03/14", "2011/03/21" };
	
	private CheckBox itemDone;
	private TextView projNameView;
	private ListView itemListView;
	private Button issueTracker, docs, addItem, contacts, chart;
	
	private DBUtils dbUtils;
	private List<User> users;
	private User user;
	private String token;
	private static String TAG = "MainActivity";

	
    protected static final int MENU_Settings = Menu.FIRST;
    protected static final int MENU_Feedbacks = Menu.FIRST+1;
    protected static final int MENU_About = Menu.FIRST+2;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getSystemService (Context.LAYOUT_INFLATER_SERVICE);

		WorkspaceView work = new WorkspaceView(this, null);
		work.setTouchSlop(32);


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


			// TODO:implement get list API below
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
    	
 	  	 		// TODO: Dummy code for a fake list, should be removed ASAP.
    			// Put items for specific project to an array list
    			items = new ArrayList<HashMap <String, Object>> ();
    	    
    			for (int i = 0; i < itemNames.length; i++) {
    				HashMap< String, Object > item = new HashMap< String, Object >();
    				item.put("checkDone", itemDone);
    				item.put("itemName", itemNames[i]);
    				item.put("itemOwner", itemOwners[i]);
    				item.put("dueRelateDay", dueRelateDays[i]);
    				item.put("dueDate", dueDates[i]);
    				items.add(item);
    			}
    	  
    			// Find all views
    			// v1:each project
    			// TODO:v2:other views
    			View v1= inflater.inflate(R.layout.project_list, null, false);
    			View v2= inflater.inflate(R.layout.main, null, false);
    	    
    			// Add views to the workspace view
    			work.addView(v1);
    			work.addView(v2);
    	    
    			// Add workspace to current content view
    			setContentView(work);
    	    
    			// Find Widgets
    			findViews();
    	    
    			// Put items for specific project to list
    			itemListView.setAdapter (new SimpleAdapter(this,items,R.layout.project_list_item,
    					new String[] { "checkDone", "itemName", "itemOwner", "dueRelateDay", "dueDate" },
    					new int[] { R.id.itemDone, R.id.item_name, R.id.item_owner, R.id.item_dueRelateDay, R.id.item_duedate }));
    	  
    			// Long click. You can add item operations here
    			itemListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
    				public boolean onItemLongClick(AdapterView<?> arg0P, View arg1P, int arg2P, long arg3P) {
    					Toast toast = Toast.makeText(MainActivity.this, "Long Click......", Toast.LENGTH_SHORT);
    					toast.show();
    					return true;
    				}
    			});
    	    
    			// Set project name on top
    			projNameView.setText(projName);
    			
    			// Set bottom menu functions
    			issueTracker.setOnClickListener(new OnClickListener(){
    	    		@Override
    	    		public void onClick(View arg0) {
    	    			// TODO:insert issue tracker activity here
    	    			// add project activity, temporarily placed here
    	    			Intent addgroup_intent= new Intent();
    					addgroup_intent.setClass(MainActivity.this,CreateProjectActivity.class);
    			        startActivity(addgroup_intent);
    	    		}
    	        });
    			docs.setOnClickListener(new OnClickListener(){
    	    		@Override
    	    		public void onClick(View arg0) {
    	    			// TODO:insert group docs activity here
    	    		}
    	        });
    			addItem.setOnClickListener(new OnClickListener(){
    	    		@Override
    	    		public void onClick(View arg0) {
    					Intent additem_intent= new Intent();
    					additem_intent.setClass(MainActivity.this,AddItemActivity.class);
    			        startActivity(additem_intent);
    	    		}
    	        });
    			contacts.setOnClickListener(new OnClickListener(){
    	    		@Override
    	    		public void onClick(View arg0) {
    	    			// TODO:insert contacts activity here
    	    		}
    	        });
    			chart.setOnClickListener(new OnClickListener(){
    	    		@Override
    	    		public void onClick(View arg0) {
    	    			// TODO:insert chart activity here
    	    		}
    	        });

    }
    
	private void findViews()
	{
		itemListView = (ListView) findViewById(R.id.proj_item_list);
		projNameView = (TextView) findViewById(R.id.proj_name);
		issueTracker = (Button) findViewById(R.id.proj_issue_tracker);
		docs = (Button) findViewById(R.id.proj_docs);
		addItem = (Button) findViewById(R.id.proj_additem);
		contacts = (Button) findViewById(R.id.proj_contact);
		chart = (Button) findViewById(R.id.proj_chart);
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
		Uri uri = Uri.parse(ArtApi.getLoginUrl());
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }

    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,MENU_Settings, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0,MENU_Feedbacks, 0, R.string.feedbacks).setIcon(android.R.drawable.ic_menu_send);
		menu.add(0,MENU_About, 0, R.string.about).setIcon(android.R.drawable.ic_menu_help);
		
		this.openOptionsMenu();
		
		return super.onCreateOptionsMenu(menu);
	}

}