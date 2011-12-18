package tw.jouou.aRoundTable.lib;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import tw.jouou.aRoundTable.R;
import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.GroupDoc;
import tw.jouou.aRoundTable.bean.Notification;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.NotLoggedInException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.j256.ormlite.stmt.DeleteBuilder;

public class SyncService extends Service {
	
	private ArtApi artApi;
	private DBUtils dbUtils;
	private Thread syncThread;
	private SharedPreferences sharedPreferences;
	private static String TAG = "SyncService";
	public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	public static final int STATUS_SYNCING = 0;
	public static final int STATUS_FINISHED_OK = 1;
	public static final int STATUS_FINISHED_SERVER_FAILED = 2;
	public static final int STATUS_FINISHED_CONNECTION_FAILED = 3;
	public static final int STATUS_CANCEL_NOT_LOGGED_IN = 4;
    public static final String PREF_LAST_UPDATE = "SYNC_LAST_UPDATE";
    public static final String ACTION_SYNC_STATUS = "tw.jouou.aRoundTable.SYNC_STATUS";
    public static final String EXTRA_SYNCSTATUS_CODE = "SYNCE_STATUS_CODE";
    public static final String EXTRA_SYNC_STATUS_STRING = "SYNC_STATUS_STRING";
	
	@Override
	public void onCreate() {
		super.onCreate();
		dbUtils = DBUtils.getInstance(this);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Periodic run
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, SyncService.class), 0);
		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		long firstTime = SystemClock.elapsedRealtime();
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					firstTime, 15*60*1000, pendingIntent);
	}
	
	public void updateStatus(int statusCode, String statusString){
		sharedPreferences.edit().putString(PREF_LAST_UPDATE, statusString).commit();
		Intent intent = new Intent(ACTION_SYNC_STATUS);
		intent.putExtra(EXTRA_SYNCSTATUS_CODE, statusCode);
		intent.putExtra(EXTRA_SYNC_STATUS_STRING, statusString);
		sendBroadcast(intent);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	private SyncBinder mBinder = new SyncBinder();
	public class SyncBinder extends Binder{
		public SyncService getService() {
			return SyncService.this;
	    }
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		try {
			sync();
		} catch (NotLoggedInException e) {
			stopSelf();
			return START_NOT_STICKY;
		}
		
		return START_STICKY;
	}
	
	public void sync() throws NotLoggedInException {
		artApi = ArtApi.getInstance(this);
		
		if(syncThread != null && syncThread.isAlive())
			return;
		
		updateStatus(STATUS_SYNCING, getString(R.string.syncing));
		
		syncThread = new Thread(new Runnable() {
			Project[] remoteProjs;
			List<Project> localProjs;
			
			@Override
			public void run() {
				try {
					remoteProjs = artApi.getProjectList();
					localProjs = dbUtils.projectsDelegate.get();
					syncProjects();
					
					for(Project project: localProjs){
						syncProjectTasks(project);
						syncProjectEvents(project);
						syncProjectMembers(project);
						syncProjectGroupDocs(project);
					}
		
					syncNotifications();
					
					updateStatus(STATUS_FINISHED_OK, getString(R.string.last_update, formatter.format(new Date())));
				} catch (ServerException e) {
					updateStatus(STATUS_FINISHED_SERVER_FAILED, getString(R.string.remote_server_problem) + " " + e.getMessage());
				} catch (ConnectionFailException e) {
					updateStatus(STATUS_FINISHED_CONNECTION_FAILED, getString(R.string.internet_connection_problem));
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				stopSelf();
			}
			
			private void syncProjects() throws ServerException, ConnectionFailException, ParseException{
				//FIXME: ugly sync logic
				boolean projectListChanged = false;
				if(remoteProjs.length != localProjs.size())
					projectListChanged = true;
				else{
					for(int i=0; i < remoteProjs.length; i++){
						if(remoteProjs[i].getServerId() != localProjs.get(i).getServerId()){
							projectListChanged = true;
							break;
						}
					}
				}
				
				if(!projectListChanged) {
					for (int i=0 ; i<localProjs.size() ; i++) {
						Project proj = dbUtils.projectsDelegate.get(remoteProjs[i].getServerId());
						Log.v(TAG, "[project] local: " + proj.getServerId() + " remote: " + remoteProjs[i].getServerId());
						if (
								!(proj.getUpdateAt().compareTo(remoteProjs[i].getUpdateAt())==0)) {
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
					localProjs = dbUtils.projectsDelegate.get();
				}
			}
			
			private void syncProjectTasks(Project project) throws ParseException, ServerException, ConnectionFailException{
				long projectServerId = project.getServerId();
				Task remoteTasks[] = artApi.getTaskList(projectServerId);
				int taskDiff = remoteTasks.length - dbUtils.tasksDelegate.count(projectServerId);
				if(taskDiff == 0) {
					for (int j=0 ; j<remoteTasks.length ; j++) {
						Task task = dbUtils.tasksDelegate.findTaskByServerId(remoteTasks[j].getServerId());
						Long[] usersId = dbUtils.tasksUsersDelegate.getUsersId(remoteTasks[j].getServerId());
						Log.v(TAG, "[task] local: " + task.getServerId() + " remote: " + remoteTasks[j].getServerId());
						if (!(task.getUpdateAt().compareTo(remoteTasks[j].getUpdateAt())==0)) {
							if(task.getUpdateAt().getTime() - remoteTasks[j].getUpdateAt().getTime() >= -60000000) {
								artApi.updateTask(task.getServerId(), task.getName(), usersId, task.getDueDate(), task.getNote(), task.getDone());
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
					List<Long> deletedTasks = dbUtils.tasksDelegate.getDeleted(projectServerId);
					if(!deletedTasks.isEmpty()) {
						for (int j=0 ; j < deletedTasks.size() ; j++) {
							artApi.deleteTask(deletedTasks.get(j));
							dbUtils.tasksDelegate.delete(deletedTasks.get(j));
							dbUtils.tasksUsersDelegate.deleteUnderTask(deletedTasks.get(j));
						}
					}
					//rebuild tasks
					dbUtils.tasksDelegate.deleteUnderProj(projectServerId);
					dbUtils.tasksUsersDelegate.deleteUnderProj(projectServerId);
					remoteTasks = artApi.getTaskList(projectServerId);
					for (int k=0 ; k < remoteTasks.length ; k++) {
						dbUtils.tasksDelegate.insert(remoteTasks[k]);
						dbUtils.tasksUsersDelegate.insertSingleTask(remoteTasks[k]);
					}
				}
			}
			
			private void syncProjectEvents(Project project) throws ServerException, ConnectionFailException, ParseException{
				long projectServerId = project.getServerId();
				Event remoteEvents[] = artApi.getEventList(projectServerId);
				int taskDiff = remoteEvents.length - dbUtils.eventsDelegate.count(projectServerId);
				if(taskDiff == 0) {
					for (int j=0 ; j<remoteEvents.length ; j++) {
						Event event = dbUtils.eventsDelegate.getEvent(remoteEvents[j].getServerId());
						Log.v(TAG, "[event] local: " + event.getServerId() + " remote: " + remoteEvents[j].getServerId());
						if (!(event.getUpdateAt().compareTo(remoteEvents[j].getUpdateAt())==0)) {
							if(event.getUpdateAt().getTime() - remoteEvents[j].getUpdateAt().getTime() >= -60000000) {
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
					Log.v(TAG, "Proj: " + project.getName() + " : event numbers vary, rebuild events db...");
					List<Long> deletedEvents = dbUtils.eventsDelegate.getDeleted(projectServerId);
					for (int j=0 ; j < deletedEvents.size() ; j++) {
						artApi.deleteEvent(deletedEvents.get(j));
						dbUtils.eventsDelegate.delete(deletedEvents.get(j));
					}
					//rebuild events
					dbUtils.eventsDelegate.deleteUnderProj(projectServerId);
					remoteEvents = artApi.getEventList(projectServerId);
					for (int k=0 ; k < remoteEvents.length ; k++) {
						Event event = new Event(remoteEvents[k].getProjId(), remoteEvents[k].getServerId(), remoteEvents[k].getName(), remoteEvents[k].getStartAt(), remoteEvents[k].getEndAt(), remoteEvents[k].getLocation(), remoteEvents[k].getNote(), remoteEvents[k].getUpdateAt());
						dbUtils.eventsDelegate.insert(event);
					}
				}
			}
			
			private void syncProjectMembers(Project project) throws SQLException, ServerException, ConnectionFailException{
				// Delete all member of project
				DeleteBuilder<User, Integer> del = dbUtils.userDao.deleteBuilder();
				del.where().eq("project_id", project.getServerId());
				dbUtils.userDao.delete(del.prepare());
				
				// Add new version
				for(User user: artApi.getUsers((int)project.getServerId())) {
					dbUtils.userDao.create(user);
				}
			}
			
			private void syncProjectGroupDocs(Project project) throws ServerException, ConnectionFailException{
				// Delete all notpad of project
				dbUtils.groupDocDelegate.delete(project.getServerId());

				// Add new version
				GroupDoc groupDoc = artApi.getNotepad(project.getServerId());
				dbUtils.groupDocDelegate.insert(groupDoc);
			}
			
			private void syncNotifications() throws ServerException, ConnectionFailException, SQLException {
				// Assume notifications can't be deleted
				for(Notification notification : artApi.getNotifications()) {
					if(dbUtils.notificationDao.queryForId(notification.id) == null)
						dbUtils.notificationDao.create(notification);
				}
			}
			
		}, "SyncThread");
		syncThread.start();
	}
}
