package tw.jouou.aRoundTable;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.j256.ormlite.stmt.QueryBuilder;

import tw.jouou.aRoundTable.bean.Member;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
	private Bundle mBundle;
	private String mProjName;
	private Project mProj;
	private Date mTaskDue;
	private int mDueType = ASSIGN_DAY_PANEL;
	private boolean mPlusMinusFlag = true; //fasle:minus ; true:plus
	private LinkedList<Member> mTaskOwners = new LinkedList<Member>();
	private LinkedList<TableRow> mDependableTasks = new LinkedList<TableRow>();
	private long mProjId;
	private LayoutInflater mInflater;
	private RelativeLayout mDateChooser;
	private TableLayout mMemberField;
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
    private AutoCompleteTextView mAutoOwner;
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
    		dbUtils = new DBUtils(this);
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
        mAutoOwner.setAdapter(new ArrayAdapter<String>(AddSingleTaskActivity.this,
        		R.layout.email_autocomplete_item, getMembersMail(mProj.getServerId())));
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
        	mEdRemarks.setText(mTask.getNote());
        	mTaskDue = mTask.getDueDate();
        	if(mTaskDue == null) {
        		findUndeterminedView();
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
        		findDependencyView();
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
        							mEdRemarks.getText().toString());
        				break;
        			case DEPENDENCY_PANEL:
        				break;
        			case UNDETERMINED_PANEL:
        				(new CreateTaskTask()).execute(mEdTitle.getText().toString(),
        							"", mEdRemarks.getText().toString());
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
        		String getString = mAutoOwner.getText().toString();
        		if(!getString.equals("")) {
        			findAddMemberView(getMember(getString));
        			mAutoOwner.getText().clear();
        		}
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
        		(RelativeLayout) mInflater.inflate(R.layout.add_item_assign_date, null)
        		.findViewById(R.id.add_single_task_assign_date);
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

	private void findDependencyView() {
		mDueType = DEPENDENCY_PANEL;
		RelativeLayout add_single_task_dependency;
		if (mTasks.isEmpty()) {
			add_single_task_dependency = new RelativeLayout(this);
			TextView noDependable = new TextView(this, null, android.R.style.TextAppearance_Medium);
			noDependable.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			noDependable.setText(R.string.no_dependable_task);
			add_single_task_dependency.addView(noDependable);
		} else {
			final String taskNames[] = new String[mTasks.size()];
			for (int i=0; i < mTasks.size(); i++) {
				taskNames[i] = mTasks.get(i).getName();
			}
		
        add_single_task_dependency = (RelativeLayout) mInflater.inflate(R.layout.add_item_dependency, null)
        		.findViewById(R.id.add_single_task_dependency);
        final TableLayout single_depend_on_view = (TableLayout) add_single_task_dependency
				.findViewById(R.id.single_depend_on_view);
        Spinner single_dependency_plus_minus = (Spinner) add_single_task_dependency.findViewById(R.id.single_dependency_plus_minus);
        ArrayAdapter<String> plus_minus_adapter = new ArrayAdapter<String>(this,
        		android.R.layout.simple_spinner_item, new String[]{getString(R.string.plus), getString(R.string.minus)});
        plus_minus_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        single_dependency_plus_minus.setAdapter(plus_minus_adapter);
        single_dependency_plus_minus.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view,
					int position, long id) {
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        });
        ImageButton single_depend_add_task = (ImageButton) add_single_task_dependency
				.findViewById(R.id.single_depend_add_task);
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
        		ImageButton ib = new ImageButton(AddSingleTaskActivity.this);
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
        mDateChooser.addView(add_single_task_dependency);
	}
	
	private void findUndeterminedView() {
		mDueType = UNDETERMINED_PANEL;
        RelativeLayout add_single_task_undetermined = (RelativeLayout) mInflater.inflate(R.layout.add_item_undetermined, null)
        		.findViewById(R.id.add_single_task_undetermined);
        mDateChooser.addView(add_single_task_undetermined);
	}
	
	private void findAddMemberView(final Member member) {
		final TableRow tr = new TableRow(AddSingleTaskActivity.this);
		mTaskOwners.add(member);
		tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView txName = new TextView(AddSingleTaskActivity.this);
		txName.setTextAppearance(AddSingleTaskActivity.this, android.R.style.TextAppearance_Medium);
		txName.setText(member.name);
		ImageButton ib = new ImageButton(AddSingleTaskActivity.this);
		ib.setImageResource(R.drawable.ic_delete);
		ib.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTaskOwners.remove(member);
				mMemberField.removeView(tr);
			}
		});
		tr.addView(txName);
		tr.addView(ib);
		mMemberField.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}
	
    private void findViews() {
    	mDateChooser = (RelativeLayout)findViewById(R.id.single_date_chooser);
    	mEdTitle = (EditText)findViewById(R.id.single_title_context);
    	mTxCreateUnder = (TextView)findViewById(R.id.single_task_create_under_context);
    	mBtnAssignDate = (ImageButton)findViewById(R.id.single_date);
    	mBtnDependency = (ImageButton)findViewById(R.id.single_dependency);
    	mBtnUndetermined = (ImageButton)findViewById(R.id.single_undetermined);
    	mAutoOwner = (AutoCompleteTextView)findViewById(R.id.single_owneradd_context);
    	mBtnAddOwner = (ImageButton)findViewById(R.id.single_owner_add);
    	mMemberField = (TableLayout)findViewById(R.id.owners_field);
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
    
    private Long[] getOwnersId() {
		Set<Long> set = new TreeSet<Long>();
		for (int i=0; i < mTaskOwners.size(); i++) {
			long ownerId = mTaskOwners.get(i).serverId;
			set.add(ownerId);
		}
		return set.toArray(new Long[set.size()]);
    }
    
    private String[] getMembersMail(long projId) {
    	List<Member> members = null;
		try {
			QueryBuilder<Member, Integer> queryBuilder = dbUtils.memberDao.queryBuilder();
			queryBuilder.where().eq("project_id", projId);
			members = dbUtils.memberDao.query(queryBuilder.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String[] emails = new String[members.size()];
		for(int i = 0; i < members.size(); i++) {
			emails[i] = members.get(i).email;
		}
		return emails;
    }
    
    private Member getMember(String email) {
    	List<Member> members = null;
		try {
			dbUtils = new DBUtils(this);
			QueryBuilder<Member, Integer> queryBuilder = dbUtils.memberDao.queryBuilder();
			queryBuilder.where().eq("email", email);
			members = dbUtils.memberDao.query(queryBuilder.prepare());
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
		    	if (dbUtils == null) {
		    		dbUtils = new DBUtils(AddSingleTaskActivity.this);
		    	}
		    	if (mBundle.getInt("addOrEdit") == 0) {
		    		if (!params[1].equals("")) {
						int serverId = ArtApi.getInstance(AddSingleTaskActivity.this)
								.createTask(mProjId, params[0], mDateToStr.parse(params[1]), params[2]);
		    			Task task = new Task(mProjId, serverId, params[0], mDateToStr.parse(params[1]), getOwnersId(), params[2], false, new Date());
		    			dbUtils.tasksDelegate.insert(task);
		    			dbUtils.tasksMembersDelegate.insert(task);
		    		} else {
		    			int serverId = ArtApi.getInstance(AddSingleTaskActivity.this)
								.createTask(mProjId, params[0], null, params[3]);
						Task task = new Task(mProjId, serverId, params[0], null, params[2], false, new Date());
						dbUtils.tasksDelegate.insert(task);
						dbUtils.tasksMembersDelegate.insert(task);
		    		}	
		    	} else {
		    		if (!params[1].equals("")) {
		    			Task task = new Task(mTask.getServerId(), mTask.getProjId(),
			    				mTask.getServerId(), params[0], mDateToStr.parse(params[1]), params[2], false, new Date());
		    			dbUtils.tasksDelegate.update(task);
		    		} else {
		    			Task task = new Task(mTask.getServerId(), mTask.getProjId(),
		    					mTask.getServerId(), params[0], null, params[2], false, new Date());
		    			dbUtils.tasksDelegate.update(task);
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