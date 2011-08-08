package tw.jouou.aRoundTable;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.lib.ArtApi;
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
	
	private DBUtils dbUtils;
	private TaskEvent mTask;
	private Bundle mBundle;
	private String mProjName;
	private Project mProj;
	private Date mTaskDue;
	private int mDueType = 0; //0:assign day ; 1:dependency; 2:undetermined
	private int mPlusMinusFlag = 1; //1:plus ; 0:minus
	private LinkedList<TableRow> mDependableTasks = new LinkedList<TableRow>();
	private long mProjId;
	private LayoutInflater mInflater;
	private RelativeLayout mDateChooser;
	private List<TaskEvent> mTasks = null;
    private EditText mEdTitle;
    private TextView mTxCreateUnder;
    private ImageButton mBtnOneDay;
    private ImageButton mBtnsSevenDay;
    private ImageButton mBtnNDay;
    private ImageButton mBtnAssignDate;
    private ImageButton mBtnDependency;
    private ImageButton mBtnUndetermined;
    private ImageButton single_owner_add;
    private Button mBtnDatePicker;
    private Button mBtnFinish;
    private Button mBtnCancel;
    private EditText mEdOwner;
    private EditText mEdRemarks;
    private SimpleDateFormat mDateToStr, mStrToDate;
    private Calendar mCalendar = Calendar.getInstance();
    private static final int DATE_DIALOG_ID = 0;
    private int mYear;
    private int mMonth;
    private int mDay;
    
    private static String TAG = "AddSingleTaskActivity";
	
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
        try {
    		mTasks = dbUtils.taskeventsDelegate.get(mProj.getId());
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
        if (mBundle.getInt("type") == 0) {
            mProjName = mProj.getName();
            mProjId = mProj.getId();
            mTxCreateUnder.setText(mProjName);
            findSingleTaskAssignDateView();
            updateDisplay(mYear, mMonth, mDay);
        } else {
        	// remove itself from dependable mTasks when edit
        	mTask = (TaskEvent)mBundle.get("taskevent");
        	Iterator<TaskEvent> irr = mTasks.iterator();
        	while (irr.hasNext()) {
        	    TaskEvent nextTaskEvent = irr.next();
        	    if(nextTaskEvent.getId() == mTask.getId()) {
        	    	irr.remove();
        	    }
        	}
        	mEdTitle.setText(mTask.getName());
        	mTxCreateUnder.setText(mBundle.getString("projname"));
        	mEdRemarks.setText(mTask.getNote());
        	mTaskDue = mTask.getDue();
        	if(mTaskDue == null) {
        		findSingleTaskUndeterminedView();
        	} else {
        		mCalendar.setTime(mTaskDue);
        		mYear = mCalendar.get(Calendar.YEAR);
        		mMonth = mCalendar.get(Calendar.MONTH); // Month is 0 based so add 1
        		mDay = mCalendar.get(Calendar.DATE);
        		findSingleTaskAssignDateView();
        	}
        }

        mBtnAssignDate.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDueType = 0;
        		mDateChooser.removeAllViews();
        		findSingleTaskAssignDateView();
      	  	}
    	});

        mBtnDependency.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDueType = 1;
        		mDateChooser.removeAllViews();
        		findSingleTaskDependencyView();
      	  	}
    	});
        
        mBtnUndetermined.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDueType = 2;
        		mDateChooser.removeAllViews();
        		findSingleTaskUndeterminedView();
      	  	}
    	});
 
        mBtnFinish.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		switch(mDueType) {       		
        			case 0:
        				(new CreateItemEventTask()).execute(mEdTitle.getText().toString(), 
        							mBtnDatePicker.getText().toString(),
        							mEdRemarks.getText().toString());
        				break;
        			case 1:
        				break;
        			case 2:
        				(new CreateItemEventTask()).execute(mEdTitle.getText().toString(),
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
        
        single_owner_add.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		// TODO:add addowner function here
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
	
	
	private void findSingleTaskAssignDateView() {
        RelativeLayout add_single_task_assign_date = 
        		(RelativeLayout) mInflater.inflate(R.layout.add_single_task_assign_date, null)
        		.findViewById(R.id.add_single_task_assign_date);
        mBtnDatePicker = (Button) add_single_task_assign_date.findViewById(R.id.single_date_picker_context);
        mBtnOneDay = (ImageButton) add_single_task_assign_date.findViewById(R.id.single_one_day);
        mBtnsSevenDay = (ImageButton) add_single_task_assign_date.findViewById(R.id.single_seven_day);
        mBtnNDay = (ImageButton) add_single_task_assign_date.findViewById(R.id.single_n_day);
        mBtnDatePicker.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		Log.v(TAG, "[be]: "+mYear+""+mMonth+""+mDay );
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
        		dialog.setTitle(getString(R.string.setting_days));
        		dialog.setIcon(R.drawable.ic_dialog_time);
                final NumberPicker mNumberPicker = (NumberPicker) view.findViewById(R.id.pref_num_picker);
                mNumberPicker.setCurrent(1);
                dialog.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					updateDisplay(mYear, mMonth, mDay+mNumberPicker.getCurrent());
    				}
                });
                dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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

	private void findSingleTaskDependencyView() {
		RelativeLayout add_single_task_dependency;
		if (mTasks.isEmpty()) {
			add_single_task_dependency = new RelativeLayout(this);
			TextView noDependable = new TextView(this, null, android.R.style.TextAppearance_Medium);
			noDependable.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			noDependable.setText("無可相依工作");
			add_single_task_dependency.addView(noDependable);
		} else {
			final String taskNames[] = new String[mTasks.size()];
			for (int i=0; i < mTasks.size(); i++) {
				taskNames[i] = mTasks.get(i).getName();
			}
		
        add_single_task_dependency = (RelativeLayout) mInflater.inflate(R.layout.add_single_task_dependency, null)
        		.findViewById(R.id.add_single_task_dependency);
        final TableLayout single_depend_on_view = (TableLayout) add_single_task_dependency
				.findViewById(R.id.single_depend_on_view);
        Spinner single_dependency_plus_minus = (Spinner) add_single_task_dependency.findViewById(R.id.single_dependency_plus_minus);
        ArrayAdapter<String> plus_minus_adapter = new ArrayAdapter<String>(this,
        		android.R.layout.simple_spinner_item, new String[]{"加","減"});
        plus_minus_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        single_dependency_plus_minus.setAdapter(plus_minus_adapter);
        single_dependency_plus_minus.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view,
					int position, long id) {
				mPlusMinusFlag = position;
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				mPlusMinusFlag = 1;
			}
        });
        ImageButton single_depend_add_task = (ImageButton) add_single_task_dependency
				.findViewById(R.id.single_depend_add_task);
        single_depend_add_task.setOnClickListener( new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		final TableRow tr = new TableRow(AddSingleTaskActivity.this);
        		mDependableTasks.add(tr);
        		tr.setTag("tr");
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
	
	private void findSingleTaskUndeterminedView() {
        RelativeLayout add_single_task_undetermined = (RelativeLayout) mInflater.inflate(R.layout.add_single_task_undetermined, null)
        		.findViewById(R.id.add_single_task_undetermined);
        mDateChooser.addView(add_single_task_undetermined);
	}
	
    private void findViews() {
    	mDateChooser = (RelativeLayout)findViewById(R.id.single_date_chooser);
    	mEdTitle = (EditText)findViewById(R.id.single_title_context);
    	mTxCreateUnder = (TextView)findViewById(R.id.single_item_create_under_context);
    	mBtnAssignDate = (ImageButton)findViewById(R.id.single_date);
    	mBtnDependency = (ImageButton)findViewById(R.id.single_dependent);
    	mBtnUndetermined = (ImageButton)findViewById(R.id.single_undetermined);
    	mEdOwner = (EditText)findViewById(R.id.single_owneradd_context);
    	single_owner_add = (ImageButton)findViewById(R.id.single_owner_add);
    	mEdRemarks = (EditText)findViewById(R.id.single_remarks_context);
    	mBtnFinish = (Button)findViewById(R.id.single_additem_finish);
    	mBtnCancel = (Button)findViewById(R.id.single_additem_cancel);
    }
    
    // get depended mTasks in array of Long
    private Long[] getDependedTasks() {
		Set<Long> set = new TreeSet<Long>();
		for (int i=0; i < mDependableTasks.size(); i++) {
			Long taskEventId = mTasks.get((int)((Spinner) mDependableTasks.get(i).findViewWithTag("sp"))
					.getSelectedItemId()).getId();
			set.add(taskEventId);
		}
		return set.toArray(new Long[set.size()]);
    }
    
    
  	// not tested after add new features
  	private class CreateItemEventTask extends AsyncTask<String, Void, Integer> {
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
		    	if (mBundle.getInt("type") == 0) {
		    		mTask = new TaskEvent(mProjId, 0, params[0], params[1], params[2], 0);
					mTask.setId(dbUtils.taskeventsDelegate.insert(mTask));
		    	} else {
		    		TaskEvent mTask = new TaskEvent(AddSingleTaskActivity.this.mTask.getId(),
		    				AddSingleTaskActivity.this.mTask.getProjId(),
		    				AddSingleTaskActivity.this.mTask.getServerId(), 0, params[0], params[1], params[2], 0);
		    		dbUtils.taskeventsDelegate.update(mTask);
		    	}
				dbUtils.close();
				/*return ArtApi.getInstance(AddSingleTaskActivity.this).createTaskevent(projServerId, 0, params[0], mDateToStr.parse(params[1]), params[2]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerException e) {
				exception = e;				
				e.printStackTrace();*/
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
        protected void onPostExecute(Integer taskeventId) {
			dialog.dismiss();
			boolean hasNetwork = true;
			
			if(exception instanceof ServerException) {
				Toast.makeText(AddSingleTaskActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			// TODO:need more specific disconnection exception
			if(taskeventId != null) {
		    	if(dbUtils == null) {
		    		dbUtils = new DBUtils(AddSingleTaskActivity.this);
		    	}
		    	mTask.setServerId(taskeventId);
				dbUtils.taskeventsDelegate.update(mTask);
				dbUtils.close();
			}else {
				hasNetwork = false;
			}	
			AddSingleTaskActivity.this.finish();
		}
	}
  	  
}