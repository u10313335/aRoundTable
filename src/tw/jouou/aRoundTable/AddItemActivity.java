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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AddItemActivity extends Activity {
	
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
    private TextView title;
    private EditText title_context;
    private TextView item_create_under;
    private TextView item_create_under_context;
    private TextView time;
    private Button one_day;
    private Button seven_day;
    private Button n_day;
    private ImageButton date;
    private Button dependent;
    private Button customize;
    private TextView additem_due;
    private TextView owner;
    private TextView owner_context;
    private EditText owneradd_context;
    private Editable owner_name;
    private String owner_name_temp = "";
    private ImageButton owner_add;
    private TextView remarks;
    private EditText remarks_context;
    private Button additem_finish;
    private Button additem_cancel;
    
    static final int DATE_DIALOG_ID = 0;
    private int mYear;
    private int mMonth;
    private int mDay;
    
    private static String TAG = "AddItemActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additem);
        findViews();
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
    	bundle = this.getIntent().getExtras();
        projName = bundle.getString("projname");
        projId = bundle.getLong("projid");
        projServerId = bundle.getLong("projserverid");
        item_create_under_context.setText(projName);
        updateDisplay();
        
        date.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		showDialog(DATE_DIALOG_ID);
      	  	}
    	});
        
        additem_finish.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		(new CreateItemEventTask()).execute(title_context.getText().toString(), 
        				additem_due.getText().toString(), remarks_context.getText().toString());
      	  	}
    	});
        
        additem_cancel.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		AddItemActivity.this.finish();
      	  	}
    	});
        
        owner_add.setOnClickListener(new OnClickListener() {
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

   	private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                		int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
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
            
	private void updateDisplay(){

		// Month is 0 based so add 1
		String fromStr = mYear+" "+(mMonth+1)+" "+mDay;
		SimpleDateFormat from = new SimpleDateFormat("yyyy MM dd");
		Date date;
		
		try {
			date = from.parse(fromStr);
			SimpleDateFormat to = new SimpleDateFormat("yyyy/MM/ddE");
			String toStr = to.format(date);
			additem_due.setText(toStr);
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
    	title = (TextView)findViewById(R.id.title);
    	title_context = (EditText)findViewById(R.id.title_context);
    	item_create_under = (TextView)findViewById(R.id.item_create_under);
    	item_create_under_context = (TextView)findViewById(R.id.item_create_under_context);
    	time = (TextView)findViewById(R.id.time);
    	one_day = (Button)findViewById(R.id.one_day);
    	seven_day = (Button)findViewById(R.id.seven_day);
    	n_day = (Button)findViewById(R.id.n_day);
    	date = (ImageButton)findViewById(R.id.date);
    	dependent = (Button)findViewById(R.id.dependent);
    	customize = (Button)findViewById(R.id.underdetermined);
    	additem_due = (TextView)findViewById(R.id.additem_due);
    	owner = (TextView)findViewById(R.id.owner);
    	owner_context = (TextView)findViewById(R.id.owner_context);
    	owneradd_context = (EditText)findViewById(R.id.owneradd_context);
    	owner_add = (ImageButton)findViewById(R.id.owner_add);
    	remarks = (TextView)findViewById(R.id.remarks);
    	remarks_context = (EditText)findViewById(R.id.remarks_context);
    	additem_finish = (Button)findViewById(R.id.additem_finish);
    	additem_cancel = (Button)findViewById(R.id.additem_cancel);
    }
    
  	  
  	private class CreateItemEventTask extends AsyncTask<String, Void, Integer> {
		private ProgressDialog dialog;
		private Exception exception;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(AddItemActivity.this);
			dialog.setMessage(getString(R.string.processing));
			dialog.show();
		}
		
		@Override
		protected Integer doInBackground(String... params) {
			try {	
		    	if (dbUtils == null) {
		    		dbUtils = new DBUtils(AddItemActivity.this);
		    	}
		    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/ddE");
				taskEvent = new TaskEvent(projId, 0, params[0], params[1], params[2], 0);
				taskEvent.setId(dbUtils.taskeventsDelegate.insert(taskEvent));
				dbUtils.close();
				return ArtApi.getInstance(AddItemActivity.this).createTaskevent(projServerId, 0, params[0], sdf.parse(params[1]), params[2]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerException e) {
				exception = e;				
				e.printStackTrace();
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
				Toast.makeText(AddItemActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			// TODO:need more specific disconnection exception
			if(taskeventId != null) {
		    	if(dbUtils == null) {
		    		dbUtils = new DBUtils(AddItemActivity.this);
		    	}
		    	taskEvent.setServerId(taskeventId);
				dbUtils.taskeventsDelegate.update(taskEvent);
				dbUtils.close();
			}else {
				hasNetwork = false;
			}	
			AddItemActivity.this.finish();
		}
	}
  	  
}