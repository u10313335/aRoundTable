package tw.jouou.aRoundTable;

import java.util.Date;

import tw.jouou.aRoundTable.bean.GroupDoc;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class GroupDocActivity extends Activity {
	
	private DBUtils dbUtils;
	private Bundle mBundle;
	private GroupDoc mGroupDoc;
	private Button Groupdocs_finish;
	private Button Groupdocs_cancel;
	private EditText Groupdocs_text;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_doc);
		
		if(dbUtils == null) {
			dbUtils = DBUtils.getInstance(this);
    	}
		
        mBundle = this.getIntent().getExtras();
        mGroupDoc = (GroupDoc)mBundle.get("groupdoc");
        findViews();
		Groupdocs_text.setText(mGroupDoc.getContent());

		Groupdocs_finish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = Groupdocs_text.getText().toString();
				GroupDoc groupDoc = new GroupDoc(mGroupDoc.getId(), mGroupDoc.getProjId(), mGroupDoc.getServerId(), 
						content, new Date());
				dbUtils.groupDocDelegate.update(groupDoc);
				GroupDocActivity.this.finish();
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
		Groupdocs_finish = (Button)findViewById(R.id.groupdocs_finish);
		Groupdocs_cancel = (Button)findViewById(R.id.groupdocs_cancel);
		Groupdocs_text = (EditText)findViewById(R.id.groupdocs_text);
    }
}
