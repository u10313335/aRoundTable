package tw.jouou.aRoundTable;

import java.util.Date;



import tw.jouou.aRoundTable.bean.GroupDoc;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.NotLoggedInException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class GroupDocActivity extends Activity {
	
	private DBUtils dbUtils;
	private Bundle mBundle;
	private GroupDoc mGroupDoc;
	private LinearLayout mainlayout;
	private ImageButton Groupdocs_finish;
	private ImageButton Groupdocs_cancel;
	private EditText Groupdocs_text;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_doc);
//		
//		if(dbUtils == null) {
//			dbUtils = DBUtils.getInstance(this);
//    	}
		
        //mBundle = this.getIntent().getExtras();
        //mGroupDoc = (GroupDoc)mBundle.get("groupdoc");
        findViews();
		//Groupdocs_text.setText(mGroupDoc.getContent());

		
		Groupdocs_text = new LineEditText(this);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		Groupdocs_text.setLayoutParams(layoutParams);
		Groupdocs_text.setBackgroundDrawable(null);
		Groupdocs_text.setBackgroundColor(Color.WHITE);
		
		mainlayout.addView(Groupdocs_text);
		
		Groupdocs_finish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
//				String content = Groupdocs_text.getText().toString();
//				(new UpdateGroupDocTask()).execute(content);
			
			}
		});
				
		Groupdocs_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GroupDocActivity.this.finish();
			}
		});
	}
	
    private void findViews() {
		Groupdocs_finish = (ImageButton)findViewById(R.id.groupdocs_finish);
		Groupdocs_cancel = (ImageButton)findViewById(R.id.groupdocs_cancel);
		mainlayout = (LinearLayout) findViewById(R.id.linear1);
		//Groupdocs_text = (EditText)findViewById(R.id.groupdocs_text);
    }
    
    
//	private class UpdateGroupDocTask extends AsyncTask<String, Void, Integer> {
//		private ProgressDialog dialog;
//		private Exception exception;
//		
//		@Override
//		protected void onPreExecute() {
//			dialog = new ProgressDialog(GroupDocActivity.this);
//			dialog.setMessage(getString(R.string.processing));
//			dialog.show();
//		}
//		
//		@Override
//		protected Integer doInBackground(String... params) {
//			try {
//				ArtApi.getInstance(GroupDocActivity.this).updateNotepad(mGroupDoc.getProjId(), params[0]);
//				GroupDoc groupDoc = new GroupDoc(mGroupDoc.getId(), mGroupDoc.getProjId(), mGroupDoc.getServerId(), 
//						params[0], new Date());
//				dbUtils.groupDocDelegate.update(groupDoc);
//			} catch (ServerException e) {
//				exception = e;
//			} catch (ConnectionFailException e) {
//				exception = e;
//			} catch (NotLoggedInException e) {
//				e.printStackTrace();
//			}
//			return 0;
//		}
//		
//		@Override
//        protected void onPostExecute(Integer serverId) {
//			dialog.dismiss();
//			if(exception instanceof ServerException) {
//				Toast.makeText(GroupDocActivity.this, getString(R.string.cannot_update_notepad_server_problem) + exception.getMessage(), Toast.LENGTH_LONG).show();
//				return;
//			}else if(exception instanceof ConnectionFailException){
//				Toast.makeText(GroupDocActivity.this, R.string.cannot_update_notepad_connection_problem, Toast.LENGTH_LONG).show();
//				return;
//			}
//			dbUtils.close();
//			GroupDocActivity.this.finish();
//		}
//	}
}