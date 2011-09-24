package tw.jouou.aRoundTable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ViewFinishedItemsActivity extends Activity {
	
	private DBUtils dbUtils;
	private Bundle mBundle;
	private Project mProj;
	private static String TAG = "ViewFinishedItemsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_finished_items);
		dbUtils = DBUtils.getInstance(this);
		mBundle = this.getIntent().getExtras();
        mProj = (Project)mBundle.get("proj");

        TextView txProjName = (TextView) findViewById(R.id.finished_item_title);
        txProjName.setText(getString(R.string.finished_task_under_project, mProj.getName()));

		List<Task> tasks = null;
    	try {
    		tasks = dbUtils.tasksDelegate.getFinished(mProj.getServerId());
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}
		ListView finishedItemListView = (ListView) findViewById(R.id.finished_item_list);

		FinishedTaskAdapter finishedTaskAdapter = new FinishedTaskAdapter(this, tasks);
    
	    finishedItemListView.setAdapter(finishedTaskAdapter);

	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (dbUtils != null) {
			dbUtils.close();
			dbUtils = null;
		}
	}
	
}
