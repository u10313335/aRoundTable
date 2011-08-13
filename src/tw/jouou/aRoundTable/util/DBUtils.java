package tw.jouou.aRoundTable.util;

import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.bean.Project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	public final static String FIELD_EVENT_NOTE = "note";

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
	public TaskEventDelegate taskeventsDelegate = new TaskEventDelegate();
	
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
	
	public class TaskEventDelegate {
		public void delete(Task taskevent) {
			if (taskevent.getId() < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_TASK, "_id = ?", new String[] { String
					.valueOf(taskevent.getId()) });
			db.close();
		}
		
		public void update(Task taskevent) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = taskevent.getValues();
			db.update(TABLE_TASK, values, "_id = ?", new String[] { String
					.valueOf(taskevent.getId()) });
			db.close();
		}
	
		public long insert(Task taskevent) {
			SQLiteDatabase db = getWritableDatabase();
			long id = db.insert(TABLE_TASK, null, taskevent.getValues());
			db.close();
			return id;
		}
	
		public List<Task> get(long projId) throws ParseException {
			List<Task> taskevents = new LinkedList<Task>();
			
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASK, null,
					"project_id=" + projId + " and finish=0 and due>= Datetime('now','localtime')",
					null, null, null,
					"due ASC", null);
			
			while (c.moveToNext()) {
				Task taskevent = new Task(c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_ID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASK_NAME)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASK_DUEDATE)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
						c.getInt(c.getColumnIndexOrThrow(FIELD_TASK_FINISHED)));
				taskevents.add(taskevent);
			}
			c.close();
			db.close();
			return taskevents;
		}
		
		public List<Task> get() throws ParseException {
			List<Task> taskevents = new LinkedList<Task>();
		
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASK, null, "finish=0 and due>= Datetime('now','localtime')", null, null, null,
					"due ASC", null);
			
			while (c.moveToNext()) {
				Task taskevent = new Task(c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_ID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_PROJECTID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASK_SERVERID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASK_NAME)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASK_DUEDATE)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASK_NOTE)),
						c.getInt(c.getColumnIndexOrThrow(FIELD_TASK_FINISHED)));
				taskevents.add(taskevent);
			}

			c.close();
			db.close();
			return taskevents;
		}
	}
	

}
