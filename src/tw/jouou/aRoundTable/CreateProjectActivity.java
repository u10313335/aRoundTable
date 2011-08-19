package tw.jouou.aRoundTable;


import java.util.LinkedList;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class CreateProjectActivity extends Activity {

	private EditText edTxtProjname;
	private int color = 0;
	private Button btnNext;
	private DBUtils dbUtils;
	private Project proj;
	private RadioButtonManager radioButtonManagers = new RadioButtonManager();
	private static String TAG = "CreateProjectActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_project);
    	 
        edTxtProjname = (EditText)findViewById(R.id.projname_input);
        
		radioButtonManagers.add((RadioButton) findViewById( R.id.color0 ));
		radioButtonManagers.add((RadioButton) findViewById( R.id.color1 ));
		radioButtonManagers.add((RadioButton) findViewById( R.id.color2 ));
		radioButtonManagers.add((RadioButton) findViewById( R.id.color3 ));
		radioButtonManagers.add((RadioButton) findViewById( R.id.color4 ));
		radioButtonManagers.add((RadioButton) findViewById( R.id.color5 ));
		radioButtonManagers.add((RadioButton) findViewById( R.id.color6 ));
		radioButtonManagers.add((RadioButton) findViewById( R.id.color7 ));
		
        btnNext = (Button)findViewById(R.id.next);
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	String projName = edTxtProjname.getText().toString();
				if ("".equals(projName)) {
					CreateProjectActivity.this.finish();
				} else {
	            	(new CreateProjectTask()).execute(projName,Integer.toString(color));
				}
            }
        });  
    }
	
	private class RadioButtonManager implements OnCheckedChangeListener{
		private LinkedList<RadioButton> radioButtons = new LinkedList<RadioButton>();
		public void add(RadioButton view) {
			radioButtons.add(view);
			view.setOnCheckedChangeListener(this);
		}
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(!isChecked) {
				return;
			} else {
				if(buttonView == ((CompoundButton) findViewById( R.id.color0 ))) {
					color = 0;
				} else if(buttonView == ((CompoundButton) findViewById( R.id.color1 ))) {
					color = 1;
				} else if(buttonView == ((CompoundButton) findViewById( R.id.color2 ))) {
					color = 2;
				} else if(buttonView == ((CompoundButton) findViewById( R.id.color3 ))) {
					color = 3;
				} else if(buttonView == ((CompoundButton) findViewById( R.id.color4 ))) {
					color = 4;
				} else if(buttonView == ((CompoundButton) findViewById( R.id.color5 ))) {
					color = 5;
				} else if(buttonView == ((CompoundButton) findViewById( R.id.color6 ))) {
					color = 6;
				} else if(buttonView == ((CompoundButton) findViewById( R.id.color7 ))) {
					color = 7;
				}
			}
			for(RadioButton radioButton : radioButtons) {
				if(radioButton != buttonView)
					radioButton.setChecked(false);
			}
		}
	}
	
	private class CreateProjectTask extends AsyncTask<String, Void, Integer> {
		private ProgressDialog dialog;
		private Exception exception;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CreateProjectActivity.this);
			dialog.setMessage(getString(R.string.processing));
			dialog.show();
		}
		
		@Override
		protected Integer doInBackground(String... params) {
			try {	
		    	if (dbUtils == null) {
		    		dbUtils = new DBUtils(CreateProjectActivity.this);
		    	}
				proj = new Project(params[0], Integer.parseInt(params[1]));
				proj.setId(dbUtils.projectsDelegate.insert(proj));
				dbUtils.close();
				return ArtApi.getInstance(CreateProjectActivity.this).createProject(params[0], params[1]);
			} catch (ServerException e) {
				exception = e;				
				e.printStackTrace();
			} catch (ConnectionFailException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
        protected void onPostExecute(Integer projectId) {
			dialog.dismiss();
			boolean hasNetwork = true;
			
			if(exception instanceof ServerException) {
				Toast.makeText(CreateProjectActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			if((projectId != null) && (projectId != -1)) {
		    	if(dbUtils == null) {
		    		dbUtils = new DBUtils(CreateProjectActivity.this);
		    	}
				proj.setServerId(projectId);
				dbUtils.projectsDelegate.update(proj);
				dbUtils.close();
			}else {
				hasNetwork = false;
			}
			
			Intent intent = new Intent(CreateProjectActivity.this, InviteMemberActivity.class);
			intent.putExtra("projname", edTxtProjname.getText().toString());
			intent.putExtra("networkstatus", hasNetwork);
			startActivity(intent);
		}
	}
}
