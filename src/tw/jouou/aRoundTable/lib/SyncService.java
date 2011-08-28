package tw.jouou.aRoundTable.lib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class SyncService extends Service {
	
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	public static final String PREF = "SYNC_PREF";
    public static final String PREF_LAST_UPDATE = "Sync_Last_Update";
	private static String TAG = "SyncService";
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		try {
			Date now = new Date();
			formatter.format(now);
			SharedPreferences settings = getSharedPreferences(PREF, 0);
	        settings.edit().putString(PREF_LAST_UPDATE, formatter.format(now)).commit();
			Project remoteProjs[] = ArtApi.getInstance(SyncService.this).getProjectList();
			DBUtils dbUtils = new DBUtils(this);
			List<Project> localProjs = dbUtils.projectsDelegate.get();
			int projDiff = remoteProjs.length - localProjs.size();
			if(projDiff == 0) {
				for (int i=0 ; i<localProjs.size() ; i++) {
					Project proj = localProjs.get(i);				
					if (!(proj.getUpdateAt().compareTo(remoteProjs[i].getUpdateAt())==0)) {
						if(proj.getUpdateAt().after(remoteProjs[i].getUpdateAt())) {
							Log.v(TAG, "project: " + proj.getServerId() + " local update to server (push)");
							ArtApi.getInstance(SyncService.this).updateProject(proj.getServerId(), proj.getName(), Integer.toString(proj.getColor()));
							proj.setUpdateAt(ArtApi.getInstance(SyncService.this).getProject(proj.getServerId()).getUpdateAt());
							dbUtils.projectsDelegate.update(proj);
						} else {
							Log.v(TAG, "project: " + proj.getServerId() + " server update to local (pull)");
							dbUtils.projectsDelegate.update(ArtApi.getInstance(SyncService.this).getProject(proj.getServerId()));
						}
					} else {
						Log.v(TAG, "project: " + proj.getServerId() + " nothing changed");
						continue;
					}
				}
			} else {
				Log.v(TAG, "project numbers vary, rebuild projects db...");
				dbUtils.projectsDelegate.deleteAll();
				for (int i=0 ; i < remoteProjs.length ; i++) {
					Project proj = new Project(remoteProjs[i].getName(), remoteProjs[i].getServerId(), remoteProjs[i].getColor(), remoteProjs[i].getUpdateAt());
					dbUtils.projectsDelegate.insert(proj);
				}
			}
			localProjs = dbUtils.projectsDelegate.get();
			for (int i=0 ; i<localProjs.size() ; i++) {
				Task remoteTasks[] = ArtApi.getInstance(SyncService.this).getTaskList(localProjs.get(i).getServerId());
				List<Task> localTasks = dbUtils.tasksDelegate.getAll(localProjs.get(i).getServerId());
				int taskDiff = remoteTasks.length - localTasks.size();
				if(taskDiff == 0) {
					for (int j=0 ; j<localTasks.size() ; j++) {
						Task task = localTasks.get(j);				
						if (!(task.getUpdateAt().compareTo(remoteTasks[j].getUpdateAt())==0)) {
							if(task.getUpdateAt().after(remoteTasks[j].getUpdateAt())) {
								Log.v(TAG, "task: " + task.getServerId() + " local update to server (push)");
								ArtApi.getInstance(SyncService.this).updateTask(task.getServerId(), task.getName(), task.getDueDate(), task.getNote(), task.getDone());
								task.setUpdateAt(ArtApi.getInstance(SyncService.this).getTask(task.getServerId()).getUpdateAt());
								dbUtils.tasksDelegate.update(task);
							} else {
								Log.v(TAG, "task: " + task.getServerId() + " server update to local (pull)");
								dbUtils.tasksDelegate.update(ArtApi.getInstance(SyncService.this).getTask(task.getServerId()));
							}
						} else {
							Log.v(TAG, "task: " + task.getServerId() + " nothing changed");
							continue;
						}
					}
				} else {
					Log.v(TAG, "task numbers vary, rebuild tasks db...");
					dbUtils.tasksDelegate.deleteAll(localProjs.get(i).getServerId());
					for (int j=0 ; j < remoteTasks.length ; j++) {
						Task task = new Task(remoteTasks[j].getProjId(), remoteTasks[j].getServerId(), remoteTasks[j].getName(), remoteTasks[j].getDueDate(), remoteTasks[j].getNote(), remoteTasks[j].getDone(), remoteTasks[j].getUpdateAt());
						dbUtils.tasksDelegate.insert(task);
					}
				}
			}
		} catch (ServerException e) {
			Log.v(TAG, e.getMessage());
		} catch (ConnectionFailException e) {
			Log.v(TAG, "Network not ok, try later");
		} catch (ParseException e) {
			Log.v(TAG, "Parse Error");
		}
	}

}
