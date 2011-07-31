package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.taptwo.android.widget.CircleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;
import org.taptwo.android.widget.ViewFlow.ViewSwitchListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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
	
	private String itemOwners[] = { "小羽、小熊", "albb", "洞洞", "所有人", "小羽、小熊", "albb", "洞洞", "所有人" };  //TODO:dummy test data, remove them ASAP
	private String token;
	private DBUtils dbUtils;
	private List<User> users;
	private List<Project> projs;
	private View lists[]; //list[0] is "all task/events list", the followings(lists[1]~) are "project list"
    private View titleView; //view on the top
    private View mainView;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
	private ViewFlow viewFlow;
	private DiffAdapter adapter;
	private ArrayList allTaskEvents = new ArrayList();
	private int position;  // screen position
	private final String colors[] = { "#FF37FD", "#FA0300", "#F7FA00", "#00DC03", "#0300FA", "#5200A0", "#F8F8F2" };  //TODO:wait to design by bearRu
    protected static final int MENU_Settings = Menu.FIRST;
    protected static final int MENU_Feedbacks = Menu.FIRST+1;
    protected static final int MENU_About = Menu.FIRST+2;
    protected static final int MENU_EditItem = Menu.FIRST;
    protected static final int MENU_DeleteItem = Menu.FIRST+1;
	private static String TAG = "MainActivity";

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
        
    	users = dbUtils.userDelegate.get();
 
    	if(!users.isEmpty()) {
    		token = users.get(0).getToken();
        	dbUtils.close();
         	update();
    	}else {
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
    	}

    	//TODO:get project list from server, used when sync
    	//new GetProjectListTask().execute();
    }
    
    protected void update() {
    	allTaskEvents.clear();
    	if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        titleView = inflater.inflate(R.layout.main_title, null);
    	projs = dbUtils.projectsDelegate.get();
    	if(!projs.isEmpty()) {
    		lists = new View[(projs.size())+1];
    		lists[0] = inflater.inflate(R.layout.all_item_list, null);
    		
    		try {
    			List<TaskEvent> taskevents;
        		taskevents = dbUtils.taskeventsDelegate.get();
        		allTaskEvents.add(taskevents);
        		formAllItemList(lists[0],taskevents);
    		} catch (ParseException e) {
    			Log.v(TAG, "Parse error");
    		}

    		for (int i=1; i < (projs.size())+1; i++) {
    			lists[i] = inflater.inflate(R.layout.project_list, null);
    			formProjLists(lists[i], projs.get(i-1));
    		}
    		mainView = inflater.inflate(R.layout.main, null);
    		setContentView(titleView);
    		addContentView(mainView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
            adapter = new DiffAdapter(this);
            viewFlow.setAdapter(adapter);
            CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
    		viewFlow.setFlowIndicator(indic);
    		viewFlow.setOnViewSwitchListener(new ViewSwitchListener() {
    		    public void onSwitched(View v, int position) {
    		        MainActivity.this.position = position;
    		        //Log.v(TAG, "Now At Screen: "+position);
    		    }
    		});
    	}else {
            Intent addgroup_intent= new Intent();
            addgroup_intent.setClass(MainActivity.this,CreateProjectActivity.class);
            startActivity(addgroup_intent);
    	}
    }
    
    // form all task/event list
    private void formAllItemList(View v, List<TaskEvent> taskevents) {
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    	ArrayList<HashMap <String, Object>> items = new ArrayList<HashMap <String, Object>> ();
    	
    	if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
    	
    	CheckBox itemDone = (CheckBox) v.findViewById(R.id.all_item_done);
		ListView allItemListView = (ListView) v.findViewById(R.id.all_item_list);
		Button btnRefresh = (Button) v.findViewById(R.id.all_item_refresh);
		Button btnAddItem = (Button) v.findViewById(R.id.all_item_add);
		Button btnAddProj = (Button) v.findViewById(R.id.all_item_add_project);
		
	    for (int i=0; i < taskevents.size(); i++) {
	    		Date due = taskevents.get(i).getDue();
	    		String dayDistance = dayDistance(due);
	    		Project proj = dbUtils.projectsDelegate.get((int)taskevents.get(i).getProjId());
	    		HashMap< String, Object > item = new HashMap< String, Object >();
	    		item.put("checkDone", itemDone);
	    		item.put("itemName", taskevents.get(i).getName());
	    		item.put("itemProj", proj.getName());
	    		item.put("taskEventId", taskevents.get(i).getId());
	    		item.put("dueRelateDay", dayDistance);
	    		if (dayDistance == getString(R.string.due_today)) {
	    			item.put("overDue", true);
	    		} else {
	    			item.put("overDue", false);
	    		}
	    		item.put("dueDate", formatter.format(due));
	    		item.put("color", proj.getColor());
	    		items.add(item);
	    	}
		
		allItemListView.setAdapter(new SpecialAdapter(this,items,R.layout.all_item_list_item,
    			new String[] { "checkDone", "itemName", "itemProj", "dueRelateDay", "dueDate" },
    			new int[] { R.id.all_item_done, R.id.all_item_name, R.id.all_item_project, R.id.all_item_due_relate_day, R.id.all_item_duedate }));

    	allItemListView.setOnCreateContextMenuListener(new ListView.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add(Menu.NONE,MENU_EditItem,0,getString(R.string.edit));
				menu.add(Menu.NONE,MENU_DeleteItem,0,getString(R.string.delete));
				menu.setHeaderTitle(getString(R.string.item_operations));
			}
    	});
    	
    	btnRefresh.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			update();
    	    }
    	});
    	
    	btnAddItem.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			String projNames[] = new String[projs.size()];
    			for (int i=1; i < (projs.size())+1; i++) {
    				projNames[i-1] = projs.get(i-1).getName();
    			}
    			Builder dialog = new Builder(MainActivity.this);
    			dialog.setTitle(getString(R.string.select_project));
    			dialog.setSingleChoiceItems(projNames, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent additem_intent= new Intent();
		    	    	additem_intent.putExtra("type", 0); // 0 add, 1 edit
		    	    	additem_intent.putExtra("proj", projs.get(which));
		    	    	additem_intent.setClass(MainActivity.this, AddItemActivity.class);
		    			startActivity(additem_intent);
					}
    			});
    			dialog.show();
    	    }
    	});
    	
    	btnAddProj.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	Intent addgroup_intent= new Intent();
    			addgroup_intent.setClass(MainActivity.this, CreateProjectActivity.class);
    			startActivity(addgroup_intent);
    	    }
    	});
    }
    
    // form task/event list belongs to specific project
	private void formProjLists(View v, Project p){
		final Project proj = p;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		ArrayList<HashMap <String, Object>> items = new ArrayList<HashMap <String, Object>> ();
		List<TaskEvent> taskevents = null;
		
    	if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
    	
    	try {
    		taskevents = dbUtils.taskeventsDelegate.get(proj.getId());
    		allTaskEvents.add(taskevents);
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
    	
		CheckBox chkBoxItemDone = (CheckBox) v.findViewById(R.id.itemDone);
		ListView projItemListView = (ListView) v.findViewById(R.id.proj_item_list);
		TextView txtProjName = (TextView) v.findViewById(R.id.proj_name);
		Button btnIssue = (Button) v.findViewById(R.id.proj_issue_tracker);
		Button btnDocs = (Button) v.findViewById(R.id.proj_docs);
		Button btnAddItem = (Button) v.findViewById(R.id.proj_additem);
		Button btnContacts = (Button) v.findViewById(R.id.proj_contact);
		Button btnChart = (Button) v.findViewById(R.id.proj_chart);	
		txtProjName.setText(p.getName());

	    for (int i=0; i < taskevents.size(); i++) {
	    	Date due = taskevents.get(i).getDue();
	    	String dayDistance = dayDistance(due);
	    	HashMap< String, Object > item = new HashMap< String, Object >();
	    	item.put("checkDone", chkBoxItemDone);
	    	item.put("itemName", taskevents.get(i).getName());
	    	item.put("itemOwner", itemOwners[0]);
	    	item.put("taskEventId", taskevents.get(i).getId());
	    	item.put("dueRelateDay", dayDistance);
    		if (dayDistance == getString(R.string.due_today)) {
    			item.put("overDue", true);
    		} else {
    			item.put("overDue", false);
    		}
	    	item.put("dueDate", formatter.format(due));
	    	item.put("color", proj.getColor());
	    	items.add(item);
	    }
		
		projItemListView.setAdapter(new SpecialAdapter(this,items,R.layout.project_list_item,
    			new String[] { "checkDone", "itemName", "itemOwner", "dueRelateDay", "dueDate" },
    			new int[] { R.id.itemDone, R.id.item_name, R.id.item_owner, R.id.item_dueRelateDay, R.id.item_duedate }));

		projItemListView.setOnCreateContextMenuListener(new ListView.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add(Menu.NONE,MENU_EditItem,0,getString(R.string.edit));
				menu.add(Menu.NONE,MENU_DeleteItem,0,getString(R.string.delete));
				menu.setHeaderTitle(getString(R.string.item_operations));
			}
    	});
    	
		btnIssue.setOnClickListener(new OnClickListener() {
    		@Override
    	    public void onClick(View arg0) {
    	    	// TODO:insert issue tracker activity here
    	    }
    	});
		btnDocs.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    	    	// TODO:insert group docs activity here
    	    }
    	});
		btnAddItem.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	Intent additem_intent= new Intent();
    	    	additem_intent.putExtra("type", 0); // 0 add, 1 edit
    	    	additem_intent.putExtra("proj", proj);
    	    	additem_intent.setClass(MainActivity.this, AddItemActivity.class);
    			startActivity(additem_intent);
    	    }
    	});
		btnContacts.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	// TODO:insert contacts activity here
    	    }
    	});
		btnChart.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	// TODO:insert chart activity here
    	    }
    	});
	}
	
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
        TaskEvent taskevent;
        switch (item.getItemId()) {
        	case MENU_EditItem:
        		taskevent = ((List<TaskEvent>) allTaskEvents.get(position)).get(menuInfo.position);
        		Intent additem_intent= new Intent();
        		additem_intent.setClass(MainActivity.this, AddItemActivity.class);
        		additem_intent.putExtra("type", 1);
        		additem_intent.putExtra("taskevent", taskevent);
        		additem_intent.putExtra("projname", dbUtils.projectsDelegate.get(taskevent.getProjId()).getName());
    			startActivity(additem_intent);
    			break;
        	case MENU_DeleteItem:
        		Log.v(TAG, "position: "+position+", menupo: "+menuInfo.position);
        		taskevent = ((List<TaskEvent>) allTaskEvents.get(position)).get(menuInfo.position);
        		dbUtils.taskeventsDelegate.delete(taskevent);
        		MainActivity.this.update();
        }
    	return super.onContextItemSelected(item); 
    }
	
	private String dayDistance(Date due) {
		Date today = new Date();
		long DAY = 24L * 60L * 60L * 1000L;
		Calendar dueCal = new GregorianCalendar();
		Calendar todayCal = new GregorianCalendar();
		dueCal.setTime(due);
		try {
			todayCal.setTime(formatter.parse(formatter.format(today)));
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
		long dis = (dueCal.getTime().getTime()-todayCal.getTime().getTime()) /DAY;
		long day = todayCal.compareTo(dueCal);
		if(day>0) {
			return getString(R.string.overdue);
		} else if(day<0) {
			return dis+getString(R.string.dayafter);
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
		users = dbUtils.userDelegate.get();
    	if(!users.isEmpty()){
    		update();
    	}
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
    
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		viewFlow.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,MENU_Settings, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0,MENU_Feedbacks, 0, R.string.feedbacks).setIcon(android.R.drawable.ic_menu_send);
		menu.add(0,MENU_About, 0, R.string.about).setIcon(android.R.drawable.ic_menu_help);
		this.openOptionsMenu();
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case MENU_Settings:
				break;
			case MENU_Feedbacks:
				break;
			case MENU_About:
				break;
		}
		return super.onOptionsItemSelected(item);
	};
	
	
	private class SpecialAdapter extends SimpleAdapter {
		ArrayList<HashMap<String, Object>> items;
		int resource;
		public SpecialAdapter(Context context, ArrayList<HashMap<String, Object>> items, int resource, String[] from, int[] to) {
			super(context, items, resource, from, to);
			this.items = items;
			this.resource = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		  View view = super.getView(position, convertView, parent);
		  TextView relateDay = null;
		  TextView color = null;
		  
		  switch(resource) {
		  	case R.layout.all_item_list_item:
		  		relateDay = (TextView) view.findViewById(R.id.all_item_due_relate_day);
		  		color = (TextView) view.findViewById(R.id.all_item_color);
		  		break;	
		  	case R.layout.project_list_item:
		  		relateDay = (TextView) view.findViewById(R.id.item_dueRelateDay);
		  		color = (TextView) view.findViewById(R.id.item_color);
		  }
		  
		  if((Boolean)items.get(position).get("overDue") == true) {
			  relateDay.setTextColor(Color.RED);
		  } else {
			  relateDay.setTextColor(Color.WHITE);
		  }		  
		  color.setBackgroundColor(Color.parseColor(colors[(Integer)items.get(position).get("color")])); 
		  return view;
		}
	}

	
	private class DiffAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public DiffAdapter(Context context) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getItemViewType(int position) {
			return position;
		}

		@Override
		public int getCount() {
			return lists.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = lists[0];
			} 
			convertView = lists[position];
			return convertView;
		}

	}


	//TODO:get project list from server, used when sync
/*	private class GetProjectListTask extends AsyncTask<Void, Void, Project[]> {
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