package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.NotLoggedInException;
import tw.jouou.aRoundTable.lib.SyncService;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddEventActivity extends Activity {
	
    private static final int ASSIGN_TIME_PANEL = 0;
    private static final int UNDETERMINED_PANEL = 1;
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
	private long mProjId;
	private LayoutInflater mInflater;
	private RelativeLayout mTimeChooser;
    private EditText mEdTitle;
    private TextView mTxCreateUnder;
    private ImageButton mBtnAssignDate;
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
        	dbUtils = DBUtils.getInstance(this);
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
            mProjId = mProj.getServerId();
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
        		(RelativeLayout) mInflater.inflate(R.layout.add_item_assign_time, null);
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
	
	private void findUndeterminedView() {
        RelativeLayout add_single_task_undetermined = (RelativeLayout) mInflater.inflate(R.layout.add_item_undetermined, null);
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
		    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		    	if (mBundle.getInt("addOrEdit") == 0) {
		    		if (!params[1].equals("")) {
		    			int serverId = ArtApi.getInstance(AddEventActivity.this)
						.createEvent(mProjId, params[0], formatter.parse(params[1]), formatter.parse(params[2]), params[3], params[4] );
		    			Event event = new Event(mProjId, serverId, params[0], formatter.parse(params[1]),
		    					formatter.parse(params[2]), params[3], params[4], new Date());
		    			dbUtils.eventsDelegate.insert(event);
		    		} else {
		    			int serverId = ArtApi.getInstance(AddEventActivity.this)
						.createEvent(mProjId, params[0], null, null, params[3], params[4] );
		    			Event event = new Event(mProjId, serverId, params[0], null, null, params[3], params[4], new Date());
		    			dbUtils.eventsDelegate.insert(event);
		    		}
		    	} else {
		    		if (!params[1].equals("")) {
		    			Event event = new Event(AddEventActivity.this.mEvent.getServerId(),
		    					AddEventActivity.this.mEvent.getProjId(),
		    					AddEventActivity.this.mEvent.getServerId(), params[0],
		    					formatter.parse(params[1]), formatter.parse(params[2]), params[3], params[4], new Date());
		    			dbUtils.eventsDelegate.update(event);
		    		} else {
		    			Event event = new Event(AddEventActivity.this.mEvent.getServerId(),
		    					AddEventActivity.this.mEvent.getProjId(),
		    					AddEventActivity.this.mEvent.getServerId(),
		    					params[0], null, null, params[3], params[4], new Date());
		    			dbUtils.eventsDelegate.update(event);
		    		}
		    		Intent syncIntent = new Intent(AddEventActivity.this, SyncService.class);
		    		startService(syncIntent);
		    	}
			} catch (ServerException e) {
				exception = e;		
			} catch (ConnectionFailException e) {
				exception = e;
			} catch (ParseException e) {
				exception = e;
			} catch (NotLoggedInException e) {
				e.printStackTrace();
			}
			return 0;
		}
		
		@Override
        protected void onPostExecute(Integer i) {
			dialog.dismiss();
			if(exception instanceof ServerException) {
				Toast.makeText(AddEventActivity.this, getString(R.string.cannot_add_event_server_problem) + exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			if(exception instanceof ConnectionFailException) {
				Toast.makeText(AddEventActivity.this, getString(R.string.cannot_add_event_connection_problem), Toast.LENGTH_LONG).show();
				return;
			}
			dbUtils.close();
			AddEventActivity.this.finish();
		}
	}
}