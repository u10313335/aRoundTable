package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
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
	
	//TODO:dummy test data, remove them ASAP
	private String itemOwners[] = { "小羽、小熊", "albb", "洞洞", "所有人", "小羽、小熊", "albb", "洞洞", "所有人" };
	private String token;
	private DBUtils dbUtils;
	private List<User> users;
	private List<Project> projs;
	private LayoutInflater mInflater;
	private View lists[]; //list[0] is "notifications", list[1] is "all task/events list",
    private View mainView; // the followings(lists[2]~) are "project list"
    private TextView txTitle;
	private ViewFlow viewFlow;
	private DiffAdapter adapter;
	private ArrayList<List<TaskEvent>> mAllTaskEvents = new ArrayList<List<TaskEvent>>();
	private int position = 1;  // screen position
	private final String colors[] = { "#00B0CF", "#A2CA30", "#F2E423",
			"#CA4483", "#E99314", "#C02B20", "#F7F7CF", "#225DAB" };
	protected static final int MENU_EditProj = Menu.FIRST;
	protected static final int MENU_QuitProj = Menu.FIRST+1;
    protected static final int MENU_Settings = Menu.FIRST+2;
    protected static final int MENU_Feedbacks = Menu.FIRST+3;
    protected static final int MENU_About = Menu.FIRST+4;
    protected static final int MENU_EditItem = Menu.FIRST;
    protected static final int MENU_DeleteItem = Menu.FIRST+1;
    private static final int TASK = 0;
    private static final int EVENT = 1;
    private static final int ADD_ITEM = 0;
    private static final int EDIT_ITEM = 1;
	private static String TAG = "MainActivity";

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
        
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
    	mAllTaskEvents.clear();
    	if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
    	projs = dbUtils.projectsDelegate.get();
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
    		} catch (ParseException e) {
    			Log.v(TAG, "Parse error");
    		}

    		for (int i=2; i < (projs.size())+2; i++) {
    			lists[i] = mInflater.inflate(R.layout.project_list, null);
    			formProjLists(lists[i], projs.get(i-2));
    		}
    		mainView = mInflater.inflate(R.layout.main, null);
    		txTitle = (TextView) mainView.findViewById(R.id.main_title);
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
    		        } else if(position==1) {
    		        	txTitle.setText(getString(R.string.all_item_event));
    		        } else {
    		        	txTitle.setText(projs.get(position-2).getName());
    		        }
    		    }
    		});
			viewFlow.setSelection(MainActivity.this.position);
    	}else {
            Intent addgroup_intent= new Intent();
            addgroup_intent.setClass(MainActivity.this,CreateProjectActivity.class);
            startActivity(addgroup_intent);
    	}
    }
    
    private void formNotification(View v) {
    	ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>> ();
    	//TODO:dummy test data, remove them ASAP
    	String[] notifications = new String[]{"小羽完成了「OR PPT」",
    			"小熊完成了「FI訪談問題」",
    			"洞洞更改了「SA PPT」時間"};
    	ListView notificationView = (ListView) v.findViewById(R.id.dynamic_issue_list);
        
        for(int i=0;i<notifications.length;i++)
        {
        	HashMap<String, Object> map = new HashMap<String, Object>();
        	map.put("notifications",notifications[i]);
        	listItem.add(map);
        }
        
        notificationView.setAdapter(new SimpleAdapter(this,listItem, 
                R.layout.notification_item,
                new String[] {"notifications"}, 
                new int[] {R.id.notificaton_context}));
    }
    
    // form all task/event list
    private void formAllItemList(View v, List<TaskEvent> taskevents) {
    	ArrayList<HashMap <String, Object>> items = new ArrayList<HashMap <String, Object>> ();
    	CheckBox itemDone = null;
		ListView allItemListView = (ListView) v.findViewById(R.id.all_item_list);
		ImageView btnRefresh = (ImageView) v.findViewById(R.id.all_item_refresh);
		ImageView btnAddItem = (ImageView) v.findViewById(R.id.all_item_add);
		ImageView btnAddProj = (ImageView) v.findViewById(R.id.all_item_add_project);
		
	    for (int i=0; i < taskevents.size(); i++) {
	    	Date due = taskevents.get(i).getDueDate();
	    	Project proj = dbUtils.projectsDelegate.get((int)taskevents.get(i).getProjId());
	    	HashMap< String, Object > item = new HashMap< String, Object >();
	    	item.put("checkDone", itemDone);
	    	item.put("itemName", taskevents.get(i).getName());
	    	item.put("itemProj", proj.getName());
	    	item.put("taskEventId", taskevents.get(i).getId());

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
    		public void onClick(View arg0) {
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
	private void formProjLists(View v, Project p){
		final Project proj = p;
		ArrayList<HashMap <String, Object>> items = new ArrayList<HashMap <String, Object>> ();
		List<TaskEvent> taskevents = null;
    	try {
    		taskevents = dbUtils.taskEventDelegate.get(proj.getId());
    		mAllTaskEvents.add(taskevents);
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
    	
		CheckBox chkBoxItemDone = null;
		ListView projItemListView = (ListView) v.findViewById(R.id.proj_item_list);
		ImageView btnIssue = (ImageView) v.findViewById(R.id.proj_issue_tracker);
		ImageView btnDocs = (ImageView) v.findViewById(R.id.proj_docs);
		ImageView btnAddItem = (ImageView) v.findViewById(R.id.proj_additem);
		ImageView btnContacts = (ImageView) v.findViewById(R.id.proj_contact);
		ImageView btnChart = (ImageView) v.findViewById(R.id.proj_chart);	

	    for (int i=0; i < taskevents.size(); i++) {
	    	Date due = taskevents.get(i).getDueDate();
	    	HashMap< String, Object > item = new HashMap< String, Object >();
	    	item.put("checkDone", chkBoxItemDone);
	    	item.put("itemName", taskevents.get(i).getName());
	    	item.put("itemOwner", itemOwners[0]);
	    	item.put("taskEventId", taskevents.get(i).getId());
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
        Intent additem_intent= new Intent();
        taskevent = ((List<TaskEvent>) mAllTaskEvents.get(position-1)).get(menuInfo.position);
        switch (item.getItemId()) {
        	case MENU_EditItem:
        		switch(taskevent.getType()) {
        			case 0:
        				Task task;
        				try {
        					task = dbUtils.tasksDelegate.getTask(taskevent.getId());
            				additem_intent.putExtra("item", task);
            				additem_intent.putExtra("type", TASK);
        				} catch (ParseException e) {
        					Log.v(TAG, "Parse error");
        				}
        				break;
        			case 1:
        				Event event;
        				try {
        					event = dbUtils.eventsDelegate.getEvent(taskevent.getId());
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
    					dbUtils.tasksDelegate.delete(taskevent.getId());
    					break;
    				case 1:
    					dbUtils.eventsDelegate.delete(taskevent.getId());
        		}
        		MainActivity.this.update();
        }
    	return super.onContextItemSelected(item); 
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

    // FIXME: duplicate notification after registration
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
	 	Uri uri = intent.getData();
	 	if(uri != null) {
		 	token = uri.getQueryParameter("");
		    User user = new User(token);
		    if (dbUtils == null) {
				dbUtils = new DBUtils(this);
			}
			dbUtils.userDelegate.insert(user);
			dbUtils.close();
			Toast.makeText(this, R.string.register_finished, Toast.LENGTH_SHORT).show();
		    Log.v(TAG, "[onNewIntent] Token back: "+token);
	 	}
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
        		viewFlow.setSelection(position); //XXX: This is UNSAFE!!! project list is sorted by alphabet order
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
		Uri uri = Uri.parse(ArtApi.getLoginUrl());
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }
    
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		viewFlow.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,MENU_EditProj, 0, R.string.edit_project).setIcon(android.R.drawable.ic_menu_edit);
		menu.add(0,MENU_QuitProj, 0, R.string.quit_project).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0,MENU_Settings, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0,MENU_Feedbacks, 0, R.string.feedbacks).setIcon(android.R.drawable.ic_menu_send);
		menu.add(0,MENU_About, 0, R.string.about).setIcon(android.R.drawable.ic_menu_help);
		this.openOptionsMenu();
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		if(position==0 || position==1) {
			menu.findItem(MENU_EditProj).setVisible(false);
			menu.findItem(MENU_QuitProj).setVisible(false);
		} else {
			menu.findItem(MENU_EditProj).setVisible(true);
			menu.findItem(MENU_QuitProj).setVisible(true);
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
    					Project proj = new Project(projs.get(position-2).getId(),
    							edProjName.getText().toString(), projs.get(position-2).getServerId(),
    							projs.get(position-2).getColor());
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
    					dbUtils.projectsDelegate.delete(projs.get(position-2));
    					dbUtils.tasksDelegate.deleteUnderProj(projs.get(position-2).getId());
    					dbUtils.eventsDelegate.deleteUnderProj(projs.get(position-2).getId());
    					update();
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
			  if(txOwner!=null) {
				  txOwner.setVisibility(View.INVISIBLE);
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
      					task = dbUtils.tasksDelegate.getTask(taskevent.getId());
      					task.setDone(1);
      					dbUtils.tasksDelegate.update(task);
      					update();
				  } catch (ParseException e) {
      					Log.v(TAG, "Parse error");
				  }
			  }
		  });
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