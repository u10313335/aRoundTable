package tw.jouou.aRoundTable;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class AddEventActivity extends Activity {
	
	private DBUtils dbUtils;
	private TaskEvent taskEvent;
	private Bundle bundle;
	private String projName;
	private long projId;
	private long projServerId;
// TODO:wait to be changed to tab layout
//	private Button single;
//  private Button assignment;
//  private Button event;
    private TextView event_title;
    private EditText event_title_context;
    private TextView event_item_create_under;
    private TextView event_item_create_under_context;
    private TextView event_time;
    private Button event_one_day;
    private Button event_seven_day;
    private Button event_n_day;
    private ImageButton event_date;
    private Button event_dependent;
    private Button event_customize;
    private TextView event_additem_due;
    private TextView event_remarks;
    private EditText event_remarks_context;
    private Button event_additem_finish;
    private Button event_additem_cancel;
    
    static final int DATE_DIALOG_ID = 0;
    private int mYear;
    private int mMonth;
    private int mDay;
    
    private static String TAG = "AddItemActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event);
        findViews();
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        event_item_create_under_context.setText(projName);
        updateDisplay(mYear, mMonth, mDay);
        
        
        event_one_day.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		updateDisplay(mYear, mMonth, mDay+1);
      	  	}
    	});
        
        event_seven_day.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		updateDisplay(mYear, mMonth, mDay+7);
      	  	}
    	});
        
        event_n_day.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		
      	  	}
    	});
        
        event_dependent.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		//TODO:add dependency here
      	  	}
    	});
        
        event_customize.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
      	  	}
    	});
        
        event_date.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		showDialog(DATE_DIALOG_ID);
      	  	}
    	});
        
        event_additem_finish.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		(new CreateItemEventTask()).execute(event_title_context.getText().toString(), 
        				event_additem_due.getText().toString(), event_remarks_context.getText().toString());
      	  	}
    	});
        
        event_additem_cancel.setOnClickListener(new OnClickListener() {
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

   	private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                		int monthOfYear, int dayOfMonth) {
                    updateDisplay(year, monthOfYear, dayOfMonth);
                }
            };
            
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
            		mDateSetListener,
            		mYear, mMonth, mDay);
    	}
    	return null;
    }
            
	private void updateDisplay(int year, int month, int day){

		// Month is 0 based so add 1
		String fromStr = year+" "+(month+1)+" "+day;
		SimpleDateFormat from = new SimpleDateFormat("yyyy MM dd");
		Date date;
		
		try {
			date = from.parse(fromStr);
			SimpleDateFormat to = new SimpleDateFormat("yyyy/MM/ddE");
			String toStr = to.format(date);
			event_additem_due.setText(toStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
    private void findViews() {
    	//TODO:wait to be changed to tab layout
    	//single = (Button)findViewById(R.id.single);
    	//assignment = (Button)findViewById(R.id.assignment);
    	//event = (Button)findViewById(R.id.event);
    	event_title = (TextView)findViewById(R.id.event_title);
    	event_title_context = (EditText)findViewById(R.id.event_title_context);
    	event_item_create_under = (TextView)findViewById(R.id.event_item_create_under);
    	event_item_create_under_context = (TextView)findViewById(R.id.event_item_create_under_context);
    	event_time = (TextView)findViewById(R.id.event_time);
    	event_one_day = (Button)findViewById(R.id.event_one_day);
    	event_seven_day = (Button)findViewById(R.id.event_seven_day);
    	event_n_day = (Button)findViewById(R.id.event_n_day);
    	event_date = (ImageButton)findViewById(R.id.event_date);
    	event_dependent = (Button)findViewById(R.id.event_dependent);
    	event_customize = (Button)findViewById(R.id.event_underdetermined);
    	event_additem_due = (TextView)findViewById(R.id.event_additem_due);
    	event_remarks = (TextView)findViewById(R.id.event_remarks);
    	event_remarks_context = (EditText)findViewById(R.id.event_remarks_context);
    	event_additem_finish = (Button)findViewById(R.id.event_additem_finish);
    	event_additem_cancel = (Button)findViewById(R.id.event_additem_cancel);
    }
    
  	  
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
		    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/ddE");
				taskEvent = new TaskEvent(projId, 0, params[0], params[1], params[2], 0);
				taskEvent.setId(dbUtils.taskeventsDelegate.insert(taskEvent));
				dbUtils.close();
/*				return ArtApi.getInstance(AddItemActivity.this).createTaskevent(projServerId, 0, params[0], sdf.parse(params[1]), params[2]);
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
		    	taskEvent.setServerId(taskeventId);
				dbUtils.taskeventsDelegate.update(taskEvent);
				dbUtils.close();
			}else {
				hasNetwork = false;
			}	
			AddEventActivity.this.finish();
		}
	}
  	  
}