package tw.jouou.aRoundTable.util;

import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.bean.Project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBUtils extends SQLiteOpenHelper {

	public DBUtils(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	public final static String DB_NAME = "aRoundTable";
	public final static int DB_VERSION = 1;
	
	public final static String TABLE_USERS = "user";
	public final static String FIELD_USERS_ID = "_id";
	public final static String FIELD_USERS_TOKEN = "token";
	
	public final static String TABLE_PROJECTS = "project";
	public final static String FIELD_PROJECTS_ID = "_id";
	public final static String FIELD_PROJECTS_NAME = "name";
	public final static String FIELD_PROJECTS_SERVERID = "server_id";
	public final static String FIELD_PROJECTS_COLOR = "color";
	
	public final static String TABLE_TASK = "task";
	public final static String FIELD_TASK_ID = "_id";
	public final static String FIELD_TASK_NAME = "name";
	public final static String FIELD_TASK_PROJECTID = "project_id";
	public final static String FIELD_TASK_SERVERID = "server_id";
	public final static String FIELD_TASK_DUEDATE = "due";
	public final static String FIELD_TASK_NOTE = "note";
	public final static String FIELD_TASK_FINISHED = "finish";
	
	public final static String TABLE_EVENT = "event";
	public final static String FIELD_EVENT_ID = "_id";
	public final static String FIELD_EVENT_NAME = "name";
	public final static String FIELD_EVENT_PROJECTID = "project_id";
	public final static String FIELD_EVENT_SERVERID = "server_id";
	public final static String FIELD_EVENT_START = "start_at";
	public final static String FIELD_EVENT_END = "end_at";
	public final static String FIELD_EVENT_LOCATION = "location";
	public final static String FIELD_EVENT_NOTE = "note";
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_USERS
				+ "( " + FIELD_USERS_ID + " INTEGER PRIMARY KEY, "
				+ FIELD_USERS_TOKEN + " TEXT )");
	
		db.execSQL( "CREATE TABLE " + TABLE_PROJECTS
				+ "( " + FIELD_PROJECTS_ID + " INTEGER PRIMARY KEY, "
				+ FIELD_PROJECTS_NAME + " TEXT, "
				+ FIELD_PROJECTS_SERVERID + " INTEGER, "
				+ FIELD_PROJECTS_COLOR + " INTEGER )");
		
		db.execSQL( "CREATE TABLE " + TABLE_TASK
				+ "( " + FIELD_TASK_ID + " INTEGER PRIMARY KEY, "
				+ FIELD_TASK_NAME + " TEXT, "
				+ FIELD_TASK_PROJECTID + " INTEGER, "
				+ FIELD_TASK_SERVERID + " INTEGER, "
				+ FIELD_TASK_DUEDATE + " DATETIME, "
				+ FIELD_TASK_NOTE + " TEXT, "
				+ FIELD_TASK_FINISHED + " INTEGER )");
		
		db.execSQL( "CREATE TABLE " + TABLE_EVENT
				+ "( " + FIELD_EVENT_ID + " INTEGER PRIMARY KEY, "
				+ FIELD_EVENT_NAME + " TEXT, "
				+ FIELD_EVENT_PROJECTID + " INTEGER, "
				+ FIELD_EVENT_SERVERID + " INTEGER, "
				+ FIELD_EVENT_START + " DATETIME, "
				+ FIELD_EVENT_END + " DATETIME, "
				+ FIELD_EVENT_LOCATION + " TEXT, "
				+ FIELD_EVENT_NOTE + " TEXT )");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
/*		switch (oldVersion) {
		case 1:			
			db.execSQL( "CREATE TABLE " + TABLE_TASK
					+ " (_id INTEGER PRIMARY KEY, "
					+ FIELD_TASK_NAME + " TEXT, "
					+ FIELD_TASK_PROJECTID + " INTEGER, "
					+ FIELD_TASK_SERVERID + " INTEGER, "
					+ FIELD_TASK_DUEDATE + " DATE, "
					+ FIELD_TASK_NOTE + " TEXT, "
					+ FIELD_TASK_TYPE + " INTEGER, "
					+ FIELD_TASK_FINISHED + " INTEGER )");
		}
*/
	}
	
	public UsersDelegate userDelegate = new UsersDelegate();
	public ProjectsDelegate projectsDelegate = new ProjectsDelegate();
	public TaskDelegate tasksDelegate = new TaskDelegate();
	public EventDelegate eventsDelegate = new EventDelegate();
	
	public class UsersDelegate {
		public void delete(User user) {
			if (user.getId() < 0)
				return;

			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_USERS, "_id = ?", new String[] { String
					.valueOf(user.getId()) });
			db.close();	
		}
		
		public void update(User user) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = user.getValues();
			db.update(TABLE_USERS, values, "_id =?", new String[] { String
					.valueOf(user.getId()) });
			db.close();
		}
	
		public void insert(User user) {
			SQLiteDatabase db = getWritableDatabase();
			db.insert(TABLE_USERS, null, user.getValues());
			db.close();
		}
	
		public List<User> get() {
			List<User> users = new LinkedList<User>();
		
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_USERS, null, null, null, null, null,
					FIELD_USERS_ID + " ASC");

			while (c.moveToNext()) {
				User user = new User(c.getLong(c.getColumnIndexOrThrow(FIELD_USERS_ID)), 
						c.getString(c.getColumnIndexOrThrow(FIELD_USERS_TOKEN)));		
				users.add(user);
			}
			c.close();
			db.close();
			return users;
		}
	}
	
	public class ProjectsDelegate {
		public void delete(Project proj) {
			if (proj.getId() < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_USERS, "_id = ?", new String[] { String
					.valueOf(proj.getId()) });
			db.close();
		}
		
		public void update(Project proj) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = proj.getValues();
			db.update(TABLE_PROJECTS, values, "_id =?", new String[] { String
					.valueOf(proj.getId()) });
			db.close();
		}
	
		public long insert(Project proj) {
			SQLiteDatabase db = getWritableDatabase();
			long id = db.insert(TABLE_PROJECTS, null, proj.getValues());
			db.close();
			return id;
		}
	
		public List<Project> get() {
			List<Project> projs = new LinkedList<Project>();
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_PROJECTS, null, null, null, null, null,
					FIELD_PROJECTS_ID + " DESC");
			while (c.moveToNext()) {
				Project proj = new Project(c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECTS_ID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_PROJECTS_NAME)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECTS_SERVERID)),
						c.getInt(c.getColumnIndexOrThrow(FIELD_PROJECTS_COLOR)));
				projs.add(proj);
			}
			c.close();
			db.close();
			return projs;
		}
		
		public Project get(long projId) {
			Project proj = null;
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_PROJECTS, null, "_id=" + projId, null, null, null, null);
			while (c.moveToNext()) {
				proj = new Project(c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECTS_ID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_PROJECTS_NAME)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECTS_SERVERID)),
						c.getInt(c.getColumnIndexOrThrow(FIELD_PROJECTS_COLOR)));
			}
			c.close();
			db.close();
			return proj;
		}
	}
	
	public class TaskDelegate {
		public void delete(Task task) {
			if (task.getId() < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_TASK, "_id = ?", new String[] { String
					.valueOf(task.getId()) });
			db.close();
		}
		
		public void update(Task task) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = task.getValues();
			db.update(TABLE_TASK, values, "_id = ?", new String[] { String
					.valueOf(task.getId()) });
			db.close();
		}
	
		public long insert(Task task) {
			SQLiteDatabase db = getWritableDatabase();
			long id = db.insert(TABLE_TASK, null, task.getValues());
			db.close();
			return id;
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
							c1.getInt(c1.getColumnIndexOrThrow(FIELD_TASK_FINISHED)));
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
							c2.getInt(c2.getColumnIndexOrThrow(FIELD_TASK_FINISHED)));
				tasks.add(task);
			}
			c2.close();
			db.close();
			return tasks;
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
							c1.getInt(c1.getColumnIndexOrThrow(FIELD_TASK_FINISHED)));
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
							c2.getInt(c2.getColumnIndexOrThrow(FIELD_TASK_FINISHED)));
				tasks.add(task);
			}
			c2.close();
			db.close();
			return tasks;
		}
	}


	public class EventDelegate {
		public void delete(Event event) {
			if (event.getId() < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_EVENT, "_id = ?", new String[] { String
					.valueOf(event.getId()) });
			db.close();
		}
		
		public void update(Event event) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = event.getValues();
			db.update(TABLE_EVENT, values, "_id = ?", new String[] { String
					.valueOf(event.getId()) });
			db.close();
		}
	
		public long insert(Event event) {
			SQLiteDatabase db = getWritableDatabase();
			long id = db.insert(TABLE_EVENT, null, event.getValues());
			db.close();
			return id;
		}
	
		public List<Event> get(long projId) throws ParseException {
			List<Event> events = new LinkedList<Event>();
			
			SQLiteDatabase db = getReadableDatabase();
			Cursor c1 = db.query(TABLE_EVENT, null,
					"project_id=" + projId + " and start_at>= Datetime('now','localtime')",
					null, null, null,
					"due ASC", null);
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
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_NOTE)));
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
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_NOTE)));
				events.add(event);
			}
			c2.close();
			db.close();
			return events;
		}
		
		public List<Event> get() throws ParseException {
			List<Event> events = new LinkedList<Event>();
		
			SQLiteDatabase db = getReadableDatabase();
			Cursor c1 = db.query(TABLE_EVENT, null, " and start_at>= Datetime('now','localtime')", null, null, null,
					"due ASC", null);
			Cursor c2 = db.query(TABLE_EVENT, null, " and start_at=''", null, null, null,
					"due ASC", null);
			while (c1.moveToNext()) {
				Event event = new Event(c1.getLong(c1.getColumnIndexOrThrow(FIELD_EVENT_ID)),
						c1.getLong(c1.getColumnIndexOrThrow(FIELD_EVENT_PROJECTID)),
						c1.getLong(c1.getColumnIndexOrThrow(FIELD_EVENT_SERVERID)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_NAME)),
						formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_START))),
						formatter.parse(c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_END))),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_LOCATION)),
						c1.getString(c1.getColumnIndexOrThrow(FIELD_EVENT_NOTE)));
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
						c2.getString(c2.getColumnIndexOrThrow(FIELD_EVENT_NOTE)));
				events.add(event);
			}
			c2.close();
			db.close();
			return events;
		}
	}

}
