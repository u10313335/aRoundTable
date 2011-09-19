package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.Notification;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.lib.SyncService;
import tw.jouou.aRoundTable.util.DBUtils;
import tw.jouou.aRoundTable.util.DBUtils.TaskEventDelegate;

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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
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
	
	//TODO:dummy test data, remove them ASAP
	private String itemOwners[] = { "小羽、小熊", "albb", "洞洞", "所有人", "小羽、小熊", "albb", "洞洞", "所有人" };
	private String token;
	private int mUnReadCount;
	private DBUtils dbUtils;
	private List<User> users;
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
	private OwnedTaskEventAdapter ownedTaskEventAdapter;
	private ProjectTaskEventAdapter[] projectTaskEventAdapters;
	private int position = 1;  // screen position
	private TypedArray colors;
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
    private static final int PROJECT_VIEWS_BORDER = 2;
	private static String TAG = "MainActivity";

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
        
        colors = getResources().obtainTypedArray(R.array.project_colors);
    	users = dbUtils.userDelegate.get();
    	
    	mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
    	if(!users.isEmpty()) {
    		token = users.get(0).getToken();
         	update();
    	}else {
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
    	}
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
		if(SyncService.getService() == null) {
			Intent syncIntent = new Intent(MainActivity.this, SyncService.class);
			PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, syncIntent, 0);
			AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
			long firstTime = SystemClock.elapsedRealtime();
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				firstTime, 15*60*1000, pendingIntent);
		}
    	if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
    	try {
			projs = dbUtils.projectsDelegate.get();
			projectTaskEventAdapters = new ProjectTaskEventAdapter[projs.size()];
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
    	if(!projs.isEmpty()) {
    		lists = new View[(projs.size())+2];
    		lists[0] = mInflater.inflate(R.layout.notification, null);
    		lists[1] = mInflater.inflate(R.layout.all_item_list, null);
    		
    		formNotification(lists[0]);
    		formAllItemList(lists[1]);

    		for (int i=0; i < projs.size(); i++) {
    			formProjLists(i, projs.get(i));
    		}
    		mainView = mInflater.inflate(R.layout.main, null);
    		txTitle = (TextView) mainView.findViewById(R.id.main_title);
    		ivUnreadIndicator = (ImageView) mainView.findViewById(R.id.main_unread_indicator);
    		txUnread = (TextView) mainView.findViewById(R.id.main_unread_count);
    		if(mUnReadCount > 0) {
    			txUnread.setText(""+mUnReadCount);
    			ivUnreadIndicator.setImageResource(R.drawable.has_unread);
    		}
    		setContentView(mainView);
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
    		        	txTitle.setTextColor(colors.getColor(projs.get(position-2).getColor(), 0));
    		        }
    		    }
    		});
    		getLastUpdate();
			viewFlow.setSelection(MainActivity.this.position);
    	}else {
            Intent addgroup_intent= new Intent();
            addgroup_intent.setClass(MainActivity.this,CreateProjectActivity.class);
            startActivity(addgroup_intent);
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
    private void formAllItemList(View v) {
		ExpandableListView allItemListView = (ExpandableListView) v.findViewById(R.id.all_item_list);
		ImageView btnRefresh = (ImageView) v.findViewById(R.id.all_item_refresh);
		ImageView btnAddItem = (ImageView) v.findViewById(R.id.all_item_add);
		ImageView btnAddProj = (ImageView) v.findViewById(R.id.all_item_add_project);
		txLastUpdate = (TextView) v.findViewById(R.id.last_update);
		TaskEventDelegate taskEventDelegate = dbUtils.taskEventDelegate;
		allItemListView.setAdapter(new OwnedTaskEventAdapter(this, taskEventDelegate.getOwned(), taskEventDelegate.getOwnedOverDue()));

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
	    		Intent syncIntent = new Intent(MainActivity.this, SyncService.class);
	    		startService(syncIntent);
	    		update();
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
	private void formProjLists(int projectPos, final Project project){
		View v = lists[PROJECT_VIEWS_BORDER + projectPos] = mInflater.inflate(R.layout.project, null);
		ExpandableListView projItemListView = (ExpandableListView) v.findViewById(R.id.proj_item_list);
		ImageView btnDocs = (ImageView) v.findViewById(R.id.proj_docs);
		ImageView btnAddItem = (ImageView) v.findViewById(R.id.proj_additem);
		ImageView btnContacts = (ImageView) v.findViewById(R.id.proj_contact);
		
		TaskEventDelegate taskEventDelegate = dbUtils.taskEventDelegate;
		long projectId = project.getServerId();
		projectTaskEventAdapters[projectPos] = new ProjectTaskEventAdapter(this, taskEventDelegate.get(projectId), taskEventDelegate.getOverDue(projectId));
		projItemListView.setAdapter(projectTaskEventAdapters[projectPos]);

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
    	    	// TODO:insert group docs activity here
    	    }
    	});
		btnAddItem.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	Intent additem_intent= new Intent();
    	    	additem_intent.putExtra("addOrEdit", ADD_ITEM);
    	    	additem_intent.putExtra("proj", project);
    	    	additem_intent.setClass(MainActivity.this, AddItemActivity.class);
    			startActivity(additem_intent);
    	    }
    	});
		btnContacts.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
    	    	intent.putExtra("proj", project);
    	    	startActivity(intent);
    	    }
    	});
	}
	
	
	private void getLastUpdate() {
        SharedPreferences settings = getSharedPreferences(SyncService.PREF, 0);
        String prefLastUpdate = settings.getString(SyncService.PREF_LAST_UPDATE, "");
        if(! "".equals(prefLastUpdate)) {
        		txLastUpdate.setText("上次更新時間 : " + prefLastUpdate);
        }
    }
	
    public boolean onContextItemSelected(MenuItem item) {
    	if(position == 0){
    		//TODO: Notification context menu
    		return false;
    	}else{
    		ExpandableListContextMenuInfo menuInfo = (ExpandableListContextMenuInfo)item.getMenuInfo();
    		ExpandableListAdapter adapter = (position == 1)? ownedTaskEventAdapter : projectTaskEventAdapters[position - PROJECT_VIEWS_BORDER];
    		long packedType = ExpandableListView.getPackedPositionType(menuInfo.packedPosition);
    		int group = ExpandableListView.getPackedPositionGroup(packedType), 
    			 child = ExpandableListView.getPackedPositionChild(packedType);
    		
    		TaskEvent taskEvent = (TaskEvent) ((child >= 0)? adapter.getChild(group, child): adapter.getGroup(group));
    		Intent additem_intent= new Intent();
            switch (item.getItemId()) {
        	case MENU_EditItem:
        		switch(taskEvent.getType()) {
        			case 0:
        				Task task;
        				try {
        					task = dbUtils.tasksDelegate.getTask(taskEvent.getServerId());
            				additem_intent.putExtra("item", task);
            				additem_intent.putExtra("type", TASK);
        				} catch (ParseException e) {
        					Log.v(TAG, "Parse error");
        				}
        				break;
        			case 1:
        				Event event;
        				try {
        					event = dbUtils.eventsDelegate.getEvent(taskEvent.getServerId());
            				additem_intent.putExtra("item", event);
            				additem_intent.putExtra("type", EVENT);
        				} catch (ParseException e) {
        					Log.v(TAG, "Parse error");
        				}
        		}
        		additem_intent.setClass(MainActivity.this, AddItemActivity.class);
        		additem_intent.putExtra("addOrEdit", EDIT_ITEM);
        		additem_intent.putExtra("proj", dbUtils.projectsDelegate.get(taskEvent.getProjId()));
    			startActivity(additem_intent);
    			break;
        	case MENU_DeleteItem:
        		switch(taskEvent.getType()) {
    				case 0:
    					dbUtils.tasksDelegate.setDelete(taskEvent.getServerId());
    					break;
    				case 1:
    					dbUtils.eventsDelegate.setDelete(taskEvent.getServerId());
        		}
	    		Intent syncIntent = new Intent(MainActivity.this, SyncService.class);
	    		startService(syncIntent);
        		MainActivity.this.update();
            }	
    	}
    	return true;
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
        	if(viewFlow != null)
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
	    					dbUtils.tasksDelegate.deleteUnderProj(projs.get(position-2).getServerId());
	    					dbUtils.eventsDelegate.deleteUnderProj(projs.get(position-2).getServerId());
	    					update();
						} catch (ServerException e) {
							Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
						} catch (ConnectionFailException e) {
							Toast.makeText(MainActivity.this, "Network not ok, try later", Toast.LENGTH_LONG).show();
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
