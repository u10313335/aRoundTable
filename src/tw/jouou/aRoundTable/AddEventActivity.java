package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
	
    private static final int ASSIGN_TIME_PANEL = 0;
    private static final int DEPENDENCY_PANEL = 1;
    private static final int UNDETERMINED_PANEL = 2;
    private static final int FROM_DATE_CHOOSER = 0;
    private static final int TO_DATE_CHOOSER = 1;
    private static final int FROM_TIME_CHOOSER = 2;
    private static final int TO_TIME_CHOOSER = 3;
    private static String TAG = "AddEventActivity";
	private DBUtils dbUtils;
	private Event mEvent;
	private Bundle mBundle;
	private String mProjName;
	private Project mProj;
	private Date mEventStartAt;
	private Date mEventEndAt;
	private int mDueType = ASSIGN_TIME_PANEL;
	private LinkedList<TableRow> mDependableEvents = new LinkedList<TableRow>();
	private long mProjId;
	private LayoutInflater mInflater;
	private RelativeLayout mTimeChooser;
	private List<Event> mEvents = null;
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
    private EditText mEdLocation;
    private EditText mEdRemarks;
    private SimpleDateFormat mDateToStr, mStrToDate, mTimeToStr, mStrToTime;
    private Calendar mCalendar = Calendar.getInstance();
    private int mStartAtYear;
    private int mStartAtMonth;
    private int mStartAtDay;
    private int mStartAtHour;
    private int mStartAtMinute;
    private int mEndAtYear;
    private int mEndAtMonth;
    private int mEndAtDay;
    private int mEndAtHour;
    private int mEndAtMinute;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.add_event_tab);
        
        if(dbUtils == null) {
    		dbUtils = new DBUtils(this);
    	}
        findViews();  //find basic views
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDateToStr = new SimpleDateFormat("yyyy/MM/dd");
        mStrToDate = new SimpleDateFormat("yyyy MM dd");
        mTimeToStr = new SimpleDateFormat("HH:mm");
        mStrToTime = new SimpleDateFormat("HH mm");
        //get today
        mStartAtYear = mCalendar.get(Calendar.YEAR);
        mStartAtMonth = mCalendar.get(Calendar.MONTH);
        mStartAtDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mStartAtHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mStartAtMinute = mCalendar.get(Calendar.MINUTE);
        mEndAtYear = mCalendar.get(Calendar.YEAR);
        mEndAtMonth = mCalendar.get(Calendar.MONTH);
        mEndAtDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mEndAtHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mEndAtMinute = mCalendar.get(Calendar.MINUTE);
        mBundle = this.getIntent().getExtras();
        mProj = (Project)mBundle.get("proj");      
        if (mBundle.getInt("addOrEdit") == 0) {
            mProjName = mProj.getName();
            mProjId = mProj.getId();
            mTxCreateUnder.setText(mProjName);
            findAssignTimeView();
            updateDate(FROM_DATE_CHOOSER, mStartAtYear, mStartAtMonth, mStartAtDay);
            updateDate(TO_DATE_CHOOSER, mStartAtYear, mStartAtMonth, mStartAtDay);
        } else {
        	mEvent = (Event)mBundle.get("event");
        	mEdTitle.setText(mEvent.getName());
        	mTxCreateUnder.setText(mProj.getName());
        	mEdLocation.setText(mEvent.getLocation());
        	mEdRemarks.setText(mEvent.getNote());
        	mEventStartAt = mEvent.getStartAt();
        	mEventEndAt = mEvent.getEndAt();
        	if(mEventStartAt == null) {
        		findUndeterminedView();
        	} else {
        		mCalendar.setTime(mEventStartAt);
        		mStartAtYear = mCalendar.get(Calendar.YEAR);
        		mStartAtMonth = mCalendar.get(Calendar.MONTH); // Month is 0 based so add 1
        		mStartAtDay = mCalendar.get(Calendar.DATE);
        		mStartAtHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        		mStartAtMinute = mCalendar.get(Calendar.MINUTE);
        		mCalendar.setTime(mEventEndAt);
        		mEndAtYear = mCalendar.get(Calendar.YEAR);
        		mEndAtMonth = mCalendar.get(Calendar.MONTH); // Month is 0 based so add 1
        		mEndAtDay = mCalendar.get(Calendar.DATE);
        		mEndAtHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        		mEndAtMinute = mCalendar.get(Calendar.MINUTE);
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
        		// TODO:temporary disable dependency for event
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
        			case ASSIGN_TIME_PANEL:
        				(new CreateEventTask()).execute(mEdTitle.getText().toString(),
        						mBtnFromDatePicker.getText().toString() + " " +
        						mBtnFromTimePicker.getText().toString(),
        						mBtnToDatePicker.getText().toString() + " " +
        						mBtnToTimePicker.getText().toString(),
        						mEdLocation.getText().toString(),
        						mEdRemarks.getText().toString());
        				break;
        			case DEPENDENCY_PANEL:
        				// TODO:temporary disable dependency for event
        				break;
        			case UNDETERMINED_PANEL:
        				(new CreateEventTask()).execute(mEdTitle.getText().toString(),
        						"", "", mEdLocation.getText().toString(), mEdRemarks.getText().toString());
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
        									mStartAtYear, mStartAtMonth, mStartAtDay);
        	case TO_DATE_CHOOSER:
        		return new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
        				public void onDateSet(DatePicker view, int year, int monthOfYear,
        						int dayOfMonth) {
        							updateDate(TO_DATE_CHOOSER, year, monthOfYear, dayOfMonth);}},
        									mEndAtYear, mEndAtMonth, mEndAtDay);
        	case FROM_TIME_CHOOSER:
        		return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
        				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        							updateTime(FROM_TIME_CHOOSER, hourOfDay, minute);}},
        									mStartAtHour, mStartAtMinute,false);
        	case TO_TIME_CHOOSER:
        		return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
        				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        							updateTime(TO_TIME_CHOOSER, hourOfDay, minute);}},
        									mEndAtHour, mEndAtMinute,false);
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
        updateDate(FROM_DATE_CHOOSER, mStartAtYear, mStartAtMonth, mStartAtDay);
        updateDate(TO_DATE_CHOOSER, mEndAtYear, mEndAtMonth, mEndAtDay);
        updateTime(FROM_TIME_CHOOSER, mStartAtHour, mStartAtMinute);
        updateTime(TO_TIME_CHOOSER, mEndAtHour, mEndAtMinute);
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
    
            
	private void updateDate(int addOrEdit, int year, int month, int day){
		// Month is 0 based so add 1
		String fromStr = year+" "+(month+1)+" "+day;
		try {
			Date date = mStrToDate.parse(fromStr);
			String toStr = mDateToStr.format(date);
			switch(addOrEdit) {
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
	
	private void updateTime(int addOrEdit, int hour, int minute){
		String fromStr = hour+" "+minute;
		try {
			Date date = mStrToTime.parse(fromStr);
			String toStr = mTimeToStr.format(date);
			switch(addOrEdit) {
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
    	mEdLocation = (EditText)findViewById(R.id.event_location_context);
    	mEdRemarks = (EditText)findViewById(R.id.event_remarks_context);
    	mBtnFinish = (Button)findViewById(R.id.event_additem_finish);
    	mBtnCancel = (Button)findViewById(R.id.event_additem_cancel);
    }
   

  	private class CreateEventTask extends AsyncTask<String, Void, Integer> {
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
		    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		    	if (mBundle.getInt("addOrEdit") == 0) {
		    		if (!params[1].equals("")) {
		    			int serverId = ArtApi.getInstance(AddEventActivity.this)
						.createEvent(mProjId, params[0], formatter.parse(params[1]), formatter.parse(params[2]), params[3], params[4] );
		    			Event event = new Event(mProjId, serverId, params[0], formatter.parse(params[1]),
		    					formatter.parse(params[2]), params[3], params[4]);
		    			dbUtils.eventsDelegate.insert(event);
		    		} else {
		    			int serverId = ArtApi.getInstance(AddEventActivity.this)
						.createEvent(mProjId, params[0], null, null, params[3], params[4] );
		    			Event event = new Event(mProjId, serverId, params[0], null, null, params[3], params[4]);
		    			dbUtils.eventsDelegate.insert(event);
		    		}
		    	} else {
		    		if (!params[1].equals("")) {
		    			Event event = new Event(AddEventActivity.this.mEvent.getId(),
		    					AddEventActivity.this.mEvent.getProjId(),
		    					AddEventActivity.this.mEvent.getServerId(), params[0],
		    					formatter.parse(params[1]), formatter.parse(params[2]), params[3], params[4]);
		    			dbUtils.eventsDelegate.update(event);
		    		} else {
		    			Event event = new Event(AddEventActivity.this.mEvent.getId(),
		    					AddEventActivity.this.mEvent.getProjId(),
		    					AddEventActivity.this.mEvent.getServerId(),
		    					params[0], null, null, params[3], params[4]);
		    			dbUtils.eventsDelegate.update(event);
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
				Toast.makeText(AddEventActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			if(exception instanceof ConnectionFailException) {
				Toast.makeText(AddEventActivity.this, "無法新增事件。（沒有網路連接）", Toast.LENGTH_LONG).show();
				return;
			}
			dbUtils.close();
			AddEventActivity.this.finish();
		}
	}
}