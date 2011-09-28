package tw.jouou.aRoundTable;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;

import com.j256.ormlite.stmt.QueryBuilder;

import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.SyncService;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import tw.jouou.aRoundTable.widget.NumberPicker;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class AddSingleTaskActivity extends Activity {
	

    private static final int DATE_DIALOG_ID = 0;
    private static final int ASSIGN_DAY_PANEL = 0;
    private static final int DEPENDENCY_PANEL = 1;
    private static final int UNDETERMINED_PANEL = 2;
    private static String TAG = "AddSingleTaskActivity";
	private DBUtils dbUtils;
	private Task mTask;
	private long[] dependOnIds;
	private Bundle mBundle;
	private String mProjName;
	private Project mProj;
	private Date mTaskDue;
	private int mDueType = ASSIGN_DAY_PANEL;
	private LinkedList<User> mTaskOwners = new LinkedList<User>();
	private LinkedList<TableRow> mDependableTasks = new LinkedList<TableRow>();
	private long mProjId;
	private LayoutInflater mInflater;
	private RelativeLayout mDateChooser;
	private RelativeLayout mTaskDependency;
	private TableLayout mUserField;
	private List<Task> mTasks = null;
    private EditText mEdTitle;
    private TextView mTxCreateUnder;
    private ImageButton mBtnOneDay;
    private ImageButton mBtnsSevenDay;
    private ImageButton mBtnNDay;
    private ImageButton mBtnAssignDate;
    private ImageButton mBtnDependency;
    private ImageButton mBtnUndetermined;
    private ImageButton mBtnAddOwner;
    private Button mBtnDatePicker;
    private Button mBtnFinish;
    private Button mBtnCancel;
    private EditText mEdRemarks;
    private SimpleDateFormat mDateToStr, mStrToDate;
    private Calendar mCalendar = Calendar.getInstance();
    private int mYear;
    private int mMonth;
    private int mDay;

	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_single_task_tab);
        
        if(dbUtils == null) {
        	dbUtils = DBUtils.getInstance(this);
    	}
        findViews();  //find basic views
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDateToStr = new SimpleDateFormat("yyyy/MM/ddE");
        mStrToDate = new SimpleDateFormat("yyyy MM dd");
        //get today
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH);
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mBundle = this.getIntent().getExtras();
        mProj = (Project)mBundle.get("proj");
        try {
    		mTasks = dbUtils.tasksDelegate.get(mProj.getServerId());
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
        if (mBundle.getInt("addOrEdit") == 0) {
            mProjName = mProj.getName();
            mProjId = mProj.getServerId();
            mTxCreateUnder.setText(mProjName);
            findAssignDateView();
            updateDisplay(mYear, mMonth, mDay);
        } else {
        	// remove itself from dependable mTasks when edit
        	mTask = (Task)mBundle.get("task");
        	Iterator<Task> irr = mTasks.iterator();
        	while (irr.hasNext()) {
        		Task nextTask = irr.next();
        		if((nextTask.getServerId() == mTask.getServerId()) || (nextTask.getDueDate() == null)) {
        			irr.remove();
        		}
        	}
        	mEdTitle.setText(mTask.getName());
        	mTxCreateUnder.setText(mProj.getName());
        	User[] users = dbUtils.tasksUsersDelegate.getUsers(mTask.getServerId());
        	if(users!=null) {
        		for(User user : users) {
        			findAddOwnerView(user);
        		}
        	}
        	mEdRemarks.setText(mTask.getNote());
        	mTaskDue = mTask.getDueDate();
        	if(mTaskDue == null) {
        		if((!mTask.getDependOn().equals("[]"))) {
                	dependOnIds = getDependedTaskFromJSON(mTask.getDependOn());
            		findDependencyView(dependOnIds);
        		} else {
        			findUndeterminedView();
        		}
        	} else {
        		mCalendar.setTime(mTaskDue);
        		mYear = mCalendar.get(Calendar.YEAR);
        		mMonth = mCalendar.get(Calendar.MONTH); // Month is 0 based so add 1
        		mDay = mCalendar.get(Calendar.DATE);
        		findAssignDateView();
        	}
        }

        mBtnAssignDate.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDateChooser.removeAllViews();
        		findAssignDateView();
      	  	}
    	});

        mBtnDependency.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDateChooser.removeAllViews();
        		findDependencyView(dependOnIds);
      	  	}
    	});
        
        mBtnUndetermined.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDateChooser.removeAllViews();
        		findUndeterminedView();
      	  	}
    	});
 
        mBtnFinish.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		switch(mDueType) {       		
        			case ASSIGN_DAY_PANEL:
        				(new CreateTaskTask()).execute(mEdTitle.getText().toString(),
        						mBtnDatePicker.getText().toString(),
        						mEdRemarks.getText().toString(), "0");
        				break;
        			case DEPENDENCY_PANEL:
        				if(!mDependableTasks.isEmpty()) {
        					String duration = ((EditText) mTaskDependency.findViewById(R.id.single_dependency_day_context)).getText().toString();
        					(new CreateTaskTask()).execute(mEdTitle.getText().toString(),
        							"", mEdRemarks.getText().toString(), duration);
        				} else {
        					Toast.makeText(AddSingleTaskActivity.this, R.string.select_a_dependable_task, Toast.LENGTH_LONG).show();
        				}
        				break;
        			case UNDETERMINED_PANEL:
        				(new CreateTaskTask()).execute(mEdTitle.getText().toString(),
        						"", mEdRemarks.getText().toString(), "0");
        				break;
        		}
      	  	}
    	});
        
        mBtnCancel.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		AddSingleTaskActivity.this.finish();
      	  	}
    	});
        
        mBtnAddOwner.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		final String[] usersMail = getUsersMail(mProj.getServerId());
        		Builder dialog = new Builder(AddSingleTaskActivity.this);
    			dialog.setTitle(R.string.add_owner);
    			dialog.setSingleChoiceItems(usersMail, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						findAddOwnerView(getUser(usersMail[which]));
					}
    			});
    			dialog.show();
        	}
    	});
    }
	
	@Override
	public void onStop() {
		super.onStop();
		if (dbUtils != null) {
			dbUtils.close();
			dbUtils = null;
		}
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
        	case DATE_DIALOG_ID:
        		return new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
        				public void onDateSet(DatePicker view, int year, int monthOfYear,
        						int dayOfMonth) {
        							updateDisplay(year, monthOfYear, dayOfMonth);}},
        									mYear, mMonth, mDay);
    	}
		return null;
    }

	private void updateDisplay(int year, int month, int day){

		// Month is 0 based so add 1
		String fromStr = year+" "+(month+1)+" "+day;
		Date date;
		
		try {
			date = mStrToDate.parse(fromStr);
			String toStr = mDateToStr.format(date);
			mBtnDatePicker.setText(toStr);
		} catch (ParseException e) {
			Log.v(TAG, "parse error");
		}
    }

	private void findAssignDateView() {
		mDueType = ASSIGN_DAY_PANEL;
        RelativeLayout add_single_task_assign_date = 
        		(RelativeLayout) mInflater.inflate(R.layout.add_item_assign_date, null);
        mBtnDatePicker = (Button) add_single_task_assign_date.findViewById(R.id.single_date_picker_context);
        mBtnOneDay = (ImageButton) add_single_task_assign_date.findViewById(R.id.single_one_day);
        mBtnsSevenDay = (ImageButton) add_single_task_assign_date.findViewById(R.id.single_seven_day);
        mBtnNDay = (ImageButton) add_single_task_assign_date.findViewById(R.id.single_n_day);
        mBtnDatePicker.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		showDialog(DATE_DIALOG_ID);
      	  	}
    	});
        mBtnOneDay.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		updateDisplay(mYear, mMonth, mDay+1);
      	  	}
    	});
        mBtnsSevenDay.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		updateDisplay(mYear, mMonth, mDay+7);
      	  	}
    	});
        mBtnNDay.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		AlertDialog.Builder dialog = new AlertDialog.Builder(AddSingleTaskActivity.this);
        		View view = mInflater.inflate(R.layout.number_picker_pref, null);
        		dialog.setView(view);
        		dialog.setTitle(R.string.setting_days);
        		dialog.setIcon(R.drawable.ic_dialog_time);
                final NumberPicker mNumberPicker = (NumberPicker) view.findViewById(R.id.pref_num_picker);
                mNumberPicker.setCurrent(1);
                dialog.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					updateDisplay(mYear, mMonth, mDay+mNumberPicker.getCurrent());
    				}
                });
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.dismiss();
    				}
                });
                dialog.show();
        	}
    	});
        
        updateDisplay(mYear, mMonth, mDay);
        mDateChooser.addView(add_single_task_assign_date);
	}
	
	private void findDependencyView(long[] dependOnIds) {
        int i, j, k;
		mDueType = DEPENDENCY_PANEL;
		if (mTasks.isEmpty()) {
			mTaskDependency = new RelativeLayout(this);
			TextView noDependable = new TextView(this, null, android.R.style.TextAppearance_Medium);
			noDependable.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			noDependable.setText(R.string.no_dependable_task);
			mTaskDependency.addView(noDependable);
		} else {
			final String taskNames[] = new String[mTasks.size()];
			for (i=0; i < mTasks.size(); i++) {
				taskNames[i] = mTasks.get(i).getName();
			}
		
			mTaskDependency = (RelativeLayout) mInflater.inflate(R.layout.add_item_dependency, null);
			final TableLayout single_depend_on_view = (TableLayout) mTaskDependency
					.findViewById(R.id.single_depend_on_view);
			ImageButton single_depend_add_task = (ImageButton) mTaskDependency
					.findViewById(R.id.single_depend_add_task);
			if(dependOnIds != null) {
				for(j = 0; j < dependOnIds.length; j++) {
					((EditText) mTaskDependency.findViewById(R.id.single_dependency_day_context)).setText(""+mTask.getDuration());
					final TableRow tr = new TableRow(AddSingleTaskActivity.this);
					mDependableTasks.add(tr);
					tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
					final Spinner sp = new Spinner(AddSingleTaskActivity.this);
					ArrayAdapter<String> depend_on_adapter = new ArrayAdapter<String>(AddSingleTaskActivity.this
							,android.R.layout.simple_spinner_item, taskNames);
					depend_on_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					for(k=0; k< mTasks.size(); k++) {
						if(mTasks.get(k).getServerId() == dependOnIds[j])
							break;
					}
					sp.setAdapter(depend_on_adapter);
					sp.setSelection(depend_on_adapter.getPosition(taskNames[k]));
					sp.setTag("sp");
					ImageButton ib = (ImageButton) mInflater.inflate(R.layout.delete_button, null);
					ib.setOnClickListener( new OnClickListener() {
						@Override
						public void onClick(View v) {
							mDependableTasks.remove(tr);
							single_depend_on_view.removeView(tr);
						}
					});
					tr.addView(sp);
					tr.addView(ib);
					single_depend_on_view.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
				}
			}
			single_depend_add_task.setOnClickListener( new OnClickListener() {
				@Override
        	public void onClick(View v) {
        		final TableRow tr = new TableRow(AddSingleTaskActivity.this);
        		mDependableTasks.add(tr);
        		tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        		final Spinner sp = new Spinner(AddSingleTaskActivity.this);
        		ArrayAdapter<String> depend_on_adapter = new ArrayAdapter<String>(AddSingleTaskActivity.this
        				,android.R.layout.simple_spinner_item, taskNames);
                depend_on_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp.setAdapter(depend_on_adapter);
                sp.setTag("sp");
        		ImageButton ib = (ImageButton) mInflater.inflate(R.layout.delete_button, null);
        		ib.setOnClickListener( new OnClickListener() {
					@Override
					public void onClick(View v) {
						mDependableTasks.remove(tr);
						single_depend_on_view.removeView(tr);
					}
        		});
        		tr.addView(sp);
        		tr.addView(ib);
        		single_depend_on_view.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        				LayoutParams.WRAP_CONTENT));
        	}
        });    
		}
        mDateChooser.addView(mTaskDependency);
	}
	
	private void findUndeterminedView() {
		mDueType = UNDETERMINED_PANEL;
        RelativeLayout add_single_task_undetermined = (RelativeLayout) mInflater.inflate(R.layout.add_item_undetermined, null);
        mDateChooser.addView(add_single_task_undetermined);
	}
	
	private void findAddOwnerView(final User user) {
		final TableRow tr = new TableRow(AddSingleTaskActivity.this);
		mTaskOwners.add(user);
		tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView txName = new TextView(AddSingleTaskActivity.this);
		txName.setTextAppearance(AddSingleTaskActivity.this, android.R.style.TextAppearance_Medium);
		txName.setText(user.name);
		ImageButton ib = (ImageButton) mInflater.inflate(R.layout.delete_button, null);
		ib.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTaskOwners.remove(user);
				mUserField.removeView(tr);
			}
		});
		tr.addView(txName);
		tr.addView(ib);
		mUserField.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}
	
    private void findViews() {
    	mDateChooser = (RelativeLayout)findViewById(R.id.single_date_chooser);
    	mEdTitle = (EditText)findViewById(R.id.single_title_context);
    	mTxCreateUnder = (TextView)findViewById(R.id.single_task_create_under_context);
    	mBtnAssignDate = (ImageButton)findViewById(R.id.single_date);
    	mBtnDependency = (ImageButton)findViewById(R.id.single_dependency);
    	mBtnUndetermined = (ImageButton)findViewById(R.id.single_undetermined);
    	mBtnAddOwner = (ImageButton)findViewById(R.id.single_owner_add);
    	mUserField = (TableLayout)findViewById(R.id.owners_field);
    	mEdRemarks = (EditText)findViewById(R.id.single_remarks_context);
    	mBtnFinish = (Button)findViewById(R.id.single_additem_finish);
    	mBtnCancel = (Button)findViewById(R.id.single_additem_cancel);
    }
    
    // get depended tasks in array of Long
    private Long[] getDependedTasks() {
		Set<Long> set = new TreeSet<Long>();
		for (int i=0; i < mDependableTasks.size(); i++) {
			Long taskEventId = mTasks.get((int)((Spinner) mDependableTasks.get(i).findViewWithTag("sp"))
					.getSelectedItemId()).getServerId();
			set.add(taskEventId);
		}
		return set.toArray(new Long[set.size()]);
    }
    
    // get depended duration
    private int getDependedDuration() {
		int duration = Integer.parseInt((((EditText) mTaskDependency
				.findViewById(R.id.single_dependency_day_context)).getText().toString()));
		return duration;
    }
    
    private long[] getDependedTaskFromJSON(String dependArray) {
    	long dependencies[] = null;
    	try {
			JSONArray jsonArray = new JSONArray(dependArray);
			dependencies = new long[jsonArray.length()];
			for(int i=0; i<dependencies.length; i++) {
				dependencies[i] = jsonArray.getLong(i);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return dependencies;
    }
    
    private String dependedTasksToJSONArray(Long dependedTasks[]) {
    	JSONArray jsonArray = new JSONArray(Arrays.asList(getDependedTasks()));
    	return jsonArray.toString();
    }
    
    private Long[] getOwnersId() {
		Set<Long> set = new TreeSet<Long>();
		for (int i=0; i < mTaskOwners.size(); i++) {
			long ownerId = mTaskOwners.get(i).serverId;
			set.add(ownerId);
		}
		return set.toArray(new Long[set.size()]);
    }
    
    private String[] getUsersMail(long projId) {
    	List<User> members = null;
		try {
			QueryBuilder<User, Integer> queryBuilder = dbUtils.userDao.queryBuilder();
			queryBuilder.where().eq("project_id", projId);
			members = dbUtils.userDao.query(queryBuilder.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String[] emails = new String[members.size()];
		for(int i = 0; i < members.size(); i++) {
			emails[i] = members.get(i).email;
		}
		return emails;
    }
    
    private User getUser(String email) {
    	List<User> members = null;
		try {
			QueryBuilder<User, Integer> queryBuilder = dbUtils.userDao.queryBuilder();
			queryBuilder.where().eq("email", email);
			members = dbUtils.userDao.query(queryBuilder.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return members.get(0);
    }
  
  	private class CreateTaskTask extends AsyncTask<String, Void, Integer> {
		private ProgressDialog dialog;
		private Exception exception;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(AddSingleTaskActivity.this);
			dialog.setMessage(getString(R.string.processing));
			dialog.show();
		}
		
		@Override
		protected Integer doInBackground(String... params) {
			try {
		    	if (mBundle.getInt("addOrEdit") == 0) {
		    		Task task = null;
		    		if (!params[1].equals("")) {
						int serverId = ArtApi.getInstance(AddSingleTaskActivity.this)
								.createTask(mProjId, params[0], getOwnersId(), mDateToStr.parse(params[1]), params[2]);
		    			task = new Task(mProjId, serverId, params[0], mDateToStr.parse(params[1]), getOwnersId(), params[2], false, dependedTasksToJSONArray(getDependedTasks()), Integer.parseInt(params[3]), new Date());
		    			dbUtils.tasksDelegate.insert(task);
		    			dbUtils.tasksUsersDelegate.insertSingleTask(task);
		    		} else {
		    			int serverId = ArtApi.getInstance(AddSingleTaskActivity.this)
								.createTask(mProjId, params[0], getOwnersId(), null, params[2]);
						task = new Task(mProjId, serverId, params[0], null, getOwnersId(), params[2], false, dependedTasksToJSONArray(getDependedTasks()), Integer.parseInt(params[3]), new Date());
						dbUtils.tasksDelegate.insert(task);
						dbUtils.tasksUsersDelegate.insertSingleTask(task);
						if(mDueType == DEPENDENCY_PANEL) {
			    			ArtApi.getInstance(AddSingleTaskActivity.this)
								.setDependencies(task.getServerId(), getDependedTasks(), getDependedDuration());
			    		}
		    		}
		    	} else {
		    		if (!params[1].equals("")) {
		    			Task task = new Task(mTask.getServerId(), mTask.getProjId(),
			    				mTask.getServerId(), params[0], mDateToStr.parse(params[1]), getOwnersId(), params[2], false, dependedTasksToJSONArray(getDependedTasks()), Integer.parseInt(params[3]), new Date());
		    			dbUtils.tasksDelegate.update(task);
						dbUtils.tasksUsersDelegate.deleteUnderTask(task.getServerId());
						dbUtils.tasksUsersDelegate.insertSingleTask(task);
		    			
		    		} else {
		    			Task task = new Task(mTask.getServerId(), mTask.getProjId(),
		    					mTask.getServerId(), params[0], null, getOwnersId(), params[2], false, dependedTasksToJSONArray(getDependedTasks()), Integer.parseInt(params[3]), new Date());
		    			dbUtils.tasksDelegate.update(task);
						dbUtils.tasksUsersDelegate.deleteUnderTask(task.getServerId());
						dbUtils.tasksUsersDelegate.insertSingleTask(task);
		    		}
		    		Intent syncIntent = new Intent(AddSingleTaskActivity.this, SyncService.class);
		    		startService(syncIntent);
		    	}
			} catch (ServerException e) {
				exception = e;
			} catch (ConnectionFailException e) {
				exception = e;
			} catch (ParseException e) {
				exception = e;
			}
			return 0;
		}
		
		@Override
        protected void onPostExecute(Integer i) {
			dialog.dismiss();
			if(exception instanceof ServerException) {
				Toast.makeText(AddSingleTaskActivity.this, getString(R.string.cannot_add_task_server_problem) + exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			if(exception instanceof ConnectionFailException) {
				Toast.makeText(AddSingleTaskActivity.this, getString(R.string.cannot_add_task_connection_problem), Toast.LENGTH_LONG).show();
				return;
			}
			dbUtils.close();
			AddSingleTaskActivity.this.finish();
		}
	}
}