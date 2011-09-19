package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.GroupDoc;
import tw.jouou.aRoundTable.bean.Notification;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.lib.SyncService;
import tw.jouou.aRoundTable.util.DBUtils;

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
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Main Activity of aRound Table
 * 
 * Shows all tasks and can switch to different views
 */
public class MainActivity extends Activity {
	
	private int mUnReadCount;
	private DBUtils dbUtils;
	private List<Project> projs;
	private LayoutInflater mInflater;
	private View lists[]; //list[0] is "notifications", list[1] is "all task/events list",
    private View mainView; // the followings(lists[2]~) are "project list"
    private TextView txTitle;
    private ImageView ivUnreadIndicator;
    private TextView txUnread;
    private TextView txLastUpdate;
	private ViewFlow viewFlow;
	private DiffAdapter adapter;
	private SharedPreferences mPrefs;
	private ArrayList<List<TaskEvent>> mAllTaskEvents = new ArrayList<List<TaskEvent>>();
	private int position = 1;  // screen position
	private final String colors[] = { "#00B0CF", "#A2CA30", "#F2E423",
			"#CA4483", "#E99314", "#C02B20", "#F7F7CF", "#225DAB" };
	private DataReceiver dataReceiver;
	private IntentFilter filter;
	protected static final int MENU_EditProj = Menu.FIRST;
	protected static final int MENU_QuitProj = Menu.FIRST+1;
	protected static final int MENU_ViewFinished = Menu.FIRST+2;
    protected static final int MENU_Settings = Menu.FIRST+3;
    protected static final int MENU_Feedbacks = Menu.FIRST+4;
    protected static final int MENU_About = Menu.FIRST+5;
    protected static final int MENU_EditItem = Menu.FIRST;
    protected static final int MENU_DeleteItem = Menu.FIRST+1;
    private static final int TASK = 0;
    private static final int EVENT = 1;
    private static final int ADD_ITEM = 0;
    private static final int EDIT_ITEM = 1;
    private static final int REQUEST_AUTH = 1;
	private static String TAG = "MainActivity";

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
    	mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	dataReceiver = new DataReceiver();
    	filter = new IntentFilter();
        filter.addAction("tw.jouou.aRoundTable.MainActivity"); 
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
    	switch (requestCode) {
		case REQUEST_AUTH:
			if(resultCode == RESULT_CANCELED)
				finish();
			break;
		default:
			break;
		}
    }
    
    protected void update() {
    	mAllTaskEvents.clear();
		mainView = mInflater.inflate(R.layout.main, null);
		txTitle = (TextView) mainView.findViewById(R.id.main_title);
		setContentView(mainView);

    	try {
			projs = dbUtils.projectsDelegate.get();
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
    	if(!projs.isEmpty()) {
    		lists = new View[(projs.size())+2];
    		lists[0] = mInflater.inflate(R.layout.notification, null);
    		lists[1] = mInflater.inflate(R.layout.all_item_list, null);
    		try {
    			List<TaskEvent> taskevents;
        		taskevents = dbUtils.taskEventDelegate.get();
        		mAllTaskEvents.add(taskevents);
        		formNotification(lists[0]);
        		formAllItemList(lists[1],taskevents);
        		ivUnreadIndicator = (ImageView) mainView.findViewById(R.id.main_unread_indicator);
        		txUnread = (TextView) mainView.findViewById(R.id.main_unread_count);
        		if(mUnReadCount > 0) {
        			txUnread.setText(""+mUnReadCount);
        			ivUnreadIndicator.setImageResource(R.drawable.has_unread);
        		}	
    		} catch (ParseException e) {
    			Log.v(TAG, "Parse error");
    		}
    		for (int i=2; i < (projs.size())+2; i++) {
    			lists[i] = mInflater.inflate(R.layout.project, null);
    			formProjLists(lists[i], projs.get(i-2));
    		} 		
    		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
            adapter = new DiffAdapter(this);
            viewFlow.setAdapter(adapter);
            CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
    		viewFlow.setFlowIndicator(indic);
    		viewFlow.setOnViewSwitchListener(new ViewSwitchListener() {
    		    public void onSwitched(View v, int position) {
    		        MainActivity.this.position = position;
    		        if(position==0) {
    		        	txTitle.setText(getString(R.string.notification));
    		        	txTitle.setTextColor(Color.parseColor("#F6F6F7"));
    		        } else if(position==1) {
    		        	txTitle.setText(getString(R.string.all_item_event));
    		        	txTitle.setTextColor(Color.parseColor("#F6F6F7"));
    		        } else {
    		        	txTitle.setText("「" + projs.get(position-2).getName() + "」的工作");
    		        	txTitle.setTextColor(Color.parseColor(colors[projs.get(position-2).getColor()]));
    		        }
    		    }
    		});
    		getLastUpdate();
			viewFlow.setSelection(position);
    	}
    }
    
    private void formNotification(View v) {
    	ExpandableListView notificationView = (ExpandableListView) v.findViewById(R.id.dynamic_issue_list);
    	List<Notification> notifications = dbUtils.notificationDelegate.get();
    	mUnReadCount = 0;
    	List<HashMap <String, String>> groups = new ArrayList<HashMap <String, String>>();
    	List<List <HashMap<String, String>>> childs = new ArrayList<List <HashMap<String, String>>>();
	    for (int i=0; i < notifications.size(); i++) {
	    	HashMap<String, String> group = new HashMap<String, String>();
	    	group.put("g", notifications.get(i).getMessage());
	    	if(!notifications.get(i).getRead()) {
	    		mUnReadCount+=1;
	    	}
	    	groups.add(group);
	        List<HashMap <String, String>> child = new ArrayList<HashMap <String, String>>();
	        HashMap<String, String> childdata = new HashMap<String, String>();
	        childdata.put("c", "回應");
	        child.add(childdata);
	        childs.add(child);
	    }
        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(this, groups, R.layout.notification_item, new String[] { "g" }, new int[] { R.id.notificaton_context }, childs, android.R.layout.simple_expandable_list_item_2, new String[] { "c" }, new int[] { android.R.id.text1});
        notificationView.setAdapter(adapter);
    }
    
    // form all task/event list
    private void formAllItemList(View v, List<TaskEvent> taskevents) {
    	ArrayList<HashMap <String, Object>> items = new ArrayList<HashMap <String, Object>> ();
    	CheckBox itemDone = null;
		ListView allItemListView = (ListView) v.findViewById(R.id.all_item_list);
		ImageView btnRefresh = (ImageView) v.findViewById(R.id.all_item_refresh);
		ImageView btnAddItem = (ImageView) v.findViewById(R.id.all_item_add);
		ImageView btnAddProj = (ImageView) v.findViewById(R.id.all_item_add_project);
		txLastUpdate = (TextView) v.findViewById(R.id.last_update);
		
	    for (int i=0; i < taskevents.size(); i++) {
	    	Date due = taskevents.get(i).getDueDate();
	    	Project proj = dbUtils.projectsDelegate.get((int)taskevents.get(i).getProjId());
	    	HashMap< String, Object > item = new HashMap< String, Object >();
	    	item.put("checkDone", itemDone);
	    	item.put("itemName", taskevents.get(i).getName());
	    	item.put("itemProj", proj.getName());

	    	if (due==null) {
	    		item.put("dueRelateDay", getString(R.string.undetermined));
    			item.put("today", false);
    			item.put("dueDate", "");
	    	} else {
		    	int dayDistance = dayDistance(due);
		    	item.put("dueRelateDay", dayDistance+getString(R.string.dayafter));
	    		if (dayDistance == 0) {
	    			item.put("today", true);
	    		} else {
	    			item.put("today", false);
	    		}
	    		item.put("dueDate", taskevents.get(i).getDue());
	    	}
	    	item.put("type", taskevents.get(i).getType());
	    	item.put("color", proj.getColor());
	    	items.add(item);
	    }
		
		allItemListView.setAdapter(new SpecialAdapter(this,items,R.layout.all_item_list_item,
    			new String[] { "checkDone", "itemName", "itemProj", "dueRelateDay", "dueDate" },
    			new int[] { R.id.all_item_done, R.id.all_item_name,
						R.id.all_item_project, R.id.all_item_due_relate_day, R.id.all_item_duedate }));

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
    		public void onClick(View v) {
    			final ArtApi artApi = ArtApi.getInstance(MainActivity.this);
		        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		        dialog.setMessage(getString(R.string.syncing));
		        dialog.show();
		        final Handler handler = new Handler() {
		        	@Override
		        	public void handleMessage(Message msg) {
		           		super.handleMessage(msg);
		           		dialog.dismiss();
		           		update();
		           	}
		        };
		        new Thread() {
					@Override
					public void run() { 
						try {
							//FIXME: probably FC due to Dao not open?
							SyncService.sync(dbUtils, MainActivity.this, artApi);
						}
						finally {
							handler.sendEmptyMessage(0);
						}
					}
				}.start();
    		}
    	});
    	
    	btnAddItem.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			String projNames[] = new String[projs.size()];
    			for (int i=0; i < (projs.size()); i++) {
    				projNames[i] = projs.get(i).getName();
    			}
    			Builder dialog = new Builder(MainActivity.this);
    			dialog.setTitle(getString(R.string.select_project));
    			dialog.setSingleChoiceItems(projNames, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent additem_intent= new Intent();
		    	    	additem_intent.putExtra("addOrEdit", ADD_ITEM);
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
		ArrayList<HashMap <String, Object>> items = new ArrayList<HashMap <String, Object>> ();
		List<TaskEvent> taskevents = null;
    	try {
    		taskevents = dbUtils.taskEventDelegate.get(proj.getServerId());
    		mAllTaskEvents.add(taskevents);
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
    	
		CheckBox chkBoxItemDone = null;
		ListView projItemListView = (ListView) v.findViewById(R.id.proj_item_list);
		ImageView btnDocs = (ImageView) v.findViewById(R.id.proj_docs);
		ImageView btnAddItem = (ImageView) v.findViewById(R.id.proj_additem);
		ImageView btnContacts = (ImageView) v.findViewById(R.id.proj_contact);

	    for (int i=0; i < taskevents.size(); i++) {
	    	Date due = taskevents.get(i).getDueDate();
	    	HashMap< String, Object > item = new HashMap< String, Object >();
	    	item.put("checkDone", chkBoxItemDone);
	    	item.put("itemName", taskevents.get(i).getName());
	    	if (taskevents.get(i).getType() == 0) {
	    		item.put("itemOwner", getMembersName(taskevents.get(i).getServerId()));
	    	} else {
	    		item.put("itemOwner", "");
	    	}
    		if (due==null) {
    			item.put("dueRelateDay", getString(R.string.undetermined));
				item.put("today", false);
				item.put("dueDate", "");
    		} else {
	    		int dayDistance = dayDistance(due);
	    		item.put("dueRelateDay", dayDistance+getString(R.string.dayafter));
    			if (dayDistance == 0) {
    				item.put("today", true);
    			} else {
    				item.put("today", false);
    			}
    			item.put("dueDate", taskevents.get(i).getDue());
    		}
    		item.put("type", taskevents.get(i).getType());
	    	item.put("color", proj.getColor());
	    	items.add(item);
	    }
		
		projItemListView.setAdapter(new SpecialAdapter(this,items,R.layout.project_list_item,
    			new String[] { "checkDone", "itemName", "itemOwner", "dueRelateDay", "dueDate" },
    			new int[] { R.id.itemDone, R.id.item_name,
						R.id.item_owner, R.id.item_dueRelateDay, R.id.item_duedate }));

		projItemListView.setOnCreateContextMenuListener(new ListView.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add(Menu.NONE,MENU_EditItem,0,getString(R.string.edit));
				menu.add(Menu.NONE,MENU_DeleteItem,0,getString(R.string.delete));
				menu.setHeaderTitle(getString(R.string.item_operations));
			}
    	});   	
		btnDocs.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			Intent groupdoc_intent = new Intent();
    			try {
					GroupDoc groupDoc = dbUtils.groupDocDelegate.get(proj.getServerId());
					groupdoc_intent.putExtra("groupdoc", groupDoc);
				} catch (ParseException e) {
					e.printStackTrace();
				}
    			groupdoc_intent.setClass(MainActivity.this, GroupDocActivity.class);
    			startActivity(groupdoc_intent);
    	    }
    	});
		btnAddItem.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	Intent additem_intent= new Intent();
    	    	additem_intent.putExtra("addOrEdit", ADD_ITEM);
    	    	additem_intent.putExtra("proj", proj);
    	    	additem_intent.setClass(MainActivity.this, AddItemActivity.class);
    			startActivity(additem_intent);
    	    }
    	});
		btnContacts.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
    	    	intent.putExtra("proj", proj);
    	    	startActivity(intent);
    	    }
    	});
	}
	
	private void getLastUpdate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String prefLastUpdate = prefs.getString(SyncService.PREF_LAST_UPDATE, "");
        if(! "".equals(prefLastUpdate)) {
        		txLastUpdate.setText(getString(R.string.last_update) + prefLastUpdate);
        }
    }
	
    private String getMembersName(long taskId) {
    	String nameList = "";
    	String[] names = dbUtils.tasksMembersDelegate.getMembers(taskId);
    	if(names != null) {
    		nameList = names[0];
    		int i = 1;
    		while(i < names.length) {
    			nameList = names[i] + ", " + nameList;
    			i++;
    		}
    	}
    	return nameList;
    }
	
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
        TaskEvent taskevent;
        Intent additem_intent= new Intent();
        taskevent = ((List<TaskEvent>) mAllTaskEvents.get(position-1)).get(menuInfo.position);
        switch (item.getItemId()) {
        	case MENU_EditItem:
        		switch(taskevent.getType()) {
        			case 0:
        				Task task;
        				try {
        					task = dbUtils.tasksDelegate.getTask(taskevent.getServerId());
            				additem_intent.putExtra("item", task);
            				additem_intent.putExtra("type", TASK);
        				} catch (ParseException e) {
        					Log.v(TAG, "Parse error");
        				}
        				break;
        			case 1:
        				Event event;
        				try {
        					event = dbUtils.eventsDelegate.getEvent(taskevent.getServerId());
            				additem_intent.putExtra("item", event);
            				additem_intent.putExtra("type", EVENT);
        				} catch (ParseException e) {
        					Log.v(TAG, "Parse error");
        				}
        		}
        		additem_intent.setClass(MainActivity.this, AddItemActivity.class);
        		additem_intent.putExtra("addOrEdit", EDIT_ITEM);
        		additem_intent.putExtra("proj", dbUtils.projectsDelegate.get(taskevent.getProjId()));
    			startActivity(additem_intent);
    			break;
        	case MENU_DeleteItem:
        		switch(taskevent.getType()) {
    				case 0:
    					dbUtils.tasksDelegate.setDelete(taskevent.getServerId());
    					break;
    				case 1:
    					dbUtils.eventsDelegate.setDelete(taskevent.getServerId());
        		}
	    		Intent syncIntent = new Intent(MainActivity.this, SyncService.class);
	    		startService(syncIntent);
        		MainActivity.this.update();
        }
    	return super.onContextItemSelected(item); 
    }
    
    private void setSyncService() {
		Intent syncIntent = new Intent(MainActivity.this, SyncService.class);
		PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, syncIntent, 0);
		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		long firstTime = SystemClock.elapsedRealtime();
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					firstTime, 15*60*1000, pendingIntent);
    }
    
    private void init() {
		try {
			Project remoteProjs[] = ArtApi.getInstance(MainActivity.this).getProjectList();
			for (int i=0 ; i < remoteProjs.length ; i++) {
				Project proj = new Project(remoteProjs[i].getName(), remoteProjs[i].getServerId(), remoteProjs[i].getColor(), remoteProjs[i].getUpdateAt());
				dbUtils.projectsDelegate.insert(proj);
			}
			List<Project> localProjs = dbUtils.projectsDelegate.get();
			for (int i=0 ; i<localProjs.size() ; i++) {
				Task remoteTasks[] = ArtApi.getInstance(MainActivity.this).getTaskList(localProjs.get(i).getServerId());
				for (int j=0 ; j < remoteTasks.length ; j++) {
					dbUtils.tasksDelegate.insert(remoteTasks[j]);
					dbUtils.tasksMembersDelegate.insert(remoteTasks[j]);
				}
				Event remoteEvents[] = ArtApi.getInstance(MainActivity.this).getEventList(localProjs.get(i).getServerId());
				for (int k=0 ; k < remoteEvents.length ; k++) {
					Event event = new Event(remoteEvents[k].getProjId(), remoteEvents[k].getServerId(), remoteEvents[k].getName(), remoteEvents[k].getStartAt(), remoteEvents[k].getEndAt(), remoteEvents[k].getLocation(), remoteEvents[k].getNote(), remoteEvents[k].getUpdateAt());
					dbUtils.eventsDelegate.insert(event);
				}
			}
		} catch (ParseException e) {
			Log.v(TAG, "Parse Error");
		} catch (ServerException e) {
			Toast.makeText(MainActivity.this, getString(R.string.remote_server_problem) + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (ConnectionFailException e) {
			Toast.makeText(MainActivity.this, getString(R.string.internet_connection_problem), Toast.LENGTH_SHORT).show();
		}
    }
	
	private Integer dayDistance(Date due) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date today = new Date();
		long DAY = 24L * 60L * 60L * 1000L;
		Calendar dueCal = new GregorianCalendar();
		Calendar todayCal = new GregorianCalendar();
		dueCal.setTime(due);
		try {
			todayCal.setTime(formatter.parse(formatter.format(today)));
			dueCal.setTime(formatter.parse(formatter.format(due)));
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
		return Math.round((dueCal.getTime().getTime()-todayCal.getTime().getTime()) /DAY);
	}

    @Override
	public void onResume() {
		super.onResume();		
		if (dbUtils == null) {
			dbUtils = new DBUtils(this);
		}
    	registerReceiver(dataReceiver, filter);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	if(!mPrefs.getBoolean("AUTHORIZED", false)) {
    		lists = new View[1];
    		lists[0] = mInflater.inflate(R.layout.all_item_list, null);
    		mainView = mInflater.inflate(R.layout.main, null);
    		setContentView(mainView);
    		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
            adapter = new DiffAdapter(this);
            viewFlow.setAdapter(adapter);
            CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
    		viewFlow.setFlowIndicator(indic);
        	Builder dialog = new Builder(MainActivity.this);
        	dialog.setTitle(R.string.welcome_message_title);
        	dialog.setMessage(R.string.welcome_message);
        	dialog.setPositiveButton(R.string.confirm,
            	new DialogInterface.OnClickListener() {
            	    	public void onClick(DialogInterface dialoginterface, int i) {
            	    		dialoginterface.dismiss();
            	    		startActivityForResult(new Intent(MainActivity.this, AuthActivity.class), REQUEST_AUTH);
            	    	}
            	    }
            	);
            	dialog.show();
            	return;
        }
    	if(mPrefs.getBoolean("AUTHORIZED", false) && !(mPrefs.getBoolean("INITIALIZED", false))) {
			final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage(getString(R.string.retriving_existed_data));
			dialog.show();
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					dialog.dismiss();
					super.handleMessage(msg);
			        update();
				}
			};
	        new Thread() { 
	            @Override
	            public void run() { 
	                try {
	                	init();
	                	setSyncService();
	                }
	                finally {
						mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				        mPrefs.edit().putBoolean("INITIALIZED", true).commit();
				        handler.sendEmptyMessage(0);
	                }
	            }
	        }.start();
	        return;
    	}
        if(SyncService.getService() == null) {
        	Intent syncIntent = new Intent(this, SyncService.class);
    		startService(syncIntent);
        }
		update();
    	if(viewFlow != null) {
    		viewFlow.setSelection(position); //XXX: This is UNSAFE!!! project list is sorted by create time
    	}
    }
    
	@Override
	public void onStop() {
		super.onStop();
		if (dbUtils != null) {
			dbUtils.close();
			dbUtils = null;
		}
		unregisterReceiver(dataReceiver);
	}
    
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		viewFlow.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_EditProj, 0, R.string.edit_project).setIcon(android.R.drawable.ic_menu_edit);
		menu.add(0, MENU_QuitProj, 0, R.string.quit_project).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, MENU_ViewFinished, 0, R.string.view_finished_items).setIcon(android.R.drawable.ic_menu_agenda);
		menu.add(0, MENU_Settings, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_Feedbacks, 0, R.string.feedbacks).setIcon(android.R.drawable.ic_menu_send);
		menu.add(0, MENU_About, 0, R.string.about).setIcon(android.R.drawable.ic_menu_help);
		this.openOptionsMenu();
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		if(position==0 || position==1) {
			menu.findItem(MENU_EditProj).setVisible(false);
			menu.findItem(MENU_QuitProj).setVisible(false);
			menu.findItem(MENU_ViewFinished).setVisible(false);
		} else {
			menu.findItem(MENU_EditProj).setVisible(true);
			menu.findItem(MENU_QuitProj).setVisible(true);
			menu.findItem(MENU_ViewFinished).setVisible(true);
		}
        return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case MENU_EditProj:
				AlertDialog.Builder editDialog = new AlertDialog.Builder(this);
				editDialog.setTitle(getString(R.string.edit_project));
				View view = mInflater.inflate(R.layout.edit_project_dialog, null);
				editDialog.setView(view);
				final EditText edProjName = (EditText) view.findViewById(R.id.edit_projname_input);
				edProjName.setText(projs.get(position-2).getName());
				editDialog.setIcon(android.R.drawable.ic_input_get);
				editDialog.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					Project proj = new Project(projs.get(position-2).getServerId(),
    							edProjName.getText().toString(), projs.get(position-2).getServerId(),
    							projs.get(position-2).getColor(), new Date());
    					dbUtils.projectsDelegate.update(proj);
    					update();
    				}
                });
				editDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.dismiss();
    				}
                });
				editDialog.show();
				break;
			case MENU_QuitProj:
				AlertDialog.Builder delDialog = new AlertDialog.Builder(this);
				delDialog.setTitle(getString(R.string.confirm_quit_project));
				delDialog.setIcon(android.R.drawable.ic_dialog_alert);
				delDialog.setMessage(getString(R.string.quit_project_prompt));
				delDialog.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					try {
							ArtApi.getInstance(MainActivity.this).quitProject(projs.get(position-2).getServerId());
		   					dbUtils.projectsDelegate.delete(projs.get(position-2));
		   					dbUtils.groupDocDelegate.delete(projs.get(position-2).getServerId());
	    					dbUtils.tasksDelegate.deleteUnderProj(projs.get(position-2).getServerId());
	    					dbUtils.eventsDelegate.deleteUnderProj(projs.get(position-2).getServerId());
	    					update();
						} catch (ServerException e) {
							Toast.makeText(MainActivity.this, getString(R.string.cannot_quit_project_server_problem) + e.getMessage(), Toast.LENGTH_LONG).show();
						} catch (ConnectionFailException e) {
							Toast.makeText(MainActivity.this, getString(R.string.cannot_quit_project_connection_problem), Toast.LENGTH_LONG).show();
						}
    				}
                });
				delDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.dismiss();
    				}
                });
				delDialog.show();
				break;
			case MENU_ViewFinished:
				Intent viewFinished = new Intent();
				viewFinished.setClass(MainActivity.this, ViewFinishedItemsActivity.class);
				viewFinished.putExtra("proj", projs.get(position-2));
    			startActivity(viewFinished);
				break;
			case MENU_Settings:
				break;
			case MENU_Feedbacks:
				// TODO: replace feedback email here
				Uri uri = Uri.parse("mailto:u103133.u103135@gmail.com");
                Intent intent = new Intent(Intent.ACTION_SENDTO,uri);
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.feedback_mail_title));
                startActivity(intent);
				break;
			case MENU_About:
				break;
		}
		return super.onOptionsItemSelected(item);
	};
	
	
	private class SpecialAdapter extends SimpleAdapter {
		ArrayList<HashMap<String, Object>> items;
		int resource;
		public SpecialAdapter(Context context, ArrayList<HashMap<String, Object>> items,
				int resource, String[] from, int[] to) {
			super(context, items, resource, from, to);
			this.items = items;
			this.resource = resource;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
		  View view = super.getView(position, convertView, parent);
		  TextView relateDay = null;
		  TextView txOwner = null;
		  TextView color = null;
		  CheckBox done = null;
		  
		  switch(resource) {
		  	case R.layout.all_item_list_item:
		  		relateDay = (TextView) view.findViewById(R.id.all_item_due_relate_day);
		  		color = (TextView) view.findViewById(R.id.all_item_color);
		  		done = (CheckBox) view.findViewById(R.id.all_item_done);
		  		break;	
		  	case R.layout.project_list_item:
		  		relateDay = (TextView) view.findViewById(R.id.item_dueRelateDay);
		  		txOwner = (TextView) view.findViewById(R.id.item_owner);
		  		color = (TextView) view.findViewById(R.id.item_color);
		  		done = (CheckBox) view.findViewById(R.id.itemDone);
		  }
		  
		  if((Boolean)items.get(position).get("today") == true) {
			  relateDay.setText(getString(R.string.due_today));
			  relateDay.setTextColor(Color.RED);
		  } else {
			  relateDay.setTextColor(Color.WHITE);
		  }
		  
		  if((Integer)items.get(position).get("type") == 1) {
			  done.setVisibility(View.GONE);
			  color.setVisibility(View.INVISIBLE);
			  if(txOwner != null) {
				  txOwner.setVisibility(View.INVISIBLE);
			  }
		  } else {
			  done.setVisibility(View.VISIBLE);
			  color.setVisibility(View.VISIBLE);
			  if(txOwner != null) {
				  txOwner.setVisibility(View.VISIBLE);
			  }
		  }
		  
		  color.setBackgroundColor(Color.parseColor(colors[(Integer)items.get(position).get("color")])); 
		  
		  done.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			  @Override
			  public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
				  TaskEvent taskevent = ((List<TaskEvent>) mAllTaskEvents.
						  get(MainActivity.this.position-1)).get(position);
				  try {
					  	Task task;
      					task = dbUtils.tasksDelegate.getTask(taskevent.getServerId());
      					task.setDone(true);
      					task.setUpdateAt(new Date());
      					dbUtils.tasksDelegate.update(task);
      					update();
    		    		Intent syncIntent = new Intent(MainActivity.this, SyncService.class);
    		    		startService(syncIntent);
				  } catch (ParseException e) {
      					Log.v(TAG, "Parse error");
				  }
			  }
		  });
		  return view;
		}
	}
	
	
    private class DataReceiver extends BroadcastReceiver {
        @Override  
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(MainActivity.this, intent.getStringExtra("service_data"), Toast.LENGTH_SHORT).show();
			txLastUpdate.setText(intent.getStringExtra("service_data"));
        }
    }   

	
	private class DiffAdapter extends BaseAdapter {
		public DiffAdapter(Context context) {
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
			convertView = lists[position];
			return convertView;
		}
	}
}