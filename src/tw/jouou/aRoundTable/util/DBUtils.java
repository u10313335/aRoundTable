package tw.jouou.aRoundTable.util;

import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.GroupDoc;
import tw.jouou.aRoundTable.bean.Member;
import tw.jouou.aRoundTable.bean.Notification;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.bean.Project;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBUtils extends OrmLiteSqliteOpenHelper {
	public final static String DB_NAME = "aRoundTable";
	public final static int DB_VERSION = 1;
	
	public final static String TABLE_USER = "user";
	public final static String FIELD_USER_ID = "_id";
	public final static String FIELD_USER_TOKEN = "token";
	
	public final static String TABLE_PROJECT = "project";
	public final static String FIELD_PROJECT_ID = "_id";
	public final static String FIELD_PROJECT_NAME = "name";
	public final static String FIELD_PROJECT_SERVERID = "server_id";
	public final static String FIELD_PROJECT_COLOR = "color";
	public final static String FIELD_PROJECT_UPDATED_AT = "updated_at";
	
	public final static String TABLE_TASK = "task";
	public final static String FIELD_TASK_ID = "_id";
	public final static String FIELD_TASK_NAME = "name";
	public final static String FIELD_TASK_PROJECTID = "project_id";
	public final static String FIELD_TASK_SERVERID = "server_id";
	public final static String FIELD_TASK_DUEDATE = "due";
	public final static String FIELD_TASK_NOTE = "note";
	public final static String FIELD_TASK_FINISHED = "finish";
	public final static String FIELD_TASK_UPDATED_AT = "updated_at";
	public final static String FIELD_TASK_TYPE = "type";
	
	public final static String TABLE_TASKS_MEMBERS = "tasks_members";
	public final static String FIELD_TASKS_MEMBERS_TASKID = "task_id";
	public final static String FIELD_TASKS_MEMBERS_PROJECTID = "project_id";
	public final static String FIELD_TASKS_MEMBERS_MEMBERID = "member_id";
	
	public final static String TABLE_EVENT = "event";
	public final static String FIELD_EVENT_ID = "_id";
	public final static String FIELD_EVENT_NAME = "name";
	public final static String FIELD_EVENT_PROJECTID = "project_id";
	public final static String FIELD_EVENT_SERVERID = "server_id";
	public final static String FIELD_EVENT_START = "start_at";
	public final static String FIELD_EVENT_END = "end_at";
	public final static String FIELD_EVENT_LOCATION = "location";
	public final static String FIELD_EVENT_NOTE = "note";
	public final static String FIELD_EVENT_UPDATED_AT = "updated_at";
	public final static String FIELD_EVENT_TYPE = "type";
	
	public final static String TABLE_NOTIFICATION = "notification";
	public final static String FIELD_NOTIFICATION_ID = "_id";
	public final static String FIELD_NOTIFICATION_MEMBERID = "member_id";
	public final static String FIELD_NOTIFICATION_MESSAGE = "message";
	public final static String FIELD_NOTIFICATION_SERVERID = "server_id";
	public final static String FIELD_NOTIFICATION_READ = "read";
	
	public final static String TABLE_GROUPDOC = "groupdoc";
	public final static String FIELD_GROUPDOC_ID = "_id";
	public final static String FIELD_GROUPDOC_CONTENT = "content";
	public final static String FIELD_GROUPDOC_PROJECTID = "project_id";
	public final static String FIELD_GROUPDOC_SERVERID = "server_id";
	public final static String FIELD_GROUPDOC_UPDATED_AT = "updated_at";

	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// This is for ORM Lite
	private ConnectionSource connectionSource;
	public Dao<Member, Integer> memberDao;


	public DBUtils(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		connectionSource =  new AndroidConnectionSource(this);
		try {
			memberDao = DaoManager.createDao(connectionSource, Member.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource conn) {
		db.execSQL("CREATE TABLE " + TABLE_USER
				+ "( " + FIELD_USER_ID + " INTEGER PRIMARY KEY, "
				+ FIELD_USER_TOKEN + " TEXT )");

		db.execSQL( "CREATE TABLE " + TABLE_PROJECT
				+ "( " + FIELD_PROJECT_ID + " INTEGER PRIMARY KEY, "
				+ FIELD_PROJECT_NAME + " TEXT, "
				+ FIELD_PROJECT_SERVERID + " INTEGER, "
				+ FIELD_PROJECT_COLOR + " INTEGER, "
				+ FIELD_PROJECT_UPDATED_AT + " DATETIME )");
		
		db.execSQL( "CREATE TABLE " + TABLE_TASK
				+ "( " + FIELD_TASK_ID + " INTEGER PRIMARY KEY, "
				+ FIELD_TASK_NAME + " TEXT, "
				+ FIELD_TASK_PROJECTID + " INTEGER, "
				+ FIELD_TASK_SERVERID + " INTEGER, "
				+ FIELD_TASK_DUEDATE + " DATETIME, "
				+ FIELD_TASK_NOTE + " TEXT, "
				+ FIELD_TASK_FINISHED + " INTEGER, "
				+ FIELD_TASK_UPDATED_AT + " DATETIME, "
				+ FIELD_TASK_TYPE + " INTEGER )");
		
		db.execSQL( "CREATE TABLE " + TABLE_TASKS_MEMBERS
				+ "( " + FIELD_TASKS_MEMBERS_TASKID + " INTEGER, "
				+ FIELD_TASKS_MEMBERS_PROJECTID + " INTEGER, "
				+ FIELD_TASKS_MEMBERS_MEMBERID + " INTEGER )");
		
		db.execSQL( "CREATE TABLE " + TABLE_EVENT
				+ "( " + FIELD_EVENT_ID + " INTEGER PRIMARY KEY, "
				+ FIELD_EVENT_NAME + " TEXT, "
				+ FIELD_EVENT_PROJECTID + " INTEGER, "
				+ FIELD_EVENT_SERVERID + " INTEGER, "
				+ FIELD_EVENT_START + " DATETIME, "
				+ FIELD_EVENT_END + " DATETIME, "
				+ FIELD_EVENT_LOCATION + " TEXT, "
				+ FIELD_EVENT_NOTE + " TEXT, "
				+ FIELD_EVENT_UPDATED_AT + " DATETIME, "
				+ FIELD_EVENT_TYPE + " INTEGER )");
		
		db.execSQL( "CREATE TABLE " + TABLE_NOTIFICATION
				+ "( " + FIELD_NOTIFICATION_ID + " INTEGER PRIMARY KEY, "
				+ FIELD_NOTIFICATION_MESSAGE + " TEXT, "
				+ FIELD_NOTIFICATION_MEMBERID + " INTEGER, "
				+ FIELD_NOTIFICATION_SERVERID + " INTEGER, "
				+ FIELD_NOTIFICATION_READ + " INTEGER )");
		
		db.execSQL( "CREATE TABLE " + TABLE_GROUPDOC
				+ "( " + FIELD_GROUPDOC_ID + " INTEGER PRIMARY KEY, "
				+ FIELD_GROUPDOC_CONTENT + " TEXT, "
				+ FIELD_GROUPDOC_PROJECTID + " INTEGER, "
				+ FIELD_NOTIFICATION_SERVERID + " INTEGER, "
				+ FIELD_GROUPDOC_UPDATED_AT + " DATETIME )");
		
		try {
			TableUtils.createTable(conn, Member.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource cs, int oldVersion,
			int newVersion) {
		switch(oldVersion) {
		}
	}	

	public UsersDelegate userDelegate = new UsersDelegate();
	public ProjectsDelegate projectsDelegate = new ProjectsDelegate();
	public TaskDelegate tasksDelegate = new TaskDelegate();
	public EventDelegate eventsDelegate = new EventDelegate();
	public TaskEventDelegate taskEventDelegate = new TaskEventDelegate();
	public NotificationDelegate notificationDelegate = new NotificationDelegate();
	public TaskMembersDelegate taskMembersDelegate = new TaskMembersDelegate();
	public GroupDocDelegate groupDocDelegate = new GroupDocDelegate();
	
	public class UsersDelegate {
		public void delete(User user) {
			if (user.getId() < 0)
				return;

			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_USER, "_id = ?", new String[] { String
					.valueOf(user.getId()) });
			db.close();	
		}
		
		public void update(User user) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = user.getValues();
			db.update(TABLE_USER, values, "_id = ?", new String[] { String
					.valueOf(user.getId()) });
			db.close();
		}
	
		public void insert(User user) {
			SQLiteDatabase db = getWritableDatabase();
			db.insert(TABLE_USER, null, user.getValues());
			db.close();
		}
	
		public List<User> get() {
			List<User> users = new LinkedList<User>();
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_USER, null, null, null, null, null,
					FIELD_USER_ID + " ASC");
			while (c.moveToNext()) {
				User user = new User(c.getLong(c.getColumnIndexOrThrow(FIELD_USER_ID)), 
						c.getString(c.getColumnIndexOrThrow(FIELD_USER_TOKEN)));		
				users.add(user);
			}
			c.close();
			db.close();
			return users;
		}
	}
	
	public class ProjectsDelegate {
		public void delete(Project proj) {
			if (proj.getServerId() < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_PROJECT, "server_id = ?", new String[] { String
					.valueOf(proj.getServerId()) });
			db.close();
		}
		
		public void deleteAll() {
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_PROJECT, "", null);
			db.close();
		}
		
		public void update(Project proj) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = proj.getValues();
			db.update(TABLE_PROJECT, values, "server_id = ?", new String[] { String
					.valueOf(proj.getServerId()) });
			db.close();
		}
	
		public long insert(Project proj) {
			SQLiteDatabase db = getWritableDatabase();
			long id = db.insert(TABLE_PROJECT, null, proj.getValues());
			db.close();
			return id;
		}
	
		public List<Project> get() throws ParseException{
			List<Project> projs = new LinkedList<Project>();
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_PROJECT, null, null, null, null, null,
					FIELD_PROJECT_ID + " DESC");
			while (c.moveToNext()) {
				Project proj = new Project(c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECT_ID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_PROJECT_NAME)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECT_SERVERID)),
						c.getInt(c.getColumnIndexOrThrow(FIELD_PROJECT_COLOR)),
						formatter.parse(c.getString(c.getColumnIndexOrThrow(FIELD_PROJECT_UPDATED_AT))));
				projs.add(proj);
			}
			c.close();
			db.close();
			return projs;
		}
		
		public Project get(long projId) {
			Project proj = null;
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_PROJECT, null, "server_id=" + projId, null, null, null, null);
			while (c.moveToNext()) {
				try {
					proj = new Project(c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECT_ID)),
							c.getString(c.getColumnIndexOrThrow(FIELD_PROJECT_NAME)),
							c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECT_SERVERID)),
							c.getInt(c.getColumnIndexOrThrow(FIELD_PROJECT_COLOR)),
							formatter.parse(c.getString(c.getColumnIndexOrThrow(FIELD_PROJECT_UPDATED_AT))));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			c.close();
			db.close();
			return proj;
		}
	}
	
	public class TaskDelegate {
		public void delete(long id) {
			if (id < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_TASK, "server_id = ?", new String[] { String
					.valueOf(id) });
			db.close();
		}
		
		public List<Long> getDeleted(long projId) throws ParseException {
			List<Long> deletedServerIds = new LinkedList<Long>();
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASK, new String[] { "server_id" },
					"project_id=" + projId + " and type=2",
					null, null, null,
					null, null);
			while (c.moveToNext()) {
				long deletedServerId = c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_SERVERID));
				deletedServerIds.add(deletedServerId);
			}
			c.close();
			db.close();
			return deletedServerIds;
		}
		
		public void setDelete(long id) {
			if (id < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(DBUtils.FIELD_TASK_TYPE, 2);
			db.update(TABLE_TASK, values, "server_id = ?", new String[] { String
					.valueOf(id) });
			db.close();
		}
		
		public void deleteAll(long projId) {
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_TASK, "project_id = ?", null);
			db.close();
		}
		
		public void deleteUnderProj(long projId) {
			if (projId < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_TASK, "project_id = ?", new String[] { String
					.valueOf(projId) });
			db.close();
		}
		
		public void update(Task task) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = task.getValues();
			db.update(TABLE_TASK, values, "server_id = ?", new String[] { String
					.valueOf(task.getServerId()) });
			db.close();
		}
	
		public long insert(Task task) {
			SQLiteDatabase db = getWritableDatabase();
			long id = db.insert(TABLE_TASK, null, task.getValues());
			db.close();
			return id;
		}
		
		public int count(long projId) throws ParseException {
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASK, null,
					"project_id=" + projId + " and type<>2", null, null, null, null, null);
			int numbers = c.getCount();
			c.close();
			db.close();
			return numbers;
		}
	
		public List<Task> get(long projId) throws ParseException {
			List<Task> tasks = new LinkedList<Task>();
			SQLiteDatabase db = getReadableDatabase();
			//c1:select normal tasks ; c2:select undetermined tasks
			Cursor c1 = db.query(TABLE_TASK, null,
					"project_id=" + projId + " and finish=0 and due>= Datetime('now','localtime')",
					null, null, null,
					"due ASC", null);
			Cursor c2 = db.query(TABLE_TASK, null,
					"project_id=" + projId + " and finish=0 and due=''",
					null, null, null, null, null);
			while (c1.moveToNext()) {
				Task task = new Task(c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_ID)),
							c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
							c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
							c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NAME)),
							formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_DUEDATE))),
							c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
							(c1.getInt(c1.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1)? true : false,
							formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c1.close();
			while (c2.moveToNext()) {
				Task task = new Task(c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_ID)),
							c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
							c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
							c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NAME)),
							null,
							c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
							(c2.getInt(c2.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1)? true : false,
							formatter.parse(c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c2.close();
			db.close();
			return tasks;
		}
		
		public List<Task> getFinished(long projId) throws ParseException {
			List<Task> tasks = new LinkedList<Task>();
			SQLiteDatabase db = getReadableDatabase();
			//c1:select normal tasks ; c2:select undetermined tasks
			Cursor c1 = db.query(TABLE_TASK, null,
					"project_id=" + projId + " and finish=1",
					null, null, null,
					"due ASC", null);
			Cursor c2 = db.query(TABLE_TASK, null,
					"project_id=" + projId + " and finish=1 and due=''",
					null, null, null, null, null);
			while (c1.moveToNext()) {
				Task task = new Task(c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_ID)),
							c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
							c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
							c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NAME)),
							formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_DUEDATE))),
							c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
							(c1.getInt(c1.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1)? true : false,
							formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c1.close();
			while (c2.moveToNext()) {
				Task task = new Task(c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_ID)),
							c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
							c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
							c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NAME)),
							null,
							c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
							(c2.getInt(c2.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1)? true : false,
							formatter.parse(c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c2.close();
			db.close();
			return tasks;
		}
		
		public Task getTask(long id) throws ParseException {
			Task task = null;
			Date dueDate = null;
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASK, null,
					"server_id=" + id,
					null, null, null, null, null);
			while (c.moveToNext()) {
				String due = c.getString(c.getColumnIndexOrThrow(FIELD_TASK_DUEDATE));
				if(!due.equals("")) {
					dueDate = formatter.parse(due);
				}
				task = new Task(c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_ID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASK_NAME)),
						dueDate,
						c.getString(c.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
						(c.getInt(c.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1)? true : false,
						formatter.parse(c.getString(c.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
			}
			c.close();
			db.close();
			return task;
		}
		
		public List<Task> get() throws ParseException {
			List<Task> tasks = new LinkedList<Task>();
			SQLiteDatabase db = getReadableDatabase();
			Cursor c1 = db.query(TABLE_TASK, null, "finish=0 and due>= Datetime('now','localtime')", null, null, null,
					"due ASC", null);
			Cursor c2 = db.query(TABLE_TASK, null, "finish=0 and due=''",
					null, null, null, null, null);
			while (c1.moveToNext()) {
				Task task = new Task(c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_ID)),
							c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
							c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
							c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NAME)),
							formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_DUEDATE))),
							c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
							(c1.getInt(c1.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1)? true : false,
							formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c1.close();
			while (c2.moveToNext()) {
				Task task = new Task(c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_ID)),
							c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
							c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
							c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NAME)),
							null,
							c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
							(c2.getInt(c2.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1)? true : false,
							formatter.parse(c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c2.close();
			db.close();
			return tasks;
		}
	}
	
	public class TaskMembersDelegate {
		public void delete(long id) {
			if (id < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_TASKS_MEMBERS, "member_id = ?", new String[] { String
					.valueOf(id) });
			db.close();
		}
		
		public void deleteUnderTask(long id) {
			if (id < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_TASKS_MEMBERS, "task_id = ?", new String[] { String
					.valueOf(id) });
			db.close();
		}
		
		public void deleteUnderProj(long id) {
			if (id < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_TASKS_MEMBERS, "project_id = ?", new String[] { String
					.valueOf(id) });
			db.close();
		}
	
		public void deleteAll() {
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_TASKS_MEMBERS, null, null);
			db.close();
		}
	
		public void update(Task task) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = task.getMembersValues();
			db.update(TABLE_TASKS_MEMBERS, values, "task_id = ?", new String[] { String
					.valueOf(task.getServerId()) });
			db.close();
		}
	
		public void insert(Task task) {
			SQLiteDatabase db = getWritableDatabase();
			db.insert(TABLE_TASKS_MEMBERS, null, task.getMembersValues());
			db.close();
		}
		
		public String[] getMembers(long taskId) {
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASKS_MEMBERS,  null, "task_id=" + taskId, null, null, null, null);
			String[] names = null;
			if(c.getCount() > 0) {
				names = new String[c.getCount()];
				while (c.moveToNext()) {
					List<Member> members = null;
					try {
						QueryBuilder<Member, Integer> queryBuilder = memberDao.queryBuilder();
						queryBuilder.where().eq("server_id", c.getLong((c.getColumnIndexOrThrow(FIELD_TASKS_MEMBERS_MEMBERID))));
						queryBuilder.selectColumns("name");
						members = memberDao.query(queryBuilder.prepare());
						names[c.getPosition()] = members.get(0).name;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			c.close();
			db.close();
			return names;
		}
	}

	public class EventDelegate {
		public void delete(long id) {
			if (id < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_EVENT, "server_id = ?", new String[] { String
					.valueOf(id) });
			db.close();
		}
		
		public List<Long> getDeleted(long projId) throws ParseException {
			List<Long> deletedServerIds = new LinkedList<Long>();
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_EVENT, new String[] { "server_id" },
					"project_id=" + projId + " and type=2",
					null, null, null,
					null, null);
			while (c.moveToNext()) {
				long deletedServerId = c.getLong(c.getColumnIndexOrThrow(FIELD_EVENT_SERVERID));
				deletedServerIds.add(deletedServerId);
			}
			c.close();
			db.close();
			return deletedServerIds;
		}
		
		public void setDelete(long id) {
			if (id < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(DBUtils.FIELD_EVENT_TYPE, 2);
			db.update(TABLE_EVENT, values, "server_id = ?", new String[] { String
					.valueOf(id) });
			db.close();
		}
		
		public void deleteAll(long projId) {
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_EVENT, "project_id = ?", null);
			db.close();
		}
		
		public void deleteUnderProj(long projId) {
			if (projId < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_EVENT, "project_id = ?", new String[] { String
					.valueOf(projId) });
			db.close();
		}
		
		public void update(Event event) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = event.getValues();
			db.update(TABLE_EVENT, values, "server_id = ?", new String[] { String
					.valueOf(event.getServerId()) });
			db.close();
		}
	
		public long insert(Event event) {
			SQLiteDatabase db = getWritableDatabase();
			long id = db.insert(TABLE_EVENT, null, event.getValues());
			db.close();
			return id;
		}
		
		public int count(long projId) throws ParseException {
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_EVENT, null,
					"project_id=" + projId + " and type<>2", null, null, null, null, null);
			int numbers = c.getCount();
			c.close();
			db.close();
			return numbers;
		}
	
		public List<Event> get(long projId) throws ParseException {
			List<Event> events = new LinkedList<Event>();
			//c1:select normal events ; c2:select undetermined events
			SQLiteDatabase db = getReadableDatabase();
			Cursor c1 = db.query(TABLE_EVENT, null,
					"project_id=" + projId + " and start_at>= Datetime('now','localtime')",
					null, null, null,
					"start_at ASC", null);
			Cursor c2 = db.query(TABLE_EVENT, null,
					"project_id=" + projId + " and start_at=''",
					null, null, null ,null, null);			
			while (c1.moveToNext()) {
				Event event = new Event(c1.getLong(c1.getColumnIndexOrThrow(FIELD_EVENT_ID)),
						c1.getLong(c1.getColumnIndexOrThrow(FIELD_EVENT_PROJECTID)),
						c1.getLong(c1.getColumnIndexOrThrow(FIELD_EVENT_SERVERID)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_NAME)),
						formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_START))),
						formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_END))),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_LOCATION)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_NOTE)),
						formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_UPDATED_AT))));
				events.add(event);
			}
			c1.close();
			while (c2.moveToNext()) {
				Event event = new Event(c2.getLong(c2.getColumnIndexOrThrow(FIELD_EVENT_ID)),
						c2.getLong(c2.getColumnIndexOrThrow(FIELD_EVENT_PROJECTID)),
						c2.getLong(c2.getColumnIndexOrThrow(FIELD_EVENT_SERVERID)),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_NAME)),
						null,null,
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_LOCATION)),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_NOTE)),
						formatter.parse(c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_UPDATED_AT))));
				events.add(event);
			}
			c2.close();
			db.close();
			return events;
		}
		
		public Event getEvent(long id) throws ParseException {
			Event event = null;
			Date startAtDate = null;
			Date endAtDate = null;
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_EVENT, null,
					"server_id=" + id,
					null, null, null, null, null);
			while (c.moveToNext()) {
				String startAt = c.getString(c.getColumnIndexOrThrow(FIELD_EVENT_START));
				if(!startAt.equals("")) {
					startAtDate = formatter.parse(startAt);
					endAtDate = formatter.parse(c.getString(c.getColumnIndexOrThrow(FIELD_EVENT_END)));
				}
				event = new Event(c.getLong(c.getColumnIndexOrThrow(FIELD_EVENT_ID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_EVENT_PROJECTID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_EVENT_SERVERID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_EVENT_NAME)),
						startAtDate, endAtDate,
						c.getString(c.getColumnIndexOrThrow(FIELD_EVENT_LOCATION)),
						c.getString(c.getColumnIndexOrThrow(FIELD_EVENT_NOTE)),
						formatter.parse(c.getString(c.getColumnIndexOrThrow(FIELD_EVENT_UPDATED_AT))));
			}
			c.close();
			db.close();
			return event;
		}
		
		public List<Event> get() throws ParseException {
			List<Event> events = new LinkedList<Event>();
		
			SQLiteDatabase db = getReadableDatabase();
			Cursor c1 = db.query(TABLE_EVENT, null, " and start_at>= Datetime('now','localtime')", null, null, null,
					"start_at ASC", null);
			Cursor c2 = db.query(TABLE_EVENT, null, " and start_at=''", null, null, null,
					"start_at ASC", null);
			while (c1.moveToNext()) {
				Event event = new Event(c1.getLong(c1.getColumnIndexOrThrow(FIELD_EVENT_ID)),
						c1.getLong(c1.getColumnIndexOrThrow(FIELD_EVENT_PROJECTID)),
						c1.getLong(c1.getColumnIndexOrThrow(FIELD_EVENT_SERVERID)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_NAME)),
						formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_START))),
						formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_END))),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_LOCATION)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_NOTE)),
						formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_UPDATED_AT))));
				events.add(event);
			}
			c1.close();
			while (c2.moveToNext()) {
				Event event = new Event(c2.getLong(c2.getColumnIndexOrThrow(FIELD_EVENT_ID)),
						c2.getLong(c2.getColumnIndexOrThrow(FIELD_EVENT_PROJECTID)),
						c2.getLong(c2.getColumnIndexOrThrow(FIELD_EVENT_SERVERID)),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_NAME)),
						formatter.parse(c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_START))),
						formatter.parse(c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_END))),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_LOCATION)),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_NOTE)),
						formatter.parse(c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_UPDATED_AT))));
				events.add(event);
			}
			c2.close();
			db.close();
			return events;
		}
	}

	public class TaskEventDelegate {	
		public List<TaskEvent> get(long projId) throws ParseException {
			List<TaskEvent> taskevents = new LinkedList<TaskEvent>();
			
			SQLiteDatabase db = getReadableDatabase();
			Cursor c1 = db.rawQuery("select server_id,name,project_id,due,finish,type" +
					" from task where project_id=" + projId +
					" and due>= Datetime('now','localtime') and finish=0 and type<>2" +
					" union all select server_id,name,project_id,start_at,server_id,type" +
					" from event where project_id=" + projId + 
					" and start_at>= Datetime('now','localtime') and type<>2" +
					" order by due ASC", null);
			Cursor c2 = db.rawQuery("select server_id,name,project_id,due,finish,type" +
					" from task where project_id=" + projId +
					" and due='' and finish=0 and type<>2" +
					" union all select server_id,name,project_id,start_at,server_id,type" +
					" from event where project_id=" + projId +
					" and start_at='' and type<>2", null);		
			while (c1.moveToNext()) {
				TaskEvent taskevent = new TaskEvent(c1.getLong(c1.getColumnIndexOrThrow("server_id")),
						c1.getString(c1.getColumnIndexOrThrow("name")),
						c1.getLong(c1.getColumnIndexOrThrow("project_id")),
						formatter.parse(c1.getString(c1.getColumnIndexOrThrow("due"))),
						c1.getInt(c1.getColumnIndexOrThrow("finish")),
						c1.getInt(c1.getColumnIndexOrThrow("type")));
				taskevents.add(taskevent);
			}
			c1.close();
			while (c2.moveToNext()) {
				TaskEvent taskevent = new TaskEvent(c2.getLong(c2.getColumnIndexOrThrow("server_id")),
						c2.getString(c2.getColumnIndexOrThrow("name")),
						c2.getLong(c2.getColumnIndexOrThrow("project_id")),
						null,
						c2.getInt(c2.getColumnIndexOrThrow("finish")),
						c2.getInt(c2.getColumnIndexOrThrow("type")));
				taskevents.add(taskevent);
			}
			c2.close();
			db.close();
			return taskevents;
		}
		
		public List<TaskEvent> get() throws ParseException {
			List<TaskEvent> taskevents = new LinkedList<TaskEvent>();
		
			SQLiteDatabase db = getReadableDatabase();
			Cursor c1 = db.rawQuery("select server_id,name,project_id,start_at,server_id,type" +
					" from event where start_at>=Datetime('now','localtime') and type<>2" +
					" union all select task.server_id,task.name,task.project_id,task.due,task.finish,task.type" +
					" from task,tasks_members where task.server_id=tasks_members.task_id" +
					" and task.due>=Datetime('now','localtime') and task.finish=0 and task.type<>2" +
					" and tasks_members.member_id=3" +
					" order by task.due ASC", null);
			Cursor c2 = db.rawQuery("select server_id,name,project_id,start_at,server_id,type" +
					" from event where start_at='' and type<>2" +
					" union all select task.server_id,task.name,task.project_id,task.due,task.finish,task.type" +
					" from task,tasks_members where task.server_id=tasks_members.task_id" +
					" and task.due='' and task.finish=0 and task.type<>2" +
					" and tasks_members.member_id=3", null);
			while (c1.moveToNext()) {
				TaskEvent taskevent = new TaskEvent(c1.getLong(c1.getColumnIndexOrThrow("server_id")),
						c1.getString(c1.getColumnIndexOrThrow("name")),
						c1.getLong(c1.getColumnIndexOrThrow("project_id")),
						formatter.parse(c1.getString(c1.getColumnIndexOrThrow("start_at"))),
						c1.getInt(c1.getColumnIndexOrThrow("server_id")),
						c1.getInt(c1.getColumnIndexOrThrow("type")));
				taskevents.add(taskevent);
			}
			c1.close();
			while (c2.moveToNext()) {
				TaskEvent taskevent = new TaskEvent(c2.getLong(c2.getColumnIndexOrThrow("server_id")),
						c2.getString(c2.getColumnIndexOrThrow("name")),
						c2.getLong(c2.getColumnIndexOrThrow("project_id")),
						null,
						c2.getInt(c2.getColumnIndexOrThrow("finish")),
						c2.getInt(c2.getColumnIndexOrThrow("type")));
				taskevents.add(taskevent);
			}
			c2.close();
			db.close();
			return taskevents;
		}
	}
	
	public class NotificationDelegate {
		public void insert(Notification notification) {
			SQLiteDatabase db = getWritableDatabase();
			db.insert(TABLE_NOTIFICATION, null, notification.getValues());
			db.close();
		}

		public List<Notification> get() {
			List<Notification> notifications = new LinkedList<Notification>();
		
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_NOTIFICATION, null, null, null, null, null,
					FIELD_NOTIFICATION_ID + " DESC");

			while (c.moveToNext()) {
				Notification notification = new Notification(c.getLong(c.getColumnIndexOrThrow(FIELD_NOTIFICATION_ID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_NOTIFICATION_MEMBERID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_NOTIFICATION_SERVERID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_NOTIFICATION_MESSAGE)),
						(c.getInt(c.getColumnIndexOrThrow(FIELD_NOTIFICATION_READ)) == 1)? true : false);
				notifications.add(notification);
			}
			c.close();
			db.close();
			return notifications;
		}
	}
	
	public class GroupDocDelegate {
		public void insert(GroupDoc groupDoc) {
			SQLiteDatabase db = getWritableDatabase();
			db.insert(TABLE_GROUPDOC, null, groupDoc.getValues());
			db.close();
		}
		
		public void delete(long projId) {
			if (projId < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_GROUPDOC, "project_id = ?", new String[] { String
					.valueOf(projId) });
			db.close();
		}

		public GroupDoc get(long projId) throws ParseException {
			GroupDoc groupDoc = null;
		
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_GROUPDOC, null, "project_id=" + projId, null, null, null, null);

			while (c.moveToNext()) {
				groupDoc = new GroupDoc(c.getLong(c.getColumnIndexOrThrow(FIELD_GROUPDOC_ID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_GROUPDOC_PROJECTID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_GROUPDOC_SERVERID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_GROUPDOC_CONTENT)),
						formatter.parse(c.getString(c.getColumnIndexOrThrow(FIELD_GROUPDOC_UPDATED_AT))));
			}
			c.close();
			db.close();
			return groupDoc;
		}
		
		public void update(GroupDoc groupDoc) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = groupDoc.getValues();
			db.update(TABLE_GROUPDOC, values, "server_id = ?", new String[] { String
					.valueOf(groupDoc.getServerId()) });
			db.close();
		}
	}
	
}
