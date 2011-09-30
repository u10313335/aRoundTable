package tw.jouou.aRoundTable.lite;

import java.util.Date;

import tw.jouou.aRoundTable.lite.bean.GroupDoc;
import tw.jouou.aRoundTable.lite.lib.ArtApi;
import tw.jouou.aRoundTable.lite.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lite.lib.ArtApi.NotLoggedInException;
import tw.jouou.aRoundTable.lite.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.lite.util.DBUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class GroupDocActivity extends Activity {
	//TODO: Simple conflict detect
	private DBUtils dbUtils;
	private Bundle mBundle;
	private GroupDoc mGroupDoc;
	private ImageButton groupdocs_finish;
	private ImageButton groupdocs_cancel;
	private EditText groupdocs_text;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_doc);
		
		dbUtils = DBUtils.getInstance(this);
		
        mBundle = this.getIntent().getExtras();
        mGroupDoc = (GroupDoc) mBundle.get("groupdoc");
        
		groupdocs_finish = (ImageButton)findViewById(R.id.groupdocs_finish);
		groupdocs_cancel = (ImageButton)findViewById(R.id.groupdocs_cancel);
		groupdocs_text = (EditText) findViewById(R.id.groupdocs_linetextview);
		groupdocs_text.setText(mGroupDoc.getContent());
		
		groupdocs_finish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = groupdocs_text.getText().toString();
				(new UpdateGroupDocTask()).execute(content);
			}
		});
				
		groupdocs_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GroupDocActivity.this.finish();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		final String content = groupdocs_text.getText().toString();
		
		if(content.equals(mGroupDoc.getContent()))
			super.onBackPressed();
		else{
			new AlertDialog.Builder(this)
				.setTitle(R.string.gorup_docs_confirm_title)
				.setMessage(R.string.gorup_docs_confirm_message)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						(new UpdateGroupDocTask()).execute(content);
					}
				})
				.setNeutralButton(android.R.string.no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).show();
		}
	}

    
	private class UpdateGroupDocTask extends AsyncTask<String, Void, Integer> {
		private ProgressDialog dialog;
		private Exception exception;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(GroupDocActivity.this);
			dialog.setMessage(getString(R.string.processing));
			dialog.show();
		}
		
		@Override
		protected Integer doInBackground(String... params) {
			try {
				ArtApi.getInstance(GroupDocActivity.this).updateNotepad(mGroupDoc.getProjId(), params[0]);
				GroupDoc groupDoc = new GroupDoc(mGroupDoc.getId(), mGroupDoc.getProjId(), mGroupDoc.getServerId(), 
						params[0], new Date());
				dbUtils.groupDocDelegate.update(groupDoc);
			} catch (ServerException e) {
				exception = e;
			} catch (ConnectionFailException e) {
				exception = e;
			} catch (NotLoggedInException e) {
				e.printStackTrace();
			}
			return 0;
		}
		
		@Override
        protected void onPostExecute(Integer serverId) {
			dialog.dismiss();
			if(exception instanceof ServerException) {
				Toast.makeText(GroupDocActivity.this, getString(R.string.cannot_update_notepad_server_problem) + exception.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}else if(exception instanceof ConnectionFailException){
				Toast.makeText(GroupDocActivity.this, R.string.cannot_update_notepad_connection_problem, Toast.LENGTH_LONG).show();
				return;
			}
			dbUtils.close();
			GroupDocActivity.this.finish();
		}
	}
}