package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import tw.jouou.aRoundTable.widget.NumberPicker;
import android.app.Activity;

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

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.widget.TimePicker;
import android.widget.Toast;

public class AddEventActivity extends Activity {
	
	private static final int DATE_DIALOG_ID = 0;
	private static final int TIME_DIALOG_ID = 1;
    private static final int ASSIGN_TIME_PANEL = 0;
    private static final int DEPENDENCY_PANEL = 1;
    private static final int UNDETERMINED_PANEL = 2;
    private static final int FROM_DATE_CHOOSER = 0;
    private static final int TO_DATE_CHOOSER = 1;
    private static final int FROM_TIME_CHOOSER = 2;
    private static final int TO_TIME_CHOOSER = 3;
    private static String TAG = "AddEventActivity";
	private DBUtils dbUtils;
	private TaskEvent mEvent;
	private Bundle mBundle;
	private String mProjName;
	private Project mProj;
	private Date mEventDue;
	private int mDueType = ASSIGN_TIME_PANEL;
	private boolean mPlusMinusFlag = true; //fasle:minus ; true:plus
	private LinkedList<TableRow> mDependableEvents = new LinkedList<TableRow>();
	private long mProjId;
	private LayoutInflater mInflater;
	private RelativeLayout mTimeChooser;
	private List<TaskEvent> mEvents = null;
    private EditText mEdTitle;
    private TextView mTxCreateUnder;
    private ImageButton mBtnAssignDate;
    private ImageButton mBtnDependency;
    private ImageButton mBtnUndetermined;
    private Button mBtnFromDatePicker;
    private Button mBtnToDatePicker;
    private Button mBtnFromTimePicker;
    private Button mBtnToTimePicker;
    private Button mBtnFinish;
    private Button mBtnCancel;
    private EditText mEdRemarks;
    private SimpleDateFormat mDateToStr1, mStrToDate1, mDateToStr2, mStrToDate2;
    private Calendar mCalendar = Calendar.getInstance();
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.add_event_tab);
        
        if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
        findViews();  //find basic views
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDateToStr1 = new SimpleDateFormat("yyyy/MM/dd");
        mStrToDate1 = new SimpleDateFormat("yyyy MM dd");
        mDateToStr2 = new SimpleDateFormat("HH:mm");
        mStrToDate2 = new SimpleDateFormat("HH mm");
        //get today
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH);
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);
        mBundle = this.getIntent().getExtras();
        mProj = (Project)mBundle.get("proj");
        try {
    		mEvents = dbUtils.taskeventsDelegate.get(mProj.getId());
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
        if (mBundle.getInt("type") == 0) {
            mProjName = mProj.getName();
            mProjId = mProj.getId();
            mTxCreateUnder.setText(mProjName);
            findAssignTimeView();
            updateDate(FROM_DATE_CHOOSER, mYear, mMonth, mDay);
            updateDate(TO_DATE_CHOOSER, mYear, mMonth, mDay);
        } else {
        	// remove itself from dependable mEvents when edit
        	mEvent = (TaskEvent)mBundle.get("taskevent");
        	Iterator<TaskEvent> irr = mEvents.iterator();
        	while (irr.hasNext()) {
        	    TaskEvent nextTaskEvent = irr.next();
        	    if(nextTaskEvent.getId() == mEvent.getId()) {
        	    	irr.remove();
        	    }
        	}
        	mEdTitle.setText(mEvent.getName());
        	mTxCreateUnder.setText(mBundle.getString("projname"));
        	mEdRemarks.setText(mEvent.getNote());
        	mEventDue = mEvent.getDue();
        	if(mEventDue == null) {
        		findUndeterminedView();
        	} else {
        		mCalendar.setTime(mEventDue);
        		mYear = mCalendar.get(Calendar.YEAR);
        		mMonth = mCalendar.get(Calendar.MONTH); // Month is 0 based so add 1
        		mDay = mCalendar.get(Calendar.DATE);
        		findAssignTimeView();
        	}
        }

        mBtnAssignDate.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDueType = ASSIGN_TIME_PANEL;
        		mTimeChooser.removeAllViews();
        		findAssignTimeView();
      	  	}
    	});

        mBtnDependency.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDueType = DEPENDENCY_PANEL;
        		mTimeChooser.removeAllViews();
        		findDependencyView();
      	  	}
    	});
        
        mBtnUndetermined.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		mDueType = UNDETERMINED_PANEL;
        		mTimeChooser.removeAllViews();
        		findUndeterminedView();
      	  	}
    	});
 
        mBtnFinish.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		switch(mDueType) {       		
        			case 0:
        				/*(new CreateItemEventTask()).execute(mEdTitle.getText().toString(), 
        							mBtnDatePicker.getText().toString(),
        							mEdRemarks.getText().toString());*/
        				break;
        			case 1:
        				break;
        			case 2:
        				/*(new CreateItemEventTask()).execute(mEdTitle.getText().toString(),
        							"", mEdRemarks.getText().toString());*/
        				break;
        		}
      	  	}
    	});
        
        mBtnCancel.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		AddEventActivity.this.finish();
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
        	case FROM_DATE_CHOOSER:
        		return new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
        				public void onDateSet(DatePicker view, int year, int monthOfYear,
        						int dayOfMonth) {
        							updateDate(FROM_DATE_CHOOSER, year, monthOfYear, dayOfMonth);}},
        									mYear, mMonth, mDay);
        	case TO_DATE_CHOOSER:
        		return new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
        				public void onDateSet(DatePicker view, int year, int monthOfYear,
        						int dayOfMonth) {
        							updateDate(TO_DATE_CHOOSER, year, monthOfYear, dayOfMonth);}},
        									mYear, mMonth, mDay);
        	case FROM_TIME_CHOOSER:
        		return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
        				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        							updateTime(FROM_TIME_CHOOSER, hourOfDay, minute);}},
        									mHour, mMinute,false);
        	case TO_TIME_CHOOSER:
        		return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
        				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        							updateTime(TO_TIME_CHOOSER, hourOfDay, minute);}},
        									mHour, mMinute,false);
    	}
		return null;
    }
    
	private void findAssignTimeView() {
        RelativeLayout add_event_assign_time = 
        		(RelativeLayout) mInflater.inflate(R.layout.add_item_assign_time, null)
        		.findViewById(R.id.add_event_assign_time);
        mBtnFromDatePicker = (Button) add_event_assign_time.findViewById(R.id.event_from_date_picker_context);
        mBtnFromTimePicker = (Button) add_event_assign_time.findViewById(R.id.event_from_time_picker_context);
        mBtnToDatePicker = (Button) add_event_assign_time.findViewById(R.id.event_to_date_picker_context);
        mBtnToTimePicker = (Button) add_event_assign_time.findViewById(R.id.event_to_time_picker_context);
        mBtnFromDatePicker.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		showDialog(FROM_DATE_CHOOSER);
      	  	}
    	});
        mBtnToDatePicker.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		showDialog(TO_DATE_CHOOSER);
      	  	}
    	}); 
        mBtnFromTimePicker.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		showDialog(FROM_TIME_CHOOSER);
      	  	}
    	});
        mBtnToTimePicker.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		showDialog(TO_TIME_CHOOSER);
      	  	}
    	}); 
        updateDate(FROM_DATE_CHOOSER, mYear, mMonth, mDay);
        updateDate(TO_DATE_CHOOSER, mYear, mMonth, mDay);
        updateTime(FROM_TIME_CHOOSER, mHour, mMinute);
        updateTime(TO_TIME_CHOOSER, mHour, mMinute);
        mTimeChooser.addView(add_event_assign_time);
	}

	private void findDependencyView() {
		RelativeLayout add_event_dependency;
		if (mEvents.isEmpty()) {
			add_event_dependency = new RelativeLayout(this);
			TextView noDependable = new TextView(this, null, android.R.style.TextAppearance_Medium);
			noDependable.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			noDependable.setText("無可相依工作");
			add_event_dependency.addView(noDependable);
		} else {
			final String taskNames[] = new String[mEvents.size()];
			for (int i=0; i < mEvents.size(); i++) {
				taskNames[i] = mEvents.get(i).getName();
			}
		
			add_event_dependency = (RelativeLayout) mInflater.inflate(R.layout.add_item_dependency, null)
        		.findViewById(R.id.add_single_task_dependency);
        final TableLayout single_depend_on_view = (TableLayout) add_event_dependency
				.findViewById(R.id.single_depend_on_view);
        Spinner single_dependency_plus_minus = (Spinner) add_event_dependency.findViewById(R.id.single_dependency_plus_minus);
        ArrayAdapter<String> plus_minus_adapter = new ArrayAdapter<String>(this,
        		android.R.layout.simple_spinner_item, new String[]{"加","減"});
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
        ImageButton single_depend_add_task = (ImageButton) add_event_dependency
				.findViewById(R.id.single_depend_add_task);
        single_depend_add_task.setOnClickListener( new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		final TableRow tr = new TableRow(AddEventActivity.this);
        		mDependableEvents.add(tr);
        		tr.setTag("tr");
        		tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        		final Spinner sp = new Spinner(AddEventActivity.this);
        		ArrayAdapter<String> depend_on_adapter = new ArrayAdapter<String>(AddEventActivity.this
        				,android.R.layout.simple_spinner_item, taskNames);
                depend_on_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp.setAdapter(depend_on_adapter);
                sp.setTag("sp");
        		ImageButton ib = new ImageButton(AddEventActivity.this);
        		ib.setImageResource(R.drawable.ic_delete);
        		ib.setOnClickListener( new OnClickListener() {
					@Override
					public void onClick(View v) {
						mDependableEvents.remove(tr);
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
        mTimeChooser.addView(add_event_dependency);
	}
	
	private void findUndeterminedView() {
        RelativeLayout add_single_task_undetermined = (RelativeLayout) mInflater.inflate(R.layout.add_item_undetermined, null)
        		.findViewById(R.id.add_single_task_undetermined);
        mTimeChooser.addView(add_single_task_undetermined);
	}
    
            
	private void updateDate(int type, int year, int month, int day){
		// Month is 0 based so add 1
		String fromStr = year+" "+(month+1)+" "+day;
		try {
			Date date = mStrToDate1.parse(fromStr);
			String toStr = mDateToStr1.format(date);
			switch(type) {
				case FROM_DATE_CHOOSER:
					mBtnFromDatePicker.setText(toStr);
					break;
				case TO_DATE_CHOOSER:
					mBtnToDatePicker.setText(toStr);
					break;
			}
		} catch (ParseException e) {
			Log.v(TAG, "parse error");
		}
    }
	
	private void updateTime(int type, int hour, int minute){
		String fromStr = hour+" "+minute;
		try {
			Date date = mStrToDate2.parse(fromStr);
			String toStr = mDateToStr2.format(date);
			switch(type) {
				case FROM_TIME_CHOOSER:
					mBtnFromTimePicker.setText(toStr);
					break;
				case TO_TIME_CHOOSER:
					mBtnToTimePicker.setText(toStr);
			}
		} catch (ParseException e) {
			Log.v(TAG, "parse error");
		}
    }
	
    private void findViews() {
    	mTimeChooser = (RelativeLayout)findViewById(R.id.event_time_chooser);
    	mEdTitle = (EditText)findViewById(R.id.event_title_context);
    	mTxCreateUnder = (TextView)findViewById(R.id.event_create_under_context);
    	mBtnAssignDate = (ImageButton)findViewById(R.id.event_date);
    	mBtnDependency = (ImageButton)findViewById(R.id.event_dependency);
    	mBtnUndetermined = (ImageButton)findViewById(R.id.event_undetermined);
    	mEdRemarks = (EditText)findViewById(R.id.event_remarks_context);
    	mBtnFinish = (Button)findViewById(R.id.event_additem_finish);
    	mBtnCancel = (Button)findViewById(R.id.event_additem_cancel);
    }
    
    // get depended mEvents in array of Long
    private Long[] getDependedTasks() {
		Set<Long> set = new TreeSet<Long>();
		for (int i=0; i < mDependableEvents.size(); i++) {
			Long taskEventId = mEvents.get((int)((Spinner) mDependableEvents.get(i).findViewWithTag("sp"))
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
			dialog = new ProgressDialog(AddEventActivity.this);
			dialog.setMessage(getString(R.string.processing));
			dialog.show();
		}
		
		@Override
		protected Integer doInBackground(String... params) {
			try {	
		    	if (dbUtils == null) {
		    		dbUtils = new DBUtils(AddEventActivity.this);
		    	}
		    	if (mBundle.getInt("type") == 0) {
		    		mEvent = new TaskEvent(mProjId, 0, params[0], params[1], params[2], 0);
					mEvent.setId(dbUtils.taskeventsDelegate.insert(mEvent));
		    	} else {
		    		TaskEvent mEvent = new TaskEvent(AddEventActivity.this.mEvent.getId(),
		    				AddEventActivity.this.mEvent.getProjId(),
		    				AddEventActivity.this.mEvent.getServerId(), 0, params[0], params[1], params[2], 0);
		    		dbUtils.taskeventsDelegate.update(mEvent);
		    	}
				dbUtils.close();
				/*return ArtApi.getInstance(AddEventActivity.this).createTaskevent(projServerId, 0, params[0], mDateToStr1.parse(params[1]), params[2]);
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
				Toast.makeText(AddEventActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			// TODO:need more specific disconnection exception
			if(taskeventId != null) {
		    	if(dbUtils == null) {
		    		dbUtils = new DBUtils(AddEventActivity.this);
		    	}
		    	mEvent.setServerId(taskeventId);
				dbUtils.taskeventsDelegate.update(mEvent);
				dbUtils.close();
			}else {
				hasNetwork = false;
			}	
			AddEventActivity.this.finish();
		}
	}
}