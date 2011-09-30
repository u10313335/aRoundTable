package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.os.Bundle;

public class DisplayTaskActivity extends Activity {
	
	private DBUtils dbUtils;
	private Bundle mBundle;
	private Task mTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_task);
		dbUtils = DBUtils.getInstance(this);
		mBundle = this.getIntent().getExtras();
        mTask = (Task)mBundle.get("task");
		
		
	}

	
	
	
	
}
