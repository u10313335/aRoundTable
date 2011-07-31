package tw.jouou.aRoundTable;


import java.io.IOException;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class CreateProjectActivity extends Activity {

	private EditText edTxtProjname;
	private int color = 0;
	private RadioGroup raGroupColorSel;
	private Button btnNext;
	private DBUtils dbUtils;
	private Project proj;
	private static String TAG = "CreateProjectActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_project);
    	 
        edTxtProjname = (EditText)findViewById(R.id.projname_input);
        raGroupColorSel = (RadioGroup)findViewById(R.id.color_select);
        raGroupColorSel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
					case R.id.color0:
						color = 0;
						break;
					case R.id.color1:
						color = 1;
						break;
					case R.id.color2:
						color = 2;
						break;
					case R.id.color3:
						color = 3;
						break;
					case R.id.color4:
						color = 4;
						break;
					case R.id.color5:
						color = 5;
						break;
					case R.id.color6:
						color = 6;
						break;
					case R.id.color7:
						color = 8;
						break;
				}
			}
        });
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
//			try {	
		    	if (dbUtils == null) {
		    		dbUtils = new DBUtils(CreateProjectActivity.this);
		    	}
				proj = new Project(params[0], Integer.parseInt(params[1]));
				proj.setId(dbUtils.projectsDelegate.insert(proj));
				dbUtils.close();
//				return ArtApi.getInstance(CreateProjectActivity.this).createProject(params[0]);
				return null;
/*			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerException e) {
				exception = e;				
				e.printStackTrace();
			}
			return null;*/
		}
		
		@Override
        protected void onPostExecute(Integer projectId) {
			dialog.dismiss();
			boolean hasNetwork = true;
			
			if(exception instanceof ServerException) {
				Toast.makeText(CreateProjectActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			// TODO:need more specific disconnection exception
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
