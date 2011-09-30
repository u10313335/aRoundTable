package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayTaskActivity extends Activity {
	
	private DBUtils dbUtils;
	private Bundle mBundle;
	private Task mTask;
	private Project mProject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_task);
		dbUtils = DBUtils.getInstance(this);
		mBundle = this.getIntent().getExtras();
        mTask = (Task)mBundle.get("task");
        mProject = dbUtils.projectsDelegate.get(mTask.getProjId());
        
		((TextView) findViewById(R.id.view_task_title)).setText("工作: "+mTask.getName());
		((TextView) findViewById(R.id.tx_project)).setText(mProject.getName());
		
	}

	
	
	
	
}
