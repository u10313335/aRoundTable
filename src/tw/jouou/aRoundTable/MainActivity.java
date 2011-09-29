package tw.jouou.aRoundTable;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.taptwo.android.widget.CircleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;
import org.taptwo.android.widget.ViewFlow.ViewSwitchListener;

import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.GroupDoc;
import tw.jouou.aRoundTable.bean.Notification;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.NotLoggedInException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.lib.SyncService;
import tw.jouou.aRoundTable.util.DBUtils;
import tw.jouou.aRoundTable.util.DBUtils.TaskEventDelegate;
import tw.jouou.aRoundTable.util.Utils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Main Activity of aRound Table
 * 
 * Shows all tasks and can switch to different views
 */
public class MainActivity extends Activity implements ViewSwitchListener {
	
	private int mUnReadCount;
	private boolean afterPause = false;
	private DBUtils dbUtils;
	private List<Project> projs;
	private LayoutInflater mInflater;
	private View lists[]; //list[0] is "notifications", list[1] is "all task/events list",
	private ExpandableListView allItemListView;
    private TextView txTitle;
    private ImageView ivUnreadIndicator;
    private TextView txUnread;
    private TextView txLastUpdate;
	private ViewFlow viewFlow;
	private ViewFlowAdapter viewFlowAdapter;
	private NotificationsAdapter notificationsAdapter;
	private OwnedTaskEventAdapter ownedTaskEventAdapter;
	private ProjectTaskEventAdapter[] projectTaskEventAdapters;
	private int position = 1;  // screen position
	private TypedArray colors;
	private SharedPreferences mPrefs;
	private BroadcastReceiver syncResultReceiver;
	private enum UnreadINdicatorState {STARTE_ON, STATE_OFF};
	private UnreadINdicatorState unreadINdicatorState = UnreadINdicatorState.STATE_OFF;
	protected static final int MENU_EditProj = Menu.FIRST;
	protected static final int MENU_QuitProj = Menu.FIRST+1;
	protected static final int MENU_ViewFinished = Menu.FIRST+2;
    protected static final int MENU_Logout = Menu.FIRST+3;
    protected static final int MENU_About = Menu.FIRST+4;
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
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	if(!mPrefs.getBoolean("AUTHORIZED", false)) {
    		startActivityForResult(new Intent(MainActivity.this, AuthActivity.class), REQUEST_AUTH);
        }
        
    	dbUtils = DBUtils.getInstance(this);
        
        colors = getResources().obtainTypedArray(R.array.project_colors);
    	mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	
    	setContentView(R.layout.main);
    	txTitle = (TextView) findViewById(R.id.main_title);
    	ivUnreadIndicator = (ImageView) findViewById(R.id.main_unread_indicator);
    	txUnread = (TextView) findViewById(R.id.main_unread_count);
    	viewFlow = (ViewFlow) findViewById(R.id.viewflow);
		viewFlow.setFlowIndicator((CircleFlowIndicator) findViewById(R.id.viewflowindic));
		update();
		viewFlow.setOnViewSwitchListener(this);
		
		registerReceiver(syncResultReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch(intent.getIntExtra(SyncService.EXTRA_SYNCSTATUS_CODE, -1)){
				case SyncService.STATUS_FINISHED_OK:
					update();
					break;
				default:
					if(txLastUpdate != null)
						txLastUpdate.setText(intent.getStringExtra(SyncService.EXTRA_SYNC_STATUS_STRING));
				}
			}
		}, new IntentFilter(SyncService.ACTION_SYNC_STATUS));
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
    
	@Override
	public void onSwitched(View arg0, int position) {
        this.position = position;
        if(position==0) {
        	txTitle.setText(getString(R.string.notification));
        	txTitle.setTextColor(Color.parseColor("#F6F6F7"));
        } else if(position==1) {
        	txTitle.setText(getString(R.string.owned_task_event));
        	txTitle.setTextColor(Color.parseColor("#F6F6F7"));
        } else {
        	txTitle.setText(getString(R.string.project_task_event, projs.get(position-2).getName()));
        	txTitle.setTextColor(colors.getColor(projs.get(position-2).getColor(), 0));
        }
	}
    
    protected void update() {
    	try {
			projs = dbUtils.projectsDelegate.get();
			projectTaskEventAdapters = new ProjectTaskEventAdapter[projs.size()];
			mUnReadCount = dbUtils.notificationDao.queryForEq(Notification.COLUMN_READ, true).size();
			if(mUnReadCount > 0) {
				txUnread.setText(Integer.toString(mUnReadCount));
				if(unreadINdicatorState == UnreadINdicatorState.STATE_OFF)
					ivUnreadIndicator.setImageResource(R.drawable.has_unread);
			}else if(unreadINdicatorState == UnreadINdicatorState.STARTE_ON)
				ivUnreadIndicator.setImageResource(R.drawable.unread);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(lists != null){
			lists = Utils.copyArray(lists, projs.size()+2);
		}else{
    		lists = new View[(projs.size())+2];
    		lists[0] = mInflater.inflate(R.layout.notification, null);
    		formAllItemList();
		}
    		
		updateNotification();
		updateAllItemList();
		updateProjectLists();
    	
		txLastUpdate.setText(mPrefs.getString(SyncService.PREF_LAST_UPDATE, ""));
    	
    	if(viewFlowAdapter == null){
    		viewFlowAdapter = new ViewFlowAdapter();
    		viewFlow.setAdapter(viewFlowAdapter);
    	}else
    		viewFlowAdapter.notifyDataSetChanged();
    	
    	viewFlow.setSelection(position);
    }

    private void updateNotification() {
    	ListView notificationView = (ListView) lists[0].findViewById(R.id.notifications);
		if(notificationsAdapter != null)
			notificationsAdapter.rebase();
		else{
			notificationsAdapter = new NotificationsAdapter(this);
			notificationView.setAdapter(notificationsAdapter);
			notificationView.setOnItemClickListener(notificationsAdapter);
		}
    }
    
    // form all task/event list
    private void formAllItemList() {
    	View v = lists[1] = mInflater.inflate(R.layout.all_item_list, null);
		allItemListView = (ExpandableListView) v.findViewById(R.id.all_item_list);
		allItemListView.setOnCreateContextMenuListener(new ExpandableCreateContextMenuListener());
		txLastUpdate = (TextView) v.findViewById(R.id.last_update);
    	
    	v.findViewById(R.id.all_item_refresh).setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			startService(new Intent(MainActivity.this, SyncService.class));
    		}
    	});
    	
    	v.findViewById(R.id.all_item_add).setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			String projNames[] = new String[projs.size()];
    			for (int i=0; i < (projs.size()); i++) {
    				projNames[i] = projs.get(i).getName();
    			}
    			new Builder(MainActivity.this)
    				.setTitle(getString(R.string.select_project))
    				.setSingleChoiceItems(projNames, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent additem_intent= new Intent();
		    	    	additem_intent.putExtra("addOrEdit", ADD_ITEM);
		    	    	additem_intent.putExtra("proj", projs.get(which));
		    	    	additem_intent.setClass(MainActivity.this, AddItemActivity.class);
		    			startActivity(additem_intent);
					}
    			}).show();
    	    }
    	});
    	
    	v.findViewById(R.id.all_item_add_project).setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	    	Intent addgroup_intent= new Intent();
    			addgroup_intent.setClass(MainActivity.this, CreateProjectActivity.class);
    			startActivity(addgroup_intent);
    	    }
    	});
    }
    
    private void updateAllItemList(){
		TaskEventDelegate taskEventDelegate = dbUtils.taskEventDelegate;
		
		int uid = mPrefs.getInt("UID", 0);
		allItemListView.setAdapter(
				ownedTaskEventAdapter = new OwnedTaskEventAdapter(
						this, 
						taskEventDelegate.getOwned(uid), 
						taskEventDelegate.getOwnedOverDue(uid)));
    }
    
    // form task/event list belongs to specific project
	private void updateProjectLists(){
		for(int i=0; i<projs.size(); i++){
			final Project project = projs.get(i);
			View v;
			
			if((v = lists[PROJECT_VIEWS_BORDER + i]) == null){
				v = lists[PROJECT_VIEWS_BORDER + i] = mInflater.inflate(R.layout.project, null);
				v.findViewById(R.id.proj_docs).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent groupdoc_intent = new Intent();
						try {
							GroupDoc groupDoc = dbUtils.groupDocDelegate.get(project.getServerId());
							groupdoc_intent.putExtra("groupdoc", groupDoc);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						groupdoc_intent.setClass(MainActivity.this, GroupDocActivity.class);
						startActivity(groupdoc_intent);
				    }
				});
				v.findViewById(R.id.proj_additem).setOnClickListener(new OnClickListener() {
				    @Override
				    public void onClick(View arg0) {
				    	Intent additem_intent= new Intent();
				    	additem_intent.putExtra("addOrEdit", ADD_ITEM);
				    	additem_intent.putExtra("proj", project);
				    	additem_intent.setClass(MainActivity.this, AddItemActivity.class);
						startActivity(additem_intent);
				    }
				});
				v.findViewById(R.id.proj_contact).setOnClickListener(new OnClickListener() {
				    @Override
				    public void onClick(View arg0) {
				    	Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
				    	intent.putExtra("proj", project);
				    	startActivity(intent);
				    }
				});
			}
			
			long projectId = project.getServerId();
			ExpandableListView projItemListView = (ExpandableListView) v.findViewById(R.id.proj_item_list);
			TaskEventDelegate taskEventDelegate = dbUtils.taskEventDelegate;
			projectTaskEventAdapters[i] = new ProjectTaskEventAdapter(this, taskEventDelegate.get(projectId), taskEventDelegate.getOverDue(projectId));
			projItemListView.setAdapter(projectTaskEventAdapters[i]);
			projItemListView.setOnCreateContextMenuListener(new ExpandableCreateContextMenuListener());
		}
	}

    public boolean onContextItemSelected(MenuItem item) {
    	if(position == 0){
    		//TODO: Notification context menu
    		return false;
    	}else{
    		ExpandableListContextMenuInfo menuInfo = (ExpandableListContextMenuInfo)item.getMenuInfo();
    		ExpandableListAdapter adapter = (position == 1)? ownedTaskEventAdapter : projectTaskEventAdapters[position - PROJECT_VIEWS_BORDER];
    		int group = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition), 
    			child = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition);
    		
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
        		MainActivity.this.update();
            }	
    	}
    	return true;
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	afterPause = true;
    }

    @Override
	public void onResume() {
		super.onResume();
		
    	if(!(mPrefs.getBoolean("INITIALIZED", false))) {
    		//TODO: blocking loading screen
    	}
    	
    	//FIXME: doing sync every resume...?
    	startService(new Intent(this, SyncService.class));

		// Only run if once been paused, to prevent duplicate in onCreate
		if(afterPause){
			update();
		}
    }
    
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(syncResultReceiver);
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
		menu.add(0, MENU_Logout, 0, R.string.logout).setIcon(android.R.drawable.ic_menu_set_as);
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
				editDialog.setTitle(R.string.edit_project);
				View editView = mInflater.inflate(R.layout.edit_project_dialog, null);
				editDialog.setView(editView);
				final EditText edProjName = (EditText) editView.findViewById(R.id.edit_projname_input);
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
						} catch (NotLoggedInException e) {
							e.printStackTrace();
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
			case MENU_Logout:
				AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
				logoutDialog.setTitle(R.string.logout);
				logoutDialog.setMessage(R.string.confirm_logout);
				logoutDialog.setIcon(android.R.drawable.ic_menu_set_as);
				logoutDialog.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					if(MainActivity.this.deleteDatabase(DBUtils.DB_NAME)) {
    						Log.v(TAG, "delete db successfully");
    					}
    					DBUtils.resetInstance(MainActivity.this);
    					mPrefs.edit().clear().commit();
    					Intent intent = getIntent();
    					overridePendingTransition(0, 0);
    					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    					finish();
    					overridePendingTransition(0, 0);
    					startActivity(intent);
    				}
                });
				logoutDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.dismiss();
    				}
                });
				logoutDialog.show();
				break;
			case MENU_About:
				AlertDialog.Builder aboutDialog = new AlertDialog.Builder(this);
				aboutDialog.setTitle(R.string.about);
				WebView aboutView = (WebView) mInflater.inflate(R.layout.about, null);
				aboutView.loadUrl("file:///android_asset/about.html");
				aboutDialog.setView(aboutView);
				aboutDialog.setIcon(R.drawable.icon);
				aboutDialog.setNegativeButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.dismiss();
    				}
                });
				aboutDialog.show();
				break;
		}
		return super.onOptionsItemSelected(item);
	};
	
	private class ExpandableCreateContextMenuListener implements OnCreateContextMenuListener{
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
    		int group = ExpandableListView.getPackedPositionGroup(info.packedPosition), 
    			child = ExpandableListView.getPackedPositionChild(info.packedPosition);
    		
    		// This item have no function
    		if(group == 0 && child == -1)
    			return;
    		
			menu.add(Menu.NONE,MENU_EditItem, 0, R.string.edit);
			menu.add(Menu.NONE,MENU_DeleteItem, 0, R.string.delete);
			menu.setHeaderTitle(getString(R.string.item_operations));
		}
	}

	private class ViewFlowAdapter extends BaseAdapter {
		public ViewFlowAdapter() {
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
