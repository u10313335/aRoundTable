package tw.jouou.aRoundTable;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.j256.ormlite.stmt.QueryBuilder;

import tw.jouou.aRoundTable.bean.Member;
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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
	private boolean mPlusMinusFlag = true; //fasle:minus ; true:plus
	private LinkedList<Member> mTaskOwners = new LinkedList<Member>();
	private LinkedList<TableRow> mDependableTasks = new LinkedList<TableRow>();
	private LinkedList<TableRow> mTasksTitle = new LinkedList<TableRow>();
	private LinkedList<TableRow> mOwnersEmail = new LinkedList<TableRow>();
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
        		String[] titles = getTasksTitle(mTasksTitle);
        		Long[] owners = getOwnersId(mOwnersEmail);
        		Tasks tasks = null;
        		switch(mDueType) {
        			case 0:
        				tasks = new Tasks(titles, owners, mBtnDatePicker.getText().toString(), mEdRemarks.getText().toString());
        				(new CreateTaskTask()).execute(tasks);
        				break;
        			case 1:
        				break;
        			case 2:
        				tasks = new Tasks(titles, owners, "", mEdRemarks.getText().toString());
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
		final TableRow titleRow = new TableRow(AddBatchTaskActivity.this);
		titleRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		final TableRow ownerRow = new TableRow(AddBatchTaskActivity.this);
		ownerRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		mTasksTitle.add(titleRow);
		mOwnersEmail.add(ownerRow);
		//widgets for titleRow
		TextView title = new TextView(AddBatchTaskActivity.this);
		title.setTextAppearance(AddBatchTaskActivity.this, android.R.style.TextAppearance_Medium);
		title.setText(R.string.item_title);
		EditText ed = new EditText(AddBatchTaskActivity.this);
		ed.setTextAppearance(AddBatchTaskActivity.this, android.R.style.TextAppearance_Medium);
		ed.setTag("title");
		ImageButton delTask = new ImageButton(AddBatchTaskActivity.this);
		delTask.setImageResource(R.drawable.ic_delete);
		delTask.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTasksTitle.remove(titleRow);
				mOwnersEmail.remove(ownerRow);
				add_batch.removeView(titleRow);
				add_batch.removeView(ownerRow);
			}
		});
		titleRow.addView(title);
		titleRow.addView(ed);
		titleRow.addView(delTask);
		
		//widgets for ownerRow
		TextView owner = new TextView(AddBatchTaskActivity.this);
		owner.setTextAppearance(AddBatchTaskActivity.this, android.R.style.TextAppearance_Medium);
		owner.setText(R.string.owner);
		final RelativeLayout memberField = new RelativeLayout(AddBatchTaskActivity.this);
		memberField.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ownerRow.addView(owner);

		//widgets for selectOwnerRow
		final AutoCompleteTextView autoOwner = new AutoCompleteTextView(AddBatchTaskActivity.this);
		autoOwner.setHint(R.string.email_to_invite);
		autoOwner.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		final TableRow.LayoutParams ownerParams = new TableRow.LayoutParams();
		ownerParams.column = 1;
		String[] membersMail = getMembersMail(mProj.getServerId());
		autoOwner.setAdapter(new ArrayAdapter<String>(AddBatchTaskActivity.this,
        		R.layout.email_autocomplete_item, membersMail));
		autoOwner.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> membersMail, View v, int position, long l) {
    			ownerRow.removeView(autoOwner);
    			final Member member = getMember(autoOwner.getText().toString());
				mTaskOwners.add(member);
				final TextView txEmail = new TextView(AddBatchTaskActivity.this);
				txEmail.setTextAppearance(AddBatchTaskActivity.this, android.R.style.TextAppearance_Medium);
				txEmail.setText(member.email);
				txEmail.setTag("email");
				ownerRow.addView(txEmail, ownerParams);
				final ImageButton delOwner = new ImageButton(AddBatchTaskActivity.this);
				delOwner.setImageResource(R.drawable.ic_delete);
				delOwner.setOnClickListener( new OnClickListener() {
					@Override
					public void onClick(View v) {
						ownerRow.removeView(txEmail);
						ownerRow.removeView(delOwner);
						mTaskOwners.remove(member);
						autoOwner.setText("");
						ownerRow.addView(autoOwner, ownerParams);
					}
				});
				ownerRow.addView(delOwner);
            }
        });

		ImageButton addOwner = new ImageButton(AddBatchTaskActivity.this);
		addOwner.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				final TableRow tr = new TableRow(AddBatchTaskActivity.this);
				final Member member = getMember(autoOwner.getText().toString());
				mTaskOwners.add(member);
				tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				TextView txName = new TextView(AddBatchTaskActivity.this);
				txName.setTextAppearance(AddBatchTaskActivity.this, android.R.style.TextAppearance_Medium);
				txName.setText(member.name);
				tr.addView(txName);
			}
		});
		ownerRow.addView(autoOwner);
		
		//add rows onto add_batch panel
		add_batch.addView(titleRow, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		add_batch.addView(ownerRow, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
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
			Spinner single_dependency_plus_minus = (Spinner) add_single_task_dependency
					.findViewById(R.id.single_dependency_plus_minus);
			ArrayAdapter<String> plus_minus_adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, new String[]{getString(R.string.plus), getString(R.string.minus)});
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
					.getSelectedItemId()).getServerId();
			set.add(taskEventId);
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
    
    private String[] getTasksTitle(LinkedList<TableRow> tasksTitle) {
		List<String> list = new LinkedList<String>();
		for (int i=0; i < tasksTitle.size(); i++) {
			String taskTitle = ((EditText) tasksTitle.get(i).findViewWithTag("title")).getText().toString();
			list.add(taskTitle);
		}
		return list.toArray(new String[list.size()]);
    }
    
    private Long[] getOwnersId(LinkedList<TableRow> ownersEmail) {
		List<Long> list = new LinkedList<Long>();
		for (int i=0; i < ownersEmail.size(); i++) {
			String ownerEmail = ((TextView) ownersEmail.get(i).findViewWithTag("email")).getText().toString();
			Member member = getMember(ownerEmail);
			list.add(member.serverId);
		}
		return list.toArray(new Long[list.size()]);
    }
    
    
    private class Tasks extends Object {
    	private String [] titles;
    	private Long [] owners;
    	private String due;
    	private String note;
    	
    	Tasks(String[] titles, Long[] owners, String due, String note) {
    		this.titles = titles;
    		this.owners = owners;
    		this.due = due;
    		this.note = note;
    	}
    }
    
    private Member getMember(String email) {
    	List<Member> members = null;
		try {
			QueryBuilder<Member, Integer> queryBuilder = dbUtils.memberDao.queryBuilder();
			queryBuilder.where().eq("email", email);
			members = dbUtils.memberDao.query(queryBuilder.prepare());
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
		    		for(int i=0; i < mTasksTitle.size(); i++) {
		    			int serverId = ArtApi.getInstance(AddBatchTaskActivity.this)
								.createTask(mProjId, params[0].titles[i], params[0].owners[i], mDateToStr.parse(params[0].due), params[0].note);
		    			Task task = new Task(mProjId, serverId, params[0].titles[i], mDateToStr.parse(params[0].due), params[0].owners[i], params[0].note, false, new Date());
		    			dbUtils.tasksDelegate.insert(task);
		    			dbUtils.tasksMembersDelegate.insertBatchTask(task);
		    		}
		    	} else {
		    		for(int i=0; i < mTasksTitle.size(); i++) {
		    			int serverId = ArtApi.getInstance(AddBatchTaskActivity.this)
								.createTask(mProjId, params[0].titles[i], params[0].owners[i], null, params[0].note);
		    			Task task = new Task(mProjId, serverId, params[0].titles[i], null, params[0].owners[i], params[0].note, false, new Date());
		    			dbUtils.tasksDelegate.insert(task);
		    			dbUtils.tasksMembersDelegate.insertBatchTask(task);
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