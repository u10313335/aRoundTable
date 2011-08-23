package tw.jouou.aRoundTable;

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
import android.content.Context;
import android.content.DialogInterface;
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

public class AddBatchTaskActivity extends Activity {

	
	private static final int DATE_DIALOG_ID = 0;
    private static final int ASSIGN_DAY_PANEL = 0;
    private static final int DEPENDENCY_PANEL = 1;
    private static final int UNDETERMINED_PANEL = 2;
    private static String TAG = "AddBatchTaskActivity";
	private DBUtils dbUtils;
	private Task mTask;
	private Bundle mBundle;
	private String mProjName;
	private Project mProj;
	private Date mTaskDue;
	private int mDueType = ASSIGN_DAY_PANEL;
	private boolean mPlusMinusFlag = true; //fasle:minus ; true:plus
	private LinkedList<TableRow> mDependableTasks = new LinkedList<TableRow>();
	private LinkedList<TableRow> mTasksTitle = new LinkedList<TableRow>();
	private long mProjId;
	private LayoutInflater mInflater;
	private RelativeLayout mDateChooser;
	private List<Task> mTasks = null;
	private TableLayout add_batch;
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
    		mTasks = dbUtils.tasksDelegate.get(mProj.getId());
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
        mProjName = mProj.getName();
        mProjId = mProj.getId();
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
        		switch(mDueType) {
        			case 0:
        				String[] titles = getTasksTitle();
        				Tasks tasks = new Tasks(titles, mBtnDatePicker.getText().toString(), mEdRemarks.getText().toString());
        				/*for(String title:titles) {
        					(new CreateTaskTask()).execute(title,
        							mBtnDatePicker.getText().toString(),
        							mEdRemarks.getText().toString());
        				}*/
        				(new CreateTaskTask()).execute(tasks);
        				break;
        			case 1:
        				break;
        			case 2:
        				/*(new CreateItemEventTask()).execute(mEdTitle.getText().toString(),
        							"", mEdRemarks.getText().toString());*/
        				break;
        		}
				//AddBatchTaskActivity.this.finish();
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
		final TableRow tr = new TableRow(AddBatchTaskActivity.this);
		mTasksTitle.add(tr);
		tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView title = new TextView(AddBatchTaskActivity.this);
		title.setTextAppearance(AddBatchTaskActivity.this, android.R.style.TextAppearance_Medium);
		title.setText("標題");
		EditText ed = new EditText(AddBatchTaskActivity.this);
		ed.setTextAppearance(AddBatchTaskActivity.this, android.R.style.TextAppearance_Medium);
		ed.setTag("ed");
		ImageButton ib = new ImageButton(AddBatchTaskActivity.this);
		ib.setImageResource(R.drawable.ic_delete);
		ib.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTasksTitle.remove(tr);
				add_batch.removeView(tr);
			}
		});
		tr.addView(title);
		tr.addView(ed);
		tr.addView(ib);
		add_batch.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}

	private void findAssignDateView() {
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
        		AlertDialog.Builder dialog = new AlertDialog.Builder(AddBatchTaskActivity.this);
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

	private void findDependencyView() {
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
			add_single_task_dependency = (RelativeLayout) mInflater.inflate(R.layout.add_item_dependency, null)
        			.findViewById(R.id.add_single_task_dependency);
			final TableLayout single_depend_on_view = (TableLayout) add_single_task_dependency
					.findViewById(R.id.single_depend_on_view);
			Spinner single_dependency_plus_minus = (Spinner) add_single_task_dependency
					.findViewById(R.id.single_dependency_plus_minus);
			ArrayAdapter<String> plus_minus_adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, new String[]{"加","減"});
			plus_minus_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			single_dependency_plus_minus.setAdapter(plus_minus_adapter);
			single_dependency_plus_minus.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}});
			ImageButton single_depend_add_task = (ImageButton) add_single_task_dependency
					.findViewById(R.id.single_depend_add_task);
			single_depend_add_task.setOnClickListener( new OnClickListener() {
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
        mDateChooser.addView(add_single_task_dependency);
	}
	
	private void findUndeterminedView() {
        RelativeLayout add_single_task_undetermined = (RelativeLayout) mInflater.inflate(R.layout.add_item_undetermined, null)
        		.findViewById(R.id.add_single_task_undetermined);
        mDateChooser.addView(add_single_task_undetermined);
	}
	
    private void findViews() {
    	add_batch = (TableLayout)findViewById(R.id.add_batch);
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
					.getSelectedItemId()).getId();
			set.add(taskEventId);
		}
		return set.toArray(new Long[set.size()]);
    }
    
    private String[] getTasksTitle() {
		Set<String> set = new TreeSet<String>();
		for (int i=0; i < mTasksTitle.size(); i++) {
			String taskTitle = ((EditText) mTasksTitle.get(i).findViewWithTag("ed")).getText().toString();
			set.add(taskTitle);
		}
		return set.toArray(new String[set.size()]);
    }
    
    private class Tasks extends Object {
    	private String [] titles;
    	private String due;
    	private String note;
    	
    	Tasks(String[] titles, String due, String note) {
    		this.titles = titles;
    		this.due = due;
    		this.note = note;
    	}
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
	    	int serverId = 0;
	    	Task task;	    	
			try {
		    	if (dbUtils == null) {
		    		dbUtils = new DBUtils(AddBatchTaskActivity.this);
		    	}
		    	if (!params[0].due.equals("")) {
		    		for(int i=0; i < mTasksTitle.size(); i++) {
		    			serverId = ArtApi.getInstance(AddBatchTaskActivity.this)
								.createTask(mProjId, params[0].titles[i], mDateToStr.parse(params[0].due), params[0].note);
		    			task = new Task(mProjId, serverId, params[0].titles[i], mDateToStr.parse(params[0].due), params[0].note, 0);
		    			dbUtils.tasksDelegate.insert(task);
		    		}
		    	} else {
		    		for(int i=0; i < mTasksTitle.size(); i++) {
		    			serverId = ArtApi.getInstance(AddBatchTaskActivity.this)
								.createTask(mProjId, params[0].titles[i], null, params[0].note);
		    			task = new Task(mProjId, serverId, params[0].titles[i], null, params[0].note, 0);
		    			dbUtils.tasksDelegate.insert(task);
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
				Toast.makeText(AddBatchTaskActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			if(exception instanceof ConnectionFailException) {
				Toast.makeText(AddBatchTaskActivity.this, "無法新增工作。（沒有網路連接）", Toast.LENGTH_LONG).show();
				return;
			}
			dbUtils.close();
			AddBatchTaskActivity.this.finish();
		}
	}
  	  
}