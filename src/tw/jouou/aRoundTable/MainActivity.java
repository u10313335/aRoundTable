package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import tw.jouou.aRoundTable.view.WorkspaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Main Activity of aRound Table
 * 
 * Shows all tasks and can switch to different views
 */
public class MainActivity extends Activity {
	
	private String itemNames[] = { "Introduction", "Class Diagram", "SA ppt", "Demo", "Introduction", "Class Diagram", "SA ppt", "Demo" };
	private String itemOwners[] = { "小羽、小熊", "albb", "洞洞", "所有人", "小羽、小熊", "albb", "洞洞", "所有人" };
	private String dueRelateDays[] = { "今天", "二天後", "十天後", "十七天後", "今天", "二天後", "十天後", "十七天後" };
	private String dueDates[] = { "2011/03/05", "2011/03/07", "2011/03/14", "2011/03/21", "2011/03/05", "2011/03/07", "2011/03/14", "2011/03/21" };
	private String token;
	private DBUtils dbUtils;
	private List<User> users;
	private List<Project> projs;
	private View projLists[];
    protected static final int MENU_Settings = Menu.FIRST;
    protected static final int MENU_Feedbacks = Menu.FIRST+1;
    protected static final int MENU_About = Menu.FIRST+2;
	private static String TAG = "MainActivity";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getSystemService (Context.LAYOUT_INFLATER_SERVICE);

        WorkspaceView work = new WorkspaceView(this, null);
      	work.setTouchSlop(32);
        
    	if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
    	
    	users = dbUtils.userDelegate.get();
    	projs = dbUtils.projectsDelegate.get();

    	// Form each project lists
    	if(!projs.isEmpty()) {
    		projLists = new View[projs.size()];
    		for (int i=0; i < projs.size(); i++){
    			projLists[i] = inflater.inflate(R.layout.project_list, null);
    			work.addView(projLists[i]);
    			formLists(projLists[i], projs.get(i));
    		}
        	// Add workspace to current content view
    		setContentView(work);
    	}else {
    		setContentView(R.layout.main);
    		Builder dialog = new Builder(MainActivity.this);
    	    dialog.setTitle(R.string.create_project_message_title);
    	    dialog.setMessage(R.string.create_project_message);
        	dialog.setPositiveButton(R.string.confirm,
        		new DialogInterface.OnClickListener() {
        	    	public void onClick(DialogInterface dialoginterface, int i){
        	    		dialoginterface.dismiss();
            	    	Intent addgroup_intent= new Intent();
            			addgroup_intent.setClass(MainActivity.this,CreateProjectActivity.class);
            			startActivity(addgroup_intent);
        	    	}
        	    }
        	);
        	dialog.show();
    	}
	
    	if(!users.isEmpty()){
    		token = users.get(0).getToken();
        	dbUtils.close();
    	}else{
    		Builder dialog = new Builder(MainActivity.this);
    	    dialog.setTitle(R.string.welcome_message_title);
    	    dialog.setMessage(R.string.welcome_message);
        	dialog.setPositiveButton(R.string.confirm,
        		new DialogInterface.OnClickListener() {
        	    	public void onClick(DialogInterface dialoginterface, int i){
        	    		dialoginterface.dismiss();
        	    		initAcc();
        	    	}
        	    }
        	);
        	dialog.show();
    	}
    	// TODO:get project list from server, used when sync
    	//new GetProjectListTask().execute();
}
    
	private void formLists(View v, Project proj) {
		final String projname = proj.getName();
		final long projId = proj.getServerId();
		CheckBox itemDone = (CheckBox) v.findViewById(R.id.itemDone);
		ListView itemListView = (ListView) v.findViewById(R.id.proj_item_list);
		TextView projNameView = (TextView) v.findViewById(R.id.proj_name);
		Button issueTracker = (Button) v.findViewById(R.id.proj_issue_tracker);
		Button docs = (Button) v.findViewById(R.id.proj_docs);
		Button addItem = (Button) v.findViewById(R.id.proj_additem);
		Button contacts = (Button) v.findViewById(R.id.proj_contact);
		Button chart = (Button) v.findViewById(R.id.proj_chart);
		
		projNameView.setText(projname);
		
    	// Put items for specific project to an array list
		ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap <String, Object>> ();
    	for (int i=0; i < itemNames.length; i++) {
    		HashMap< String, Object > item = new HashMap< String, Object >();
    		item.put("checkDone", itemDone);
    		item.put("itemName", itemNames[i]);
    		item.put("itemOwner", itemOwners[i]);
    		item.put("dueRelateDay", dueRelateDays[i]);
    		item.put("dueDate", dueDates[i]);
    		items.add(item);
    	}
		
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
    	    	additem_intent.putExtra("projname", projname);
    	    	additem_intent.putExtra("projid", projId);
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

    // FIXME: duplicate notification after registration 
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
	 	Uri uri = intent.getData();
	 	token = uri.getQueryParameter("");
	    User user = new User(token);
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

	// TODO:get project list from server, used when sync
	/*private class GetProjectListTask extends AsyncTask<Void, Void, Project[]> {
		private Dialog dialog;
		private Exception exception;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(MainActivity.this);
			dialog.show();
		}
		
		@Override
		protected Project[] doInBackground(Void... params) {
			try {				
				return ArtApi.getInstance(MainActivity.this).getProjectList();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerException e) {
				exception = e;				
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
        protected void onPostExecute(Project[] projects) {
			dialog.dismiss();
			
			if(exception instanceof ServerException) {
				Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			
			if(projects == null)
				return;
			
			projName = new String[projects.length];
			
			for(int i=0; i < projects.length; i++) {
				projName[i] = projects[i].getName();
			}
			// Set project name on top
			projNameView.setText(projName[0]);
		}
	}*/
	
}