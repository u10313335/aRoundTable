package tw.jouou.aRoundTable;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



import tw.jouou.aRoundTable.bean.Project;
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

public class AddSingleTaskActivity extends Activity {
	
	private DBUtils dbUtils;
	private TaskEvent taskEvent;
	private Bundle bundle;
	private String projName;
	private Project proj;
	private long projId;
	private long projServerId;
    private TextView single_title;
    private EditText single_title_context;
    private TextView single_item_create_under;
    private TextView single_item_create_under_context;
    private TextView single_time;
    private Button single_one_day;
    private Button single_seven_day;
    private Button single_n_day;
    private ImageButton single_date;
    private Button single_dependent;
    private Button single_customize;
    private TextView single_additem_due;
    private TextView single_owner;
    private TextView single_owner_context;
    private EditText single_owneradd_context;
    private ImageButton single_owner_add;
    private TextView single_remarks;
    private EditText single_remarks_context;
    private Button single_additem_finish;
    private Button single_additem_cancel;
    private SimpleDateFormat dateToStr, strToDate;
    
    private static final int DATE_DIALOG_ID = 0;
    private int mYear;
    private int mMonth;
    private int mDay;
    
    private static String TAG = "AddSingleTaskActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_single_task_tab);
        findViews();
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        dateToStr = new SimpleDateFormat("yyyy/MM/ddE");
        strToDate = new SimpleDateFormat("yyyy MM dd");
        bundle = this.getIntent().getExtras();
        if (bundle.getInt("type") == 0) {
            proj = (Project)bundle.get("proj");
            projName = proj.getName();
            projId = proj.getId();
            projServerId = proj.getServerId();
            single_item_create_under_context.setText(projName);
            updateDisplay(mYear, mMonth, mDay);
        } else {
        	taskEvent = (TaskEvent)bundle.get("taskevent");
        	single_title_context.setText(taskEvent.getName());
        	single_item_create_under_context.setText(bundle.getString("projname"));
        	single_additem_due.setText(dateToStr.format(taskEvent.getDue()));
        	single_remarks_context.setText(taskEvent.getNote());
        }   
       
        single_one_day.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		updateDisplay(mYear, mMonth, mDay+1);
      	  	}
    	});
        
        single_seven_day.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		updateDisplay(mYear, mMonth, mDay+7);
      	  	}
    	});
        
        single_n_day.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		
      	  	}
    	});
        
        single_dependent.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		//TODO:add dependency here
      	  	}
    	});
        
        single_customize.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
      	  	}
    	});
        
        single_date.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		showDialog(DATE_DIALOG_ID);
      	  	}
    	});
        
        single_additem_finish.setOnClickListener(new OnClickListener() {
        	@Override
      	  	public void onClick(View v) {
        		(new CreateItemEventTask()).execute(single_title_context.getText().toString(), 
        				single_additem_due.getText().toString(), single_remarks_context.getText().toString());
      	  	}
    	});
        
        single_additem_cancel.setOnClickListener(new OnClickListener() {
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
		Date date;
		
		try {
			date = strToDate.parse(fromStr);
			String toStr = dateToStr.format(date);
			single_additem_due.setText(toStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
    private void findViews() {
    	single_title = (TextView)findViewById(R.id.single_title);
    	single_title_context = (EditText)findViewById(R.id.single_title_context);
    	single_item_create_under = (TextView)findViewById(R.id.single_item_create_under);
    	single_item_create_under_context = (TextView)findViewById(R.id.single_item_create_under_context);
    	single_time = (TextView)findViewById(R.id.single_time);
    	single_one_day = (Button)findViewById(R.id.single_one_day);
    	single_seven_day = (Button)findViewById(R.id.single_seven_day);
    	single_n_day = (Button)findViewById(R.id.single_n_day);
    	single_date = (ImageButton)findViewById(R.id.single_date);
    	single_dependent = (Button)findViewById(R.id.single_dependent);
    	single_customize = (Button)findViewById(R.id.single_underdetermined);
    	single_additem_due = (TextView)findViewById(R.id.single_additem_due);
    	single_owner = (TextView)findViewById(R.id.single_owner);
    	single_owner_context = (TextView)findViewById(R.id.single_owner_context);
    	single_owneradd_context = (EditText)findViewById(R.id.single_owneradd_context);
    	single_owner_add = (ImageButton)findViewById(R.id.single_owner_add);
    	single_remarks = (TextView)findViewById(R.id.single_remarks);
    	single_remarks_context = (EditText)findViewById(R.id.single_remarks_context);
    	single_additem_finish = (Button)findViewById(R.id.single_additem_finish);
    	single_additem_cancel = (Button)findViewById(R.id.single_additem_cancel);
    }
    
  	  
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
		    	if (bundle.getInt("type") == 0) {
		    		taskEvent = new TaskEvent(projId, 0, params[0], params[1], params[2], 0);
					taskEvent.setId(dbUtils.taskeventsDelegate.insert(taskEvent));
		    	} else {
		    		TaskEvent taskEvent = new TaskEvent(AddSingleTaskActivity.this.taskEvent.getId(),
		    				AddSingleTaskActivity.this.taskEvent.getProjId(),
		    				AddSingleTaskActivity.this.taskEvent.getServerId(), 0, params[0], params[1], params[2], 0);
		    		dbUtils.taskeventsDelegate.update(taskEvent);
		    	}
				dbUtils.close();
				/*return ArtApi.getInstance(AddSingleTaskActivity.this).createTaskevent(projServerId, 0, params[0], dateToStr.parse(params[1]), params[2]);
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
		    	taskEvent.setServerId(taskeventId);
				dbUtils.taskeventsDelegate.update(taskEvent);
				dbUtils.close();
			}else {
				hasNetwork = false;
			}	
			AddSingleTaskActivity.this.finish();
		}
	}
  	  
}