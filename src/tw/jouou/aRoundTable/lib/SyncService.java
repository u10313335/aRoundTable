package tw.jouou.aRoundTable.lib;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.stmt.DeleteBuilder;

import tw.jouou.aRoundTable.R;
import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.Member;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class SyncService extends Service {
	
	private ArtApi artApi;
	private static SyncService SYNC_SERVICE = null;
	public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	public static final String PREF = "SYNC_PREF";
    public static final String PREF_LAST_UPDATE = "SYNC_LAST_UPDATE";
    public static final int SERVER_FAILED = 0;
    public static final int CONNECTION_FAILED = 1;
	private static String TAG = "SyncService";
	
	@Override
	public void onCreate() {
		super.onCreate();
		SYNC_SERVICE = this;
		artApi = ArtApi.getInstance(this);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public static Service getService() {
		return SYNC_SERVICE;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		DBUtils dbUtils = new DBUtils(this);
		sync(dbUtils, this, artApi);
		return START_STICKY;
	}
	
	public static void sync(DBUtils dbUtils, Context context, ArtApi artApi) {
		try {
			Project remoteProjs[] = artApi.getProjectList();
			
			List<Project> localProjs = dbUtils.projectsDelegate.get();
			int projDiff = remoteProjs.length - localProjs.size();
			if(projDiff == 0) {
				for (int i=0 ; i<localProjs.size() ; i++) {
					Project proj = dbUtils.projectsDelegate.get(remoteProjs[i].getServerId());
					Log.v(TAG, "[project] local: " + proj.getServerId() + " remote: " + remoteProjs[i].getServerId());
					if (!(proj.getUpdateAt().compareTo(remoteProjs[i].getUpdateAt())==0)) {
						if(proj.getUpdateAt().after(remoteProjs[i].getUpdateAt())) {
							Log.v(TAG, "project: " + proj.getServerId() + " local update to server (push)");
							artApi.updateProject(proj.getServerId(), proj.getName(), Integer.toString(proj.getColor()));
							proj.setUpdateAt(artApi.getProject(proj.getServerId()).getUpdateAt());
							dbUtils.projectsDelegate.update(proj);
						} else {
							Log.v(TAG, "project: " + proj.getServerId() + " server update to local (pull)");
							dbUtils.projectsDelegate.update(artApi.getProject(proj.getServerId()));
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
				Task remoteTasks[] = artApi.getTaskList(localProjs.get(i).getServerId());
				int taskDiff = remoteTasks.length - dbUtils.tasksDelegate.count(localProjs.get(i).getServerId());
				if(taskDiff == 0) {
					for (int j=0 ; j<remoteTasks.length ; j++) {
						Task task = dbUtils.tasksDelegate.getTask(remoteTasks[j].getServerId());
						Log.v(TAG, "[task] local: " + task.getServerId() + " remote: " + remoteTasks[j].getServerId());
						if (!(task.getUpdateAt().compareTo(remoteTasks[j].getUpdateAt())==0)) {
							if(task.getUpdateAt().after(remoteTasks[j].getUpdateAt())) {
								artApi.updateTask(task.getServerId(), task.getName(), task.getDueDate(), task.getNote(), task.getDone());
								task.setUpdateAt(artApi.getTask(task.getServerId()).getUpdateAt());
								dbUtils.tasksDelegate.update(task);
							} else {
								Log.v(TAG, "task: " + task.getServerId() + " server update to local (pull)");
								dbUtils.tasksDelegate.update(artApi.getTask(task.getServerId()));
							}
						} else {
							Log.v(TAG, "task: " + task.getServerId() + " nothing changed");
							continue;
						}
					}
				} else {
					Log.v(TAG, "task numbers vary, rebuild tasks db...");
					List<Long> deletedTasks = dbUtils.tasksDelegate.getDeleted(localProjs.get(i).getServerId());
					if(!deletedTasks.isEmpty()) {
						for (int j=0 ; j < deletedTasks.size() ; j++) {
							artApi.deleteTask(deletedTasks.get(j));
							dbUtils.tasksDelegate.delete(deletedTasks.get(j));
							dbUtils.taskMembersDelegate.deleteUnderTask(deletedTasks.get(j));
						}
					}
					//rebuild tasks
					dbUtils.tasksDelegate.deleteUnderProj(localProjs.get(i).getServerId());
					dbUtils.taskMembersDelegate.deleteUnderProj(localProjs.get(i).getServerId());
					remoteTasks = artApi.getTaskList(localProjs.get(i).getServerId());
					for (int k=0 ; k < remoteTasks.length ; k++) {
						dbUtils.tasksDelegate.insert(remoteTasks[k]);
						dbUtils.taskMembersDelegate.insert(remoteTasks[k]);
					}
				}
			}
			for (int i=0 ; i<localProjs.size() ; i++) {
				Event remoteEvents[] = artApi.getEventList(localProjs.get(i).getServerId());
				int taskDiff = remoteEvents.length - dbUtils.eventsDelegate.count(localProjs.get(i).getServerId());
				if(taskDiff == 0) {
					for (int j=0 ; j<remoteEvents.length ; j++) {
						Event event = dbUtils.eventsDelegate.getEvent(remoteEvents[j].getServerId());
						Log.v(TAG, "[event] local: " + event.getServerId() + " remote: " + remoteEvents[j].getServerId());
						if (!(event.getUpdateAt().compareTo(remoteEvents[j].getUpdateAt())==0)) {
							if(event.getUpdateAt().after(remoteEvents[j].getUpdateAt())) {
								Log.v(TAG, "event: " + event.getServerId() + " local update to server (push)");
								artApi.updateEvent(event.getServerId(), event.getName(), event.getStartAt(), event.getEndAt(), event.getLocation(), event.getNote());
								event.setUpdateAt(artApi.getEvent(event.getServerId()).getUpdateAt());
								dbUtils.eventsDelegate.update(event);
							} else {
								Log.v(TAG, "event: " + event.getServerId() + " server update to local (pull)");
								dbUtils.eventsDelegate.update(artApi.getEvent(event.getServerId()));
							}
						} else {
							Log.v(TAG, "event: " + event.getServerId() + " nothing changed");
							continue;
						}
					}
				} else {
					Log.v(TAG, "event numbers vary, rebuild events db...");
					List<Long> deletedEvents = dbUtils.eventsDelegate.getDeleted(localProjs.get(i).getServerId());
					for (int j=0 ; j < deletedEvents.size() ; j++) {
						artApi.deleteEvent(deletedEvents.get(j));
						dbUtils.eventsDelegate.delete(deletedEvents.get(j));
					}
					//rebuild events
					dbUtils.eventsDelegate.deleteAll(localProjs.get(i).getServerId());
					remoteEvents = artApi.getEventList(localProjs.get(i).getServerId());
					for (int k=0 ; k < remoteEvents.length ; k++) {
						Event event = new Event(remoteEvents[k].getProjId(), remoteEvents[k].getServerId(), remoteEvents[k].getName(), remoteEvents[k].getStartAt(), remoteEvents[k].getEndAt(), remoteEvents[k].getLocation(), remoteEvents[k].getNote(), remoteEvents[k].getUpdateAt());
						dbUtils.eventsDelegate.insert(event);
					}
				}
			}
			for(Project project : localProjs) {
				try {
					// Delete all member of project
					DeleteBuilder<Member, Integer> del = dbUtils.memberDao.deleteBuilder();
					del.where().eq("project_id", project.getServerId());
					dbUtils.memberDao.delete(del.prepare());
					
					// Add new version
					for(Member member: artApi.getMembers(project.getServerId())) {
						dbUtils.memberDao.create(member);
					}	
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			prefs.edit().putString(PREF_LAST_UPDATE, formatter.format(new Date())).commit();
		} catch (ServerException e) {
			Intent intent = new Intent();
            intent.setAction("tw.jouou.aRoundTable.MainActivity");
            intent.putExtra("service_data", context.getString(R.string.remote_server_problem) + " " + e.getMessage());
            context.sendBroadcast(intent);
		} catch (ConnectionFailException e) {
			Intent intent = new Intent();
            intent.setAction("tw.jouou.aRoundTable.MainActivity");
            intent.putExtra("service_data", context.getString(R.string.internet_connection_problem));
            context.sendBroadcast(intent);
		} catch (ParseException e) {
			Log.v(TAG, "Parse Error");
		}
	}
}
