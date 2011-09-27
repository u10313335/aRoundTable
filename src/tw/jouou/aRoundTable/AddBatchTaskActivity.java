package tw.jouou.aRoundTable;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;

import com.j256.ormlite.stmt.QueryBuilder;

import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import tw.jouou.aRoundTable.widget.NumberPicker;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class AddBatchTaskActivity extends Activity {

	
	private static final int DATE_DIALOG_ID = 0;
    private static final int ASSIGN_DAY_PANEL = 0;
    private static final int DEPENDENCY_PANEL = 1;
    private static final int UNDETERMINED_PANEL = 2;
    private static String TAG = "AddBatchTaskActivity";
	private DBUtils dbUtils;
	private Bundle mBundle;
	private String mProjName;
	private Project mProj;
	private int mDueType = ASSIGN_DAY_PANEL;
	private LinkedList<TableRow> mDependableTasks = new LinkedList<TableRow>();
	private LinkedList<TaskField> mOwners = new LinkedList<TaskField>();
	private long mProjId;
	private LayoutInflater mInflater;
	private RelativeLayout mDateChooser;
	private RelativeLayout mTaskDependency;
	private List<Task> mTasks = null;
	private LinearLayout mAddBatch;
    private TextView mTxCreateUnder;
    private ImageButton mBtnAddBatch;
    private ImageButton mBtnOneDay;
    private ImageButton mBtnsSevenDay;
    private ImageButton mBtnNDay;
    private ImageButton mBtnAssignDate;
    private ImageButton mBtnDependency;
    private ImageButton mBtnUndetermined;
    private Button mBtnDatePicker;
    private Button mBtnFinish;
    private Button mBtnCancel;
    private EditText mEdRemarks;
    private SimpleDateFormat mDateToStr, mStrToDate;
    private Calendar mCalendar = Calendar.getInstance();
    private int mYear;
    private int mMonth;
    private int mDay;	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_batch_task_tab);
		 
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
        mProjName = mProj.getName();
        mProjId = mProj.getServerId();
        mTxCreateUnder.setText(mProjName);
        findAssignDateView();
        updateDisplay(mYear, mMonth, mDay);
        
        mBtnAddBatch.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		findAddBatchView();
      	  	}
    	});

        mBtnAssignDate.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDueType = ASSIGN_DAY_PANEL;
        		mDateChooser.removeAllViews();
        		findAssignDateView();
      	  	}
    	});

        mBtnDependency.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDueType = DEPENDENCY_PANEL;
        		mDateChooser.removeAllViews();
        		findDependencyView();
      	  	}
    	});
        
        mBtnUndetermined.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDueType = UNDETERMINED_PANEL;
        		mDateChooser.removeAllViews();
        		findUndeterminedView();
      	  	}
    	});
 
        mBtnFinish.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		String[] titles = getTasksTitle(mOwners);
        		Long[] owners = getOwnersId(mOwners);
        		Tasks tasks = null;
        		switch(mDueType) {
        			case ASSIGN_DAY_PANEL:
        				tasks = new Tasks(titles, owners, mBtnDatePicker.getText().toString(), mEdRemarks.getText().toString(), 0);
        				(new CreateTaskTask()).execute(tasks);
        				break;
        			case DEPENDENCY_PANEL:
        				if(!mDependableTasks.isEmpty()) {
        					String duration = ((EditText) mTaskDependency.findViewById(R.id.single_dependency_day_context)).getText().toString();
        					tasks = new Tasks(titles, owners, "", mEdRemarks.getText().toString(), Integer.parseInt(duration));
        					(new CreateTaskTask()).execute(tasks);
        				} else {
        					Toast.makeText(AddBatchTaskActivity.this, R.string.select_a_dependable_task, Toast.LENGTH_LONG).show();
        				}
        				break;
        			case UNDETERMINED_PANEL:
        				tasks = new Tasks(titles, owners, "", mEdRemarks.getText().toString(), 0);
        				(new CreateTaskTask()).execute(tasks);
        				break;
        		}
      	  	}
    	});
        
        mBtnCancel.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		AddBatchTaskActivity.this.finish();
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
	
	private void findAddBatchView() {
		final TableLayout addBatchField = (TableLayout) mInflater.inflate(R.layout.add_batch_field, null);
		final TableRow titleRow = (TableRow) addBatchField.findViewById(R.id.batch_task_title_row);
		final TableRow ownerSelectRow = (TableRow) addBatchField.findViewById(R.id.batch_task_owner_row);
		final TaskField taskField = new TaskField(titleRow);
		mOwners.add(taskField);
		//widgets for titleRow
		EditText ed = (EditText) titleRow.findViewById(R.id.batch_task_title_context);
		ed.setTag("title");
		ImageButton addOwner = (ImageButton) ownerSelectRow.findViewById(R.id.batch_btn_add_owner);
		addOwner.setOnClickListener(new OnClickListener() {
	        	@Override
	      	  	public void onClick(View v) {
	        		final String[] usersMail = getUsersMail(mProj.getServerId());
	        		Builder dialog = new Builder(AddBatchTaskActivity.this);
	    			dialog.setTitle(R.string.add_owner);
	    			dialog.setSingleChoiceItems(usersMail, -1, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							addBatchField.removeView(ownerSelectRow);
							final User member = getUser(usersMail[which]);
							taskField.setOwner(member);
							final TableRow ownerDisplayRow = (TableRow) mInflater.inflate(R.layout.add_batch_display, null);
							TextView tx = (TextView) ownerDisplayRow.findViewById(R.id.batch_task_owner_context);
							tx.setText(member.name);
							ImageButton delOwner = (ImageButton) ownerDisplayRow.findViewById(R.id.batch_owner_delete);
							delOwner.setOnClickListener( new OnClickListener() {
								@Override
								public void onClick(View v) {
									taskField.setOwner(null);
									addBatchField.removeView(ownerDisplayRow);
									addBatchField.addView(ownerSelectRow);
								}
							});
							addBatchField.addView(ownerDisplayRow);
						}
	    			});
	    			dialog.show();
	        	}
	    });
		
		ImageButton delTask = (ImageButton) titleRow.findViewById(R.id.batch_task_delete);
		delTask.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				mOwners.remove(taskField);
				mAddBatch.removeView(addBatchField);
			}
		});
		
		//add rows onto add_batch panel
		mAddBatch.addView(addBatchField);
	}

	private void findAssignDateView() {
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
        		AlertDialog.Builder dialog = new AlertDialog.Builder(AddBatchTaskActivity.this);
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

	private void findDependencyView() {
		if (mTasks.isEmpty()) {
			mTaskDependency = new RelativeLayout(this);
			TextView noDependable = new TextView(this, null, android.R.style.TextAppearance_Medium);
			noDependable.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			noDependable.setText(R.string.no_dependable_task);
			mTaskDependency.addView(noDependable);
		} else {
			final String taskNames[] = new String[mTasks.size()];
			for (int i=0; i < mTasks.size(); i++) {
				taskNames[i] = mTasks.get(i).getName();
			}
			mTaskDependency = (RelativeLayout) mInflater.inflate(R.layout.add_item_dependency, null);
			final TableLayout single_depend_on_view = (TableLayout) mTaskDependency
					.findViewById(R.id.single_depend_on_view);
			ImageButton batch_depend_add_task = (ImageButton) mTaskDependency
					.findViewById(R.id.single_depend_add_task);
			batch_depend_add_task.setOnClickListener( new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		final TableRow tr = new TableRow(AddBatchTaskActivity.this);
        		mDependableTasks.add(tr);
        		tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        		final Spinner sp = new Spinner(AddBatchTaskActivity.this);
        		ArrayAdapter<String> depend_on_adapter = new ArrayAdapter<String>(AddBatchTaskActivity.this
        				,android.R.layout.simple_spinner_item, taskNames);
                depend_on_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp.setAdapter(depend_on_adapter);
                sp.setTag("sp");
        		ImageButton ib = new ImageButton(AddBatchTaskActivity.this);
        		ib.setImageResource(R.drawable.ic_delete);
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
        RelativeLayout add_single_task_undetermined = (RelativeLayout) mInflater.inflate(R.layout.add_item_undetermined, null);
        mDateChooser.addView(add_single_task_undetermined);
	}
	
    private void findViews() {
    	mAddBatch = (LinearLayout)findViewById(R.id.add_batch);
    	mBtnAddBatch = (ImageButton)findViewById(R.id.batch_add);
    	mDateChooser = (RelativeLayout)findViewById(R.id.batch_time_chooser);
    	mTxCreateUnder = (TextView)findViewById(R.id.batch_create_under_context);
    	mBtnAssignDate = (ImageButton)findViewById(R.id.batch_date);
    	mBtnDependency = (ImageButton)findViewById(R.id.batch_dependency);
    	mBtnUndetermined = (ImageButton)findViewById(R.id.batch_undetermined);
    	mEdRemarks = (EditText)findViewById(R.id.batch_remarks_context);
    	mBtnFinish = (Button)findViewById(R.id.batch_additem_finish);
    	mBtnCancel = (Button)findViewById(R.id.batch_additem_cancel);
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
    
    private String dependedTasksToJSONArray(Long dependedTasks[]) {
    	JSONArray jsonArray = new JSONArray(Arrays.asList(getDependedTasks()));
    	return jsonArray.toString();
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
    
    private String[] getTasksTitle(LinkedList<TaskField> taskField) {
		List<String> list = new LinkedList<String>();
		for (int i=0; i < taskField.size(); i++) {
			String taskTitle = ((EditText) taskField.get(i).title.findViewWithTag("title")).getText().toString();
			list.add(taskTitle);
		}
		return list.toArray(new String[list.size()]);
    }
    
    private Long[] getOwnersId(LinkedList<TaskField> taskField) {
		List<Long> list = new LinkedList<Long>();
		for (int i=0; i < taskField.size(); i++) {
			User owner =  taskField.get(i).owner;
			if(owner!=null) {
				list.add(owner.serverId);
			} else {
				list.add((long)-1);
			}
		}
		return list.toArray(new Long[list.size()]);
    }
    
    
    private class Tasks extends Object {
    	private String [] titles;
    	private Long [] owners;
    	private String due;
    	private String note;
    	private int duration;
    	
    	Tasks(String[] titles, Long[] owners, String due, String note, int duration) {
    		this.titles = titles;
    		this.owners = owners;
    		this.due = due;
    		this.note = note;
    		this.duration = duration;
    	}
    }
    
    private class TaskField extends Object {
    	private TableRow title;
    	private User owner;
    	
    	TaskField(TableRow title) {
    		this.title = title;
    	}
    	
    	private void setOwner(User owner) {
    		this.owner = owner;
    	}
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
    
    
  	private class CreateTaskTask extends AsyncTask<Tasks, Void, Integer> {
		private ProgressDialog dialog;
		private Exception exception;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(AddBatchTaskActivity.this);
			dialog.setMessage(getString(R.string.processing));
			dialog.show();
		}
		
		@Override
		protected Integer doInBackground(Tasks... params) { 	
			try {
		    	if (!params[0].due.equals("")) {
		    		for(int i=0; i < mOwners.size(); i++) {
		    			int serverId = ArtApi.getInstance(AddBatchTaskActivity.this)
								.createTask(mProjId, params[0].titles[i], params[0].owners[i], mDateToStr.parse(params[0].due), params[0].note);
		    			Task task = new Task(mProjId, serverId, params[0].titles[i], mDateToStr.parse(params[0].due), params[0].owners[i], params[0].note, false, dependedTasksToJSONArray(getDependedTasks()), params[0].duration, new Date());
		    			dbUtils.tasksDelegate.insert(task);
		    			dbUtils.tasksUsersDelegate.insertBatchTask(task);
		    		}
		    	} else {
		    		for(int i=0; i < mOwners.size(); i++) {
		    			int serverId = ArtApi.getInstance(AddBatchTaskActivity.this)
								.createTask(mProjId, params[0].titles[i], params[0].owners[i], null, params[0].note);
		    			Task task = new Task(mProjId, serverId, params[0].titles[i], null, params[0].owners[i], params[0].note, false, dependedTasksToJSONArray(getDependedTasks()), params[0].duration, new Date());
		    			dbUtils.tasksDelegate.insert(task);
		    			dbUtils.tasksUsersDelegate.insertBatchTask(task);
		    			if( mDueType == DEPENDENCY_PANEL) {
			    			ArtApi.getInstance(AddBatchTaskActivity.this)
								.setDependencies(task.getServerId(), getDependedTasks(), getDependedDuration());
			    		}
		    		}
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
				Toast.makeText(AddBatchTaskActivity.this, getString(R.string.cannot_add_task_server_problem) + exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			if(exception instanceof ConnectionFailException) {
				Toast.makeText(AddBatchTaskActivity.this, getString(R.string.cannot_add_task_connection_problem), Toast.LENGTH_LONG).show();
				return;
			}
			dbUtils.close();
			AddBatchTaskActivity.this.finish();
		}
	}
  	  
}