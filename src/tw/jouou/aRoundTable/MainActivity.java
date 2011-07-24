package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import tw.jouou.aRoundTable.view.WorkspaceView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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
	
	// TODO:dummy test data
	private String itemOwners[] = { "小羽、小熊", "albb", "洞洞", "所有人", "小羽、小熊", "albb", "洞洞", "所有人" };
	private String projNames[];
	private String token;
	private DBUtils dbUtils;
	private List<User> users;
	private List<Project> projs;
	private View lists[];
	private Date today = new Date();  //today
	private WorkspaceView work;
    protected static final int MENU_Settings = Menu.FIRST;
    protected static final int MENU_Feedbacks = Menu.FIRST+1;
    protected static final int MENU_About = Menu.FIRST+2;
    protected static final int MENU_EditItem = Menu.FIRST;
    protected static final int MENU_DeleteItem = Menu.FIRST+1;
	private static String TAG = "MainActivity";
	//private int[] lights = new int[3];
    private View addView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
/*        if(dbUtils == null) {
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
        		new DialogInterface.OnClickListener() {
        	    	public void onClick(DialogInterface dialoginterface, int i) {
        	    		dialoginterface.dismiss();
        	    		initAcc();
        	    	}
        	    }
        	);
        	dialog.show();
    	}*/
     	update();

    	// TODO:get project list from server, used when sync
    	//new GetProjectListTask().execute();
    }
    
    protected void update() {
    	// Form each project lists
    	if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addView = inflater.inflate(R.layout.main, null); 
        work = new WorkspaceView(this, null);
      	work.setTouchSlop(32);
    	projs = dbUtils.projectsDelegate.get();
    	if(!projs.isEmpty()) {
    		lists = new View[(projs.size())+1];
    		lists[0] = inflater.inflate(R.layout.all_item_list, null);
    		projNames = new String[projs.size()];
    		work.addView(lists[0]);
    		try {
        		formAllItemList(lists[0]);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		//lights[0] = R.drawable.add;
    		//lights[1] = R.drawable.date;
    		//lights[2] = R.drawable.group;
    		for (int i=1; i < (projs.size())+1; i++) {
    			lists[i] = inflater.inflate(R.layout.project_list, null);
    			work.addView(lists[i]);
//    			Log.v(TAG, Integer.toString(work.getScreenForView(lists[i])));
    			try {
					formProjLists(lists[i], projs.get(i-1));
					projNames[i-1] = projs.get(i-1).getName();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
        	// Add workspace to current content view
    		//setContentView(work);
    		setContentView(addView);
    		addContentView(work, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
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
    }
    
    private void formAllItemList(View v) throws ParseException {
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    	ArrayList<HashMap <String, Object>> items = null;
    	List<TaskEvent> taskevents = null;
    	
    	if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
    	
    	try {
    		taskevents = dbUtils.taskeventsDelegate.get();
			//Log.v(TAG, "projId= "+proj.getId());
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			Log.v(TAG, "Parse error");
		}
    	
    	CheckBox itemDone = (CheckBox) v.findViewById(R.id.all_item_done);
		ListView itemListView = (ListView) v.findViewById(R.id.all_item_list);
		Button refresh = (Button) v.findViewById(R.id.all_item_refresh);
		Button addItem = (Button) v.findViewById(R.id.all_item_add);
		Button addProject = (Button) v.findViewById(R.id.all_item_add_project);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View all_item_list = inflater.inflate(R.layout.all_item_list_item, null); 
		//TextView itemDueRelateDay = (TextView) itemListView.findViewById(R.id.all_item_due_relate_day);
		
		if(taskevents != null) {
			items = new ArrayList<HashMap <String, Object>> ();
	    	for (int i=0; i < taskevents.size(); i++) {
	    			Date due = taskevents.get(i).getDue();
	    			String dayDistance = dayDistance(today, due);
	    			HashMap< String, Object > item = new HashMap< String, Object >();
	    			item.put("checkDone", itemDone);
	    			item.put("itemName", taskevents.get(i).getName());
	    			item.put("itemProj", projs.get((int)taskevents.get(i).getProjId()-1).getName());
	    			item.put("dueRelateDay", dayDistance);
	    			if (dayDistance == getString(R.string.due_today)) {
	    				item.put("overDue", true);
	    			} else {
	    				item.put("overDue", false);
	    			}
	    			item.put("dueDate", formatter.format(due));
	    			items.add(item);
	    	}
		}else {
			setContentView(R.layout.main);
			return;
		}
		
		// Put items for specific project to list
    	itemListView.setAdapter (new SpecialAdapter(this,items,R.layout.all_item_list_item,
    			new String[] { "checkDone", "itemName", "itemProj", "dueRelateDay", "dueDate" },
    			new int[] { R.id.all_item_done, R.id.all_item_name, R.id.all_item_project, R.id.all_item_due_relate_day, R.id.all_item_duedate }));

    	itemListView.setOnCreateContextMenuListener(new ListView.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add(Menu.NONE,MENU_EditItem,0,getString(R.string.edit));
				menu.add(Menu.NONE,MENU_DeleteItem,0,getString(R.string.delete));
			}
    	});
    	
    	// Set bottom menu functions
    	refresh.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			update();
    	    }
    	});
    	
    	addItem.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			Builder dialog = new Builder(MainActivity.this);
    			dialog.setTitle(getString(R.string.select_proj));
    			dialog.setSingleChoiceItems(projNames, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent additem_intent= new Intent();
		    	    	additem_intent.putExtra("projname", projNames[which]);
		    	    	additem_intent.putExtra("projid", projs.get(which).getId());
		    	    	additem_intent.putExtra("projserverid", projs.get(which).getServerId());
		    	    	additem_intent.setClass(MainActivity.this, AddItemActivity.class);
		    			startActivity(additem_intent);
					}
    			});
    			dialog.show();
    	    }
    	});
    	
    	addProject.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	Intent addgroup_intent= new Intent();
    			addgroup_intent.setClass(MainActivity.this, CreateProjectActivity.class);
    			startActivity(addgroup_intent);
    	    }
    	});
    }
    
	private void formProjLists(View v, Project proj) throws IllegalArgumentException, ParseException {
		final String projName = proj.getName();
		final long projId = proj.getId();
		//Log.v(TAG, Long.toString(projId));
		final long projServerId = proj.getServerId();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		ArrayList<HashMap <String, Object>> items = null;
		List<TaskEvent> taskevents = null;
		
    	if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
    	
    	try {
    		taskevents = dbUtils.taskeventsDelegate.get(proj.getId());
			//Log.v(TAG, "projId= "+proj.getId());
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			Log.v(TAG, "Parse error");
		}
    	
		CheckBox itemDone = (CheckBox) v.findViewById(R.id.itemDone);
		ListView itemListView = (ListView) v.findViewById(R.id.proj_item_list);
		TextView projNameView = (TextView) v.findViewById(R.id.proj_name);
		Button issueTracker = (Button) v.findViewById(R.id.proj_issue_tracker);
		Button docs = (Button) v.findViewById(R.id.proj_docs);
		Button addItem = (Button) v.findViewById(R.id.proj_additem);
		Button contacts = (Button) v.findViewById(R.id.proj_contact);
		Button chart = (Button) v.findViewById(R.id.proj_chart);
		
		/*Button light = (Button) addView.findViewById(R.id.light);
		
		Resources resources = MainActivity.this.getResources();
        Drawable drawable = resources.getDrawable(lights[((int)projId)-1]); 
        
		light.setBackgroundDrawable(drawable);*/
		
		projNameView.setText(projName);
		
		// Put items for specific project to an array list
		if(taskevents != null) {
			items = new ArrayList<HashMap <String, Object>> ();
	    	for (int i=0; i < taskevents.size(); i++) {
	    		Date due = taskevents.get(i).getDue();
	    		String dayDistance = dayDistance(today, due);
	    		HashMap< String, Object > item = new HashMap< String, Object >();
	    		item.put("checkDone", itemDone);
	    		item.put("itemName", taskevents.get(i).getName());
	    		item.put("itemOwner", itemOwners[0]);
	    		item.put("dueRelateDay", dayDistance);
    			if (dayDistance == getString(R.string.due_today)) {
    				item.put("overDue", true);
    			} else {
    				item.put("overDue", false);
    			}
	    		item.put("dueDate", formatter.format(due));
	    		items.add(item);
	    	}
		}else {
			setContentView(R.layout.main);
			return;
		}
		
    	// Put items for specific project to list
    	itemListView.setAdapter (new SpecialAdapter(this,items,R.layout.project_list_item,
    			new String[] { "checkDone", "itemName", "itemOwner", "dueRelateDay", "dueDate" },
    			new int[] { R.id.itemDone, R.id.item_name, R.id.item_owner, R.id.item_dueRelateDay, R.id.item_duedate }));

    	itemListView.setOnCreateContextMenuListener( new ListView.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add(Menu.NONE,MENU_EditItem,0,getString(R.string.edit));
				menu.add(Menu.NONE,MENU_DeleteItem,0,getString(R.string.delete));
			}
    	});
    	
    	// Set bottom menu functions
    	issueTracker.setOnClickListener(new OnClickListener() {
    		@Override
    	    public void onClick(View arg0) {
    	    	// TODO:insert issue tracker activity here
    	    }
    	});
    	docs.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    	    	// TODO:insert group docs activity here
    	    }
    	});
    	addItem.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	Intent additem_intent= new Intent();
    	    	additem_intent.putExtra("projname", projName);
    	    	additem_intent.putExtra("projid", projId);
    	    	additem_intent.putExtra("projserverid", projServerId);
    	    	additem_intent.setClass(MainActivity.this, AddItemActivity.class);
    			startActivity(additem_intent);
    	    }
    	});
    	contacts.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	// TODO:insert contacts activity here
    	    }
    	});
    	chart.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	// TODO:insert chart activity here
    	    }
    	});
	}
	
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo =(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	Toast.makeText(MainActivity.this,String.valueOf(menuInfo.position), Toast.LENGTH_LONG).show();
    	return super.onContextItemSelected(item); 
    }
	
	private String dayDistance(Date today, Date due) {
		Calendar c1 = new GregorianCalendar();
		Calendar c2 = new GregorianCalendar();
		c1.setTime(due);
		c2.setTime(today);
		long day = c1.get(Calendar.DAY_OF_MONTH)-c2.get(Calendar.DAY_OF_MONTH);
		if(day>0) {
			return day+getString(R.string.dayafter);
		} else if(day<0) {
			return getString(R.string.overdue);
		} else {
			return getString(R.string.due_today);
		}
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
    
    @Override
	public void onResume() {
		super.onResume();
		if (dbUtils == null) {
			dbUtils = new DBUtils(this);
		}
		update();
    }
    
	@Override
	public void onStop() {
		super.onStop();
		if (dbUtils != null) {
			dbUtils.close();
			dbUtils = null;
		}
	}
 
    public void initAcc(){
		Uri uri = Uri.parse("http://api.hime.loli.tw/login");
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
	
	private class SpecialAdapter extends SimpleAdapter {
		ArrayList<HashMap<String, Object>> items;
		int resource;
		public SpecialAdapter(Context context, ArrayList<HashMap<String, Object>> items, int resource, /*boolean[] overdue,*/ String[] from, int[] to) {
			super(context, items, resource, from, to);
			this.items = items;
			this.resource = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		  View view = super.getView(position, convertView, parent);
		  TextView relateDay = null;
		  
		  switch(resource) {
		  	case R.layout.all_item_list_item:
		  		relateDay = (TextView) view.findViewById(R.id.all_item_due_relate_day);
		  		break;
		  		
		  	case R.layout.project_list_item:
		  		relateDay = (TextView) view.findViewById(R.id.item_dueRelateDay);		  
		  }
		  
		  if((Boolean)items.get(position).get("overDue") == true) {
			  relateDay.setTextColor(Color.RED);
		  } else {
			  relateDay.setTextColor(Color.WHITE);
		  }
		  return view;
		}
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