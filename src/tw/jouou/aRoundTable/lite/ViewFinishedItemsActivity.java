package tw.jouou.aRoundTable.lite;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import tw.jouou.aRoundTable.lite.bean.Project;
import tw.jouou.aRoundTable.lite.bean.Task;
import tw.jouou.aRoundTable.lite.lib.SyncService;
import tw.jouou.aRoundTable.lite.util.DBUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

public class ViewFinishedItemsActivity extends Activity {
	
	private DBUtils dbUtils;
	private Bundle mBundle;
	private Project mProj;
	protected static final int MENU_SetUndone = Menu.FIRST;
	private FinishedTaskAdapter finishedTaskAdapter;
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

		ListView finishedItemListView = (ListView) findViewById(R.id.finished_item_list);
		
		List<Task> tasks = null;
		try {
			tasks = dbUtils.tasksDelegate.getFinished(mProj.getServerId());
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "IllegalArgument");
		} catch (ParseException e) {
			Log.v(TAG, "Parse error");
		}

		finishedTaskAdapter = new FinishedTaskAdapter(this, tasks);
    
	    finishedItemListView.setAdapter(finishedTaskAdapter);
	    
	    finishedItemListView.setOnCreateContextMenuListener(new ListView.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add(Menu.NONE,MENU_SetUndone,0, R.string.set_undone);
				menu.setHeaderTitle(getString(R.string.item_operations));
			}
    	});
	}
	
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
    	Task task = (Task) finishedTaskAdapter.getItem(menuInfo.position);
        switch (item.getItemId()) {
        	case MENU_SetUndone:
        		task.setDone(false);
				task.setUpdateAt(new Date());
				dbUtils.tasksDelegate.update(task);
	    		Intent syncIntent = new Intent(this, SyncService.class);
	    		startService(syncIntent);
	    		finish();
    	}
    	return true;
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
