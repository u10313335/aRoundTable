package tw.jouou.aRoundTable.lib;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.stmt.DeleteBuilder;

import tw.jouou.aRoundTable.MainActivity;
import tw.jouou.aRoundTable.R;
import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.Member;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class SyncService extends Service {
	
	private ArtApi artApi;
	private static SyncService SYNC_SERVICE = null;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	public static final String PREF = "SYNC_PREF";
    public static final String PREF_LAST_UPDATE = "SYNC_LAST_UPDATE";
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
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(this, MainActivity.class); 
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent appIntent = PendingIntent.getActivity(this, 0, notifyIntent,0);
        Notification notification = new Notification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.icon = R.drawable.notification;
        notification.tickerText = "正在同步項目";
        notification.setLatestEventInfo(this, "正在同步項目", "", appIntent);
        notificationManager.notify(0, notification);
        notificationManager.cancelAll();
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
					Project proj = dbUtils.projectsDelegate.get(remoteProjs[i].getServerId());
					Log.v(TAG, "[project] local: " + proj.getServerId() + " remote: " + remoteProjs[i].getServerId());
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
				int taskDiff = remoteTasks.length - dbUtils.tasksDelegate.count(localProjs.get(i).getServerId());
				if(taskDiff == 0) {
					for (int j=0 ; j<remoteTasks.length ; j++) {
						Task task = dbUtils.tasksDelegate.getTask(remoteTasks[j].getServerId());
						Log.v(TAG, "[task] local: " + task.getServerId() + " remote: " + remoteTasks[j].getServerId());
						if (!(task.getUpdateAt().compareTo(remoteTasks[j].getUpdateAt())==0)) {
							if(task.getUpdateAt().after(remoteTasks[j].getUpdateAt())) {
								Log.v(TAG, "task: " + task.getServerId() + " local update to server (push)" + remoteTasks[j].getServerId());
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
					List<Long> deletedTasks = dbUtils.tasksDelegate.getDeleted(localProjs.get(i).getServerId());
					for (int j=0 ; j < deletedTasks.size() ; j++) {
						ArtApi.getInstance(SyncService.this).deleteTask(deletedTasks.get(j));
						dbUtils.tasksDelegate.delete(deletedTasks.get(j));
						dbUtils.taskMembersDelegate.deleteUnderTask(deletedTasks.get(j));
					}
					//rebuild tasks
					dbUtils.tasksDelegate.deleteAll(localProjs.get(i).getServerId());
					dbUtils.taskMembersDelegate.deleteUnderProj(localProjs.get(i).getServerId());
					remoteTasks = ArtApi.getInstance(SyncService.this).getTaskList(localProjs.get(i).getServerId());
					for (int k=0 ; k < remoteTasks.length ; k++) {
						dbUtils.tasksDelegate.insert(remoteTasks[k]);
						dbUtils.taskMembersDelegate.insert(remoteTasks[k]);
					}
				}
			}
			for (int i=0 ; i<localProjs.size() ; i++) {
				Event remoteEvents[] = ArtApi.getInstance(SyncService.this).getEventList(localProjs.get(i).getServerId());
				int taskDiff = remoteEvents.length - dbUtils.eventsDelegate.count(localProjs.get(i).getServerId());
				if(taskDiff == 0) {
					for (int j=0 ; j<remoteEvents.length ; j++) {
						Event event = dbUtils.eventsDelegate.getEvent(remoteEvents[j].getServerId());
						Log.v(TAG, "[event] local: " + event.getServerId() + " remote: " + remoteEvents[j].getServerId());
						if (!(event.getUpdateAt().compareTo(remoteEvents[j].getUpdateAt())==0)) {
							if(event.getUpdateAt().after(remoteEvents[j].getUpdateAt())) {
								Log.v(TAG, "event: " + event.getServerId() + " local update to server (push)");
								ArtApi.getInstance(SyncService.this).updateEvent(event.getServerId(), event.getName(), event.getStartAt(), event.getEndAt(), event.getLocation(), event.getNote());
								event.setUpdateAt(ArtApi.getInstance(SyncService.this).getEvent(event.getServerId()).getUpdateAt());
								dbUtils.eventsDelegate.update(event);
							} else {
								Log.v(TAG, "event: " + event.getServerId() + " server update to local (pull)");
								dbUtils.eventsDelegate.update(ArtApi.getInstance(SyncService.this).getEvent(event.getServerId()));
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
						ArtApi.getInstance(SyncService.this).deleteEvent(deletedEvents.get(j));
						dbUtils.eventsDelegate.delete(deletedEvents.get(j));
					}
					//rebuild events
					dbUtils.eventsDelegate.deleteAll(localProjs.get(i).getServerId());
					remoteEvents = ArtApi.getInstance(SyncService.this).getEventList(localProjs.get(i).getServerId());
					for (int k=0 ; k < remoteEvents.length ; k++) {
						Event event = new Event(remoteEvents[k].getProjId(), remoteEvents[k].getServerId(), remoteEvents[k].getName(), remoteEvents[k].getStartAt(), remoteEvents[k].getEndAt(), remoteEvents[k].getLocation(), remoteEvents[k].getNote(), remoteEvents[k].getUpdateAt());
						dbUtils.eventsDelegate.insert(event);
					}
				}
			}
			syncMembers(dbUtils, localProjs);
		} catch (ServerException e) {
			Log.v(TAG, e.getMessage());
		} catch (ConnectionFailException e) {
			Log.v(TAG, "Network not ok, try later");
		} catch (ParseException e) {
			Log.v(TAG, "Parse Error");
		}
	}
	
	private void syncMembers(DBUtils dbUtils, List<Project> projects) throws ServerException, ConnectionFailException {
		for(Project project : projects) {
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
	}
}
