package tw.jouou.aRoundTable.util;

import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.GroupDoc;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.bean.Notification;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.bean.TaskEvent;
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

//FIXME: This class is TOO BIG, split it!!
public class DBUtils extends OrmLiteSqliteOpenHelper {
	private SQLiteDatabase db;
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
	public final static String FIELD_TASK_DEPEND_ON = "depend_on";
	public final static String FIELD_TASK_DEPEND_DURATION = "depend_duration";
	public final static String FIELD_TASK_UPDATED_AT = "updated_at";
	public final static String FIELD_TASK_TYPE = "type";

	public final static String TABLE_TASKS_USERS = "tasks_users";
	public final static String FIELD_TASKS_USERS_TASKID = "task_id";
	public final static String FIELD_TASKS_USERS_PROJECTID = "project_id";
	public final static String FIELD_TASKS_USERS_USERID = "user_id";

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

	public final static String TABLE_GROUPDOC = "groupdoc";
	public final static String FIELD_GROUPDOC_ID = "_id";
	public final static String FIELD_GROUPDOC_CONTENT = "content";
	public final static String FIELD_GROUPDOC_PROJECTID = "project_id";
	public final static String FIELD_GROUPDOC_SERVERID = "server_id";
	public final static String FIELD_GROUPDOC_UPDATED_AT = "updated_at";

	private SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// This is for ORM Lite
	private ConnectionSource connectionSource;
	public Dao<User, Integer> userDao;
	public Dao<Notification, Integer> notificationDao;

	private static DBUtils instance;

	private DBUtils(Context context) {
		super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
		connectionSource = new AndroidConnectionSource(this);
		 db = getReadableDatabase();
		try {
			userDao = DaoManager.createDao(connectionSource, User.class);
			notificationDao = DaoManager.createDao(connectionSource, Notification.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static DBUtils getInstance(Context context) {
		if (instance != null)
			return instance;
		return new DBUtils(context);
	}
	
	public static void resetInstance(Context context) {
		instance = new DBUtils(context);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource conn) {
		db.execSQL("CREATE TABLE " + TABLE_USER + "( " + FIELD_USER_ID
				+ " INTEGER PRIMARY KEY, " + FIELD_USER_TOKEN + " TEXT )");

		db.execSQL("CREATE TABLE " + TABLE_PROJECT + "( " + FIELD_PROJECT_ID
				+ " INTEGER PRIMARY KEY, " + FIELD_PROJECT_NAME + " TEXT, "
				+ FIELD_PROJECT_SERVERID + " INTEGER, " + FIELD_PROJECT_COLOR
				+ " INTEGER, " + FIELD_PROJECT_UPDATED_AT + " DATETIME )");

		db.execSQL("CREATE TABLE " + TABLE_TASK + "( " + FIELD_TASK_ID
				+ " INTEGER PRIMARY KEY, " + FIELD_TASK_NAME + " TEXT, "
				+ FIELD_TASK_PROJECTID + " INTEGER, " + FIELD_TASK_SERVERID
				+ " INTEGER, " + FIELD_TASK_DUEDATE + " DATETIME, "
				+ FIELD_TASK_NOTE + " TEXT, " + FIELD_TASK_FINISHED
				+ " INTEGER, " + FIELD_TASK_DEPEND_ON + " TEXT, "
				+ FIELD_TASK_DEPEND_DURATION + " INTEGER, "
				+ FIELD_TASK_UPDATED_AT + " DATETIME, "
				+ FIELD_TASK_TYPE + " INTEGER )");

		db.execSQL("CREATE TABLE " + TABLE_TASKS_USERS + "( "
				+ FIELD_TASKS_USERS_TASKID + " INTEGER, "
				+ FIELD_TASKS_USERS_PROJECTID + " INTEGER, "
				+ FIELD_TASKS_USERS_USERID + " INTEGER )");

		db.execSQL("CREATE TABLE " + TABLE_EVENT + "( " + FIELD_EVENT_ID
				+ " INTEGER PRIMARY KEY, " + FIELD_EVENT_NAME + " TEXT, "
				+ FIELD_EVENT_PROJECTID + " INTEGER, " + FIELD_EVENT_SERVERID
				+ " INTEGER, " + FIELD_EVENT_START + " DATETIME, "
				+ FIELD_EVENT_END + " DATETIME, " + FIELD_EVENT_LOCATION
				+ " TEXT, " + FIELD_EVENT_NOTE + " TEXT, "
				+ FIELD_EVENT_UPDATED_AT + " DATETIME, " + FIELD_EVENT_TYPE
				+ " INTEGER )");

		db.execSQL("CREATE VIEW IF NOT EXISTS taskevents AS "
				+ "SELECT task.server_id AS server_id, "
				+ "task.name AS name, " + "task.project_id AS project_id,"
				+ "task.due AS date, " + "task.finish AS finish, "
				+ "0 AS type " + "FROM task WHERE task.type <> 2 UNION SELECT "
				+ "event.server_id AS server_id, " + "event.name AS name, "
				+ "event.project_id AS project_id, "
				+ "event.start_at AS date, " + "0 AS finish, " + "1 AS type "
				+ "FROM event " + "WHERE event.type <> 2 ORDER BY date ASC");

		db.execSQL("CREATE TABLE " + TABLE_GROUPDOC + "( " + FIELD_GROUPDOC_ID
				+ " INTEGER PRIMARY KEY, " + FIELD_GROUPDOC_CONTENT + " TEXT, "
				+ FIELD_GROUPDOC_PROJECTID + " INTEGER, "
				+ FIELD_GROUPDOC_SERVERID + " INTEGER, "
				+ FIELD_GROUPDOC_UPDATED_AT + " DATETIME )");

		try {
			TableUtils.createTable(conn, User.class);
			TableUtils.createTable(conn, Notification.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource cs,
			int oldVersion, int newVersion) {
		switch (oldVersion) {
		}
	}

	public ProjectsDelegate projectsDelegate = new ProjectsDelegate();
	public TaskDelegate tasksDelegate = new TaskDelegate();
	public EventDelegate eventsDelegate = new EventDelegate();
	public TaskEventDelegate taskEventDelegate = new TaskEventDelegate();
	public TasksUsersDelegate tasksUsersDelegate = new TasksUsersDelegate();
	public GroupDocDelegate groupDocDelegate = new GroupDocDelegate();

	public class ProjectsDelegate {
		public void delete(Project proj) {
			if (proj.getServerId() < 0)
				return;
			db.delete(TABLE_PROJECT, "server_id = ?",
					new String[] { String.valueOf(proj.getServerId()) });
		}

		public void deleteAll() {
			db.delete(TABLE_PROJECT, "", null);
		}

		public void update(Project proj) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = proj.getValues();
			db.update(TABLE_PROJECT, values, "server_id = ?",
					new String[] { String.valueOf(proj.getServerId()) });
		}

		public long insert(Project proj) {
			long id = db.insert(TABLE_PROJECT, null, proj.getValues());
			return id;
		}

		public List<Project> get() throws ParseException {
			List<Project> projs = new LinkedList<Project>();
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_PROJECT, null, null, null, null, null,
					FIELD_PROJECT_ID + " DESC");
			while (c.moveToNext()) {
				Project proj = new Project(
						c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECT_ID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_PROJECT_NAME)),
						c.getLong(c
								.getColumnIndexOrThrow(FIELD_PROJECT_SERVERID)),
						c.getInt(c.getColumnIndexOrThrow(FIELD_PROJECT_COLOR)),
						formatter.parse(c.getString(c
								.getColumnIndexOrThrow(FIELD_PROJECT_UPDATED_AT))));
				projs.add(proj);
			}
			c.close();
			return projs;
		}

		public Project get(long projId) {
			Project proj = null;
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_PROJECT, null, "server_id=" + projId,
					null, null, null, null);
			while (c.moveToNext()) {
				try {
					proj = new Project(
							c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECT_ID)),
							c.getString(c
									.getColumnIndexOrThrow(FIELD_PROJECT_NAME)),
							c.getLong(c
									.getColumnIndexOrThrow(FIELD_PROJECT_SERVERID)),
							c.getInt(c
									.getColumnIndexOrThrow(FIELD_PROJECT_COLOR)),
							formatter.parse(c.getString(c
									.getColumnIndexOrThrow(FIELD_PROJECT_UPDATED_AT))));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			c.close();
			return proj;
		}
	}

	public class TaskDelegate {
		public void delete(long id) {
			if (id < 0)
				return;
			db.delete(TABLE_TASK, "server_id = ?",
					new String[] { String.valueOf(id) });
		}

		public List<Long> getDeleted(long projId) throws ParseException {
			List<Long> deletedServerIds = new LinkedList<Long>();
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASK, new String[] { "server_id" },
					"project_id=" + projId + " and type=2", null, null, null,
					null, null);
			while (c.moveToNext()) {
				long deletedServerId = c.getLong(c
						.getColumnIndexOrThrow(FIELD_TASK_SERVERID));
				deletedServerIds.add(deletedServerId);
			}
			c.close();
			return deletedServerIds;
		}

		public void setDelete(long id) {
			if (id < 0)
				return;
			ContentValues values = new ContentValues();
			values.put(DBUtils.FIELD_TASK_TYPE, 2);
			db.update(TABLE_TASK, values, "server_id = ?",
					new String[] { String.valueOf(id) });
		}

		public void deleteUnderProj(long projId) {
			if (projId < 0)
				return;
			db.delete(TABLE_TASK, "project_id = ?",
					new String[] { String.valueOf(projId) });
		}

		public void update(Task task) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = task.getValues();
			db.update(TABLE_TASK, values, "server_id = ?",
					new String[] { String.valueOf(task.getServerId()) });
		}

		public long insert(Task task) {
			long id = db.insert(TABLE_TASK, null, task.getValues());
			return id;
		}

		public int count(long projId) throws ParseException {
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASK, null, "project_id=" + projId
					+ " and type<>2", null, null, null, null, null);
			int numbers = c.getCount();
			c.close();
			return numbers;
		}

		public List<Task> get(long projId) throws ParseException {
			List<Task> tasks = new LinkedList<Task>();
			SQLiteDatabase db = getReadableDatabase();
			// c1:select normal tasks ; c2:select undetermined tasks
			Cursor c1 = db.query(TABLE_TASK, null, "project_id=" + projId
					+ " and finish=0 and due>= Datetime('now','localtime') and type<> 2",
					null, null, null, "due ASC", null);
			Cursor c2 = db.query(TABLE_TASK, null, "project_id=" + projId
					+ " and finish=0 and due='' and type<> 2", null, null, null, null, null);
			while (c1.moveToNext()) {
				Task task = new Task(
						c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_ID)),
						c1.getLong(c1
								.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
						c1.getLong(c1
								.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NAME)),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_TASK_DUEDATE))),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
						(c1.getInt(c1
								.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1) ? true
								: false, 
						c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_DEPEND_ON)),
						c1.getInt(c1.getColumnIndexOrThrow(FIELD_TASK_DEPEND_DURATION)),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c1.close();
			while (c2.moveToNext()) {
				Task task = new Task(
						c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_ID)),
						c2.getLong(c2
								.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
						c2.getLong(c2
								.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NAME)),
						null,
						c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
						(c2.getInt(c2
								.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1) ? true
								: false,
						c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_DEPEND_ON)),
						c2.getInt(c2.getColumnIndexOrThrow(FIELD_TASK_DEPEND_DURATION)),
						formatter.parse(c2.getString(c2
								.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c2.close();
			return tasks;
		}

		public List<Task> getFinished(long projId) throws ParseException {
			List<Task> tasks = new LinkedList<Task>();
			// c1:select normal tasks ; c2:select undetermined tasks
			Cursor c1 = db.query(TABLE_TASK, null, "project_id=" + projId
					+ " and finish=1 and due<>''", null, null, null, "due ASC", null);
			Cursor c2 = db.query(TABLE_TASK, null, "project_id=" + projId
					+ " and finish=1 and due=''", null, null, null, null, null);
			while (c1.moveToNext()) {
				Task task = new Task(
						c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_ID)),
						c1.getLong(c1
								.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
						c1.getLong(c1
								.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NAME)),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_TASK_DUEDATE))),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
						(c1.getInt(c1
								.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1) ? true
								: false,
						c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_DEPEND_ON)),
						c1.getInt(c1.getColumnIndexOrThrow(FIELD_TASK_DEPEND_DURATION)),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c1.close();
			while (c2.moveToNext()) {
				Task task = new Task(
						c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_ID)),
						c2.getLong(c2
								.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
						c2.getLong(c2
								.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NAME)),
						null,
						c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
						(c2.getInt(c2
								.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1) ? true
								: false,
						c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_DEPEND_ON)),
						c2.getInt(c2.getColumnIndexOrThrow(FIELD_TASK_DEPEND_DURATION)),
						formatter.parse(c2.getString(c2
								.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c2.close();
			return tasks;
		}

		public Task getTask(long id) throws ParseException {
			Task task = null;
			Date dueDate = null;
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASK, null, "server_id=" + id, null,
					null, null, null, null);
			while (c.moveToNext()) {
				String due = c.getString(c
						.getColumnIndexOrThrow(FIELD_TASK_DUEDATE));
				if (!due.equals("")) {
					dueDate = formatter.parse(due);
				}
				task = new Task(
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_ID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASK_NAME)),
						dueDate,
						c.getString(c.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
						(c.getInt(c.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1) ? true
								: false,
						c.getString(c.getColumnIndexOrThrow(FIELD_TASK_DEPEND_ON)),
						c.getInt(c.getColumnIndexOrThrow(FIELD_TASK_DEPEND_DURATION)),
						formatter.parse(c.getString(c
								.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
			}
			c.close();
			return task;
		}

		public List<Task> get() throws ParseException {
			List<Task> tasks = new LinkedList<Task>();
			SQLiteDatabase db = getReadableDatabase();
			Cursor c1 = db.query(TABLE_TASK, null,
					"finish=0 and due>= Datetime('now','localtime')", null,
					null, null, "due ASC", null);
			Cursor c2 = db.query(TABLE_TASK, null, "finish=0 and due=''", null,
					null, null, null, null);
			while (c1.moveToNext()) {
				Task task = new Task(
						c1.getLong(c1.getColumnIndexOrThrow(FIELD_TASK_ID)),
						c1.getLong(c1
								.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
						c1.getLong(c1
								.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NAME)),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_TASK_DUEDATE))),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
						(c1.getInt(c1
								.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1) ? true
								: false,
						c1.getString(c1.getColumnIndexOrThrow(FIELD_TASK_DEPEND_ON)),
						c1.getInt(c1.getColumnIndexOrThrow(FIELD_TASK_DEPEND_DURATION)),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c1.close();
			while (c2.moveToNext()) {
				Task task = new Task(
						c2.getLong(c2.getColumnIndexOrThrow(FIELD_TASK_ID)),
						c2.getLong(c2
								.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
						c2.getLong(c2
								.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NAME)),
						null,
						c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
						(c2.getInt(c2
								.getColumnIndexOrThrow(FIELD_TASK_FINISHED)) == 1) ? true
								: false,
						c2.getString(c2.getColumnIndexOrThrow(FIELD_TASK_DEPEND_ON)),
						c2.getInt(c2.getColumnIndexOrThrow(FIELD_TASK_DEPEND_DURATION)),
						formatter.parse(c2.getString(c2
								.getColumnIndexOrThrow(FIELD_TASK_UPDATED_AT))));
				tasks.add(task);
			}
			c2.close();
			return tasks;
		}
	}

	public class TasksUsersDelegate {
		public void delete(long id) {
			if (id < 0)
				return;
			db.delete(TABLE_TASKS_USERS, "user_id = ?",
					new String[] { String.valueOf(id) });
		}

		public void deleteUnderTask(long id) {
			if (id < 0)
				return;
			db.delete(TABLE_TASKS_USERS, "task_id = ?",
					new String[] { String.valueOf(id) });
		}

		public void deleteUnderProj(long id) {
			if (id < 0)
				return;
			db.delete(TABLE_TASKS_USERS, "project_id = ?",
					new String[] { String.valueOf(id) });
		}

		public void deleteAll() {
			db.delete(TABLE_TASKS_USERS, null, null);
		}

		public void insertSingleTask(Task task) {
			for (int i = 0; i < task.getOwners().length; i++) {
				db.insert(TABLE_TASKS_USERS, null, task.getOwnersValues(i));
			}
		}

		public void insertBatchTask(Task task) {
			db.insert(TABLE_TASKS_USERS, null, task.getOwnerValues());
		}

		public String[] getUsersName(long taskId) {
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASKS_USERS, null, "task_id=" + taskId,
					null, null, null, null);
			String[] names = null;
			if (c.getCount() > 0) {
				names = new String[c.getCount()];
				while (c.moveToNext()) {
					List<User> members = null;
					try {
						QueryBuilder<User, Integer> queryBuilder = userDao
								.queryBuilder();
						queryBuilder
								.where()
								.eq("server_id",
										c.getLong((c
												.getColumnIndexOrThrow(FIELD_TASKS_USERS_USERID))));
						queryBuilder.selectColumns("name");
						members = userDao.query(queryBuilder.prepare());
						if (members.size() > 0) {
							names[c.getPosition()] = members.get(0).name;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			c.close();
			return names;
		}
		
		public User[] getUsers(long taskId) {
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASKS_USERS, null, "task_id=" + taskId,
					null, null, null, null);
			User[] users = null;
			if (c.getCount() > 0) {
				users = new User[c.getCount()];
				while (c.moveToNext()) {
					List<User> members = null;
					try {
						QueryBuilder<User, Integer> queryBuilder = userDao
								.queryBuilder();
						queryBuilder
								.where()
								.eq("server_id",
										c.getLong((c
												.getColumnIndexOrThrow(FIELD_TASKS_USERS_USERID))));
						members = userDao.query(queryBuilder.prepare());
						if (members.size() > 0) {
							users[c.getPosition()] = members.get(0);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			c.close();
			return users;
		}
		
		public Long[] getUsersId(long taskId) {
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASKS_USERS, null, "task_id=" + taskId,
					null, null, null, null);
			Long[] usersId = null;
			if (c.getCount() > 0) {
				usersId = new Long[c.getCount()];
				while (c.moveToNext()) {
					List<User> members = null;
					try {
						QueryBuilder<User, Integer> queryBuilder = userDao
								.queryBuilder();
						queryBuilder
								.where()
								.eq("server_id",
										c.getLong((c
												.getColumnIndexOrThrow(FIELD_TASKS_USERS_USERID))));
						queryBuilder.selectColumns("server_id");
						members = userDao.query(queryBuilder.prepare());
						if (members.size() > 0) {
							usersId[c.getPosition()] = (long) members.get(0).serverId;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			c.close();
			return usersId;
		}
	}

	public class EventDelegate {
		public void delete(long id) {
			if (id < 0)
				return;
			db.delete(TABLE_EVENT, "server_id = ?",
					new String[] { String.valueOf(id) });
		}

		public List<Long> getDeleted(long projId) throws ParseException {
			List<Long> deletedServerIds = new LinkedList<Long>();
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_EVENT, new String[] { "server_id" },
					"project_id=" + projId + " and type=2", null, null, null,
					null, null);
			while (c.moveToNext()) {
				long deletedServerId = c.getLong(c
						.getColumnIndexOrThrow(FIELD_EVENT_SERVERID));
				deletedServerIds.add(deletedServerId);
			}
			c.close();
			return deletedServerIds;
		}

		public void setDelete(long id) {
			if (id < 0)
				return;
			ContentValues values = new ContentValues();
			values.put(DBUtils.FIELD_EVENT_TYPE, 2);
			db.update(TABLE_EVENT, values, "server_id = ?",
					new String[] { String.valueOf(id) });
		}

		public void deleteUnderProj(long projId) {
			if (projId < 0)
				return;
			db.delete(TABLE_EVENT, "project_id = ?",
					new String[] { String.valueOf(projId) });
		}

		public void update(Event event) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = event.getValues();
			db.update(TABLE_EVENT, values, "server_id = ?",
					new String[] { String.valueOf(event.getServerId()) });
		}

		public long insert(Event event) {
			long id = db.insert(TABLE_EVENT, null, event.getValues());
			return id;
		}

		public int count(long projId) throws ParseException {
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_EVENT, null, "project_id=" + projId
					+ " and type<>2", null, null, null, null, null);
			int numbers = c.getCount();
			c.close();
			return numbers;
		}

		public List<Event> get(long projId) throws ParseException {
			List<Event> events = new LinkedList<Event>();
			// c1:select normal events ; c2:select undetermined events
			SQLiteDatabase db = getReadableDatabase();
			Cursor c1 = db.query(TABLE_EVENT, null, "project_id=" + projId
					+ " and start_at>= Datetime('now','localtime')", null,
					null, null, "start_at ASC", null);
			Cursor c2 = db.query(TABLE_EVENT, null, "project_id=" + projId
					+ " and start_at=''", null, null, null, null, null);
			while (c1.moveToNext()) {
				Event event = new Event(
						c1.getLong(c1.getColumnIndexOrThrow(FIELD_EVENT_ID)),
						c1.getLong(c1
								.getColumnIndexOrThrow(FIELD_EVENT_PROJECTID)),
						c1.getLong(c1
								.getColumnIndexOrThrow(FIELD_EVENT_SERVERID)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_NAME)),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_EVENT_START))),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_EVENT_END))),
						c1.getString(c1
								.getColumnIndexOrThrow(FIELD_EVENT_LOCATION)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_NOTE)),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_EVENT_UPDATED_AT))));
				events.add(event);
			}
			c1.close();
			while (c2.moveToNext()) {
				Event event = new Event(
						c2.getLong(c2.getColumnIndexOrThrow(FIELD_EVENT_ID)),
						c2.getLong(c2
								.getColumnIndexOrThrow(FIELD_EVENT_PROJECTID)),
						c2.getLong(c2
								.getColumnIndexOrThrow(FIELD_EVENT_SERVERID)),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_NAME)),
						null,
						null,
						c2.getString(c2
								.getColumnIndexOrThrow(FIELD_EVENT_LOCATION)),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_NOTE)),
						formatter.parse(c2.getString(c2
								.getColumnIndexOrThrow(FIELD_EVENT_UPDATED_AT))));
				events.add(event);
			}
			c2.close();
			return events;
		}

		public Event getEvent(long id) throws ParseException {
			Event event = null;
			Date startAtDate = null;
			Date endAtDate = null;
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_EVENT, null, "server_id=" + id, null,
					null, null, null, null);
			while (c.moveToNext()) {
				String startAt = c.getString(c
						.getColumnIndexOrThrow(FIELD_EVENT_START));
				if (!startAt.equals("")) {
					startAtDate = formatter.parse(startAt);
					endAtDate = formatter.parse(c.getString(c
							.getColumnIndexOrThrow(FIELD_EVENT_END)));
				}
				event = new Event(
						c.getLong(c.getColumnIndexOrThrow(FIELD_EVENT_ID)),
						c.getLong(c
								.getColumnIndexOrThrow(FIELD_EVENT_PROJECTID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_EVENT_SERVERID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_EVENT_NAME)),
						startAtDate,
						endAtDate,
						c.getString(c
								.getColumnIndexOrThrow(FIELD_EVENT_LOCATION)),
						c.getString(c.getColumnIndexOrThrow(FIELD_EVENT_NOTE)),
						formatter.parse(c.getString(c
								.getColumnIndexOrThrow(FIELD_EVENT_UPDATED_AT))));
			}
			c.close();
			return event;
		}

		public List<Event> get() throws ParseException {
			List<Event> events = new LinkedList<Event>();

			SQLiteDatabase db = getReadableDatabase();
			Cursor c1 = db.query(TABLE_EVENT, null,
					" and start_at>= Datetime('now','localtime')", null, null,
					null, "start_at ASC", null);
			Cursor c2 = db.query(TABLE_EVENT, null, " and start_at=''", null,
					null, null, "start_at ASC", null);
			while (c1.moveToNext()) {
				Event event = new Event(
						c1.getLong(c1.getColumnIndexOrThrow(FIELD_EVENT_ID)),
						c1.getLong(c1
								.getColumnIndexOrThrow(FIELD_EVENT_PROJECTID)),
						c1.getLong(c1
								.getColumnIndexOrThrow(FIELD_EVENT_SERVERID)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_NAME)),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_EVENT_START))),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_EVENT_END))),
						c1.getString(c1
								.getColumnIndexOrThrow(FIELD_EVENT_LOCATION)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_NOTE)),
						formatter.parse(c1.getString(c1
								.getColumnIndexOrThrow(FIELD_EVENT_UPDATED_AT))));
				events.add(event);
			}
			c1.close();
			while (c2.moveToNext()) {
				Event event = new Event(
						c2.getLong(c2.getColumnIndexOrThrow(FIELD_EVENT_ID)),
						c2.getLong(c2
								.getColumnIndexOrThrow(FIELD_EVENT_PROJECTID)),
						c2.getLong(c2
								.getColumnIndexOrThrow(FIELD_EVENT_SERVERID)),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_NAME)),
						formatter.parse(c2.getString(c2
								.getColumnIndexOrThrow(FIELD_EVENT_START))),
						formatter.parse(c2.getString(c2
								.getColumnIndexOrThrow(FIELD_EVENT_END))),
						c2.getString(c2
								.getColumnIndexOrThrow(FIELD_EVENT_LOCATION)),
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_NOTE)),
						formatter.parse(c2.getString(c2
								.getColumnIndexOrThrow(FIELD_EVENT_UPDATED_AT))));
				events.add(event);
			}
			c2.close();
			return events;
		}
	}

	public class TaskEventDelegate {
		public static final int TYPE_TASK = 0;
		public static final int TYPE_EVENT = 1;

		public List<TaskEvent> get(long projectId) {
			return query("(date >= Datetime('now','localtime') OR date = '')"
					+ "AND finish = 0 " + "AND project_id = ?",
					new String[] { Long.toString(projectId) });
		}

		public List<TaskEvent> getOwned(int userId) {
			return query(
					"(date >= Datetime('now','localtime') OR date = '')"
							+ "AND finish = 0 "
							+ "AND (type = 1 OR server_id IN (SELECT task_id FROM tasks_users WHERE user_id = ?))",
					new String[] { Long.toString(userId) });
		}

		public List<TaskEvent> getOverDue(long projectId) {
			return query("(date < Datetime('now','localtime') AND date <> '')"
					+ "AND finish = 0 " + "AND project_id = ?",
					new String[] { Long.toString(projectId) });
		}

		public List<TaskEvent> getOwnedOverDue(int userId) {
			return query(
					"(date < Datetime('now','localtime') AND date <> '')"
							+ "AND finish = 0 "
							+ "AND (type = 1 OR server_id IN (SELECT task_id FROM tasks_users WHERE user_id = ?))",
					new String[] { Integer.toString(userId) });
		}

		private List<TaskEvent> query(String selection, String[] selectionArgs) {
			SQLiteDatabase db = getReadableDatabase();
			Cursor cursor = db.query("taskevents", null, selection,
					selectionArgs, null, null, null);
			LinkedList<TaskEvent> taskEvents = new LinkedList<TaskEvent>();
			while (cursor.moveToNext()) {
				TaskEvent taskevent = new TaskEvent(cursor.getLong(cursor
						.getColumnIndexOrThrow("server_id")),
						cursor.getString(cursor.getColumnIndexOrThrow("name")),
						cursor.getLong(cursor
								.getColumnIndexOrThrow("project_id")),
						stringToDate(cursor.getString(cursor
								.getColumnIndex("date"))), cursor.getInt(cursor
								.getColumnIndexOrThrow("finish")),
						cursor.getInt(cursor.getColumnIndexOrThrow("type")));
				taskEvents.add(taskevent);
			}
			try {
				return taskEvents;
			} finally {
				cursor.close();
			}
		}
	}

	private Date stringToDate(String dateString) {
		// TODO: is these two line needed?
		if (dateString == null)
			return null;
		try {
			return formatter.parse(dateString);
		} catch (ParseException e) {
			return null;
		}
	}

	public class GroupDocDelegate {
		public void insert(GroupDoc groupDoc) {
			db.insert(TABLE_GROUPDOC, null, groupDoc.getValues());
		}

		public void delete(long projId) {
			if (projId < 0)
				return;
			db.delete(TABLE_GROUPDOC, "project_id = ?",
					new String[] { String.valueOf(projId) });
		}

		public GroupDoc get(long projId) throws ParseException {
			GroupDoc groupDoc = null;

			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_GROUPDOC, null, "project_id=" + projId,
					null, null, null, null);

			while (c.moveToNext()) {
				groupDoc = new GroupDoc(
						c.getLong(c.getColumnIndexOrThrow(FIELD_GROUPDOC_ID)),
						c.getLong(c
								.getColumnIndexOrThrow(FIELD_GROUPDOC_PROJECTID)),
						c.getLong(c
								.getColumnIndexOrThrow(FIELD_GROUPDOC_SERVERID)),
						c.getString(c
								.getColumnIndexOrThrow(FIELD_GROUPDOC_CONTENT)),
						formatter.parse(c.getString(c
								.getColumnIndexOrThrow(FIELD_GROUPDOC_UPDATED_AT))));
			}
			c.close();
			return groupDoc;
		}

		public void update(GroupDoc groupDoc) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = groupDoc.getValues();
			db.update(TABLE_GROUPDOC, values, "server_id = ?",
					new String[] { String.valueOf(groupDoc.getServerId()) });
		}
	}

}
