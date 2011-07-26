package tw.jouou.aRoundTable.util;

import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.bean.Project;

import java.text.ParseException;
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
	
	public final static String TABLE_TASKEVENT = "taskevent";
	public final static String FIELD_TASKEVENT_ID = "_id";
	public final static String FIELD_TASKEVENT_NAME = "name";
	public final static String FIELD_TASKEVENT_PROJECTID = "project_id";
	public final static String FIELD_TASKEVENT_SERVERID = "server_id";
	public final static String FIELD_TASKEVENT_DUEDATE = "due";
	public final static String FIELD_TASKEVENT_NOTE = "note";
	public final static String FIELD_TASKEVENT_TYPE = "type";
	public final static String FIELD_TASKEVENT_FINISHED = "finish";

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_USERS
				+ " (_id INTEGER PRIMARY KEY, "
				+ FIELD_USERS_TOKEN + " TEXT )");
	
		db.execSQL( "CREATE TABLE " + TABLE_PROJECTS
				+ " (_id INTEGER PRIMARY KEY, "
				+ FIELD_PROJECTS_NAME + " TEXT, "
				+ FIELD_PROJECTS_SERVERID + " INTEGER )");
		
		db.execSQL( "CREATE TABLE " + TABLE_TASKEVENT
				+ " (_id INTEGER PRIMARY KEY, "
				+ FIELD_TASKEVENT_NAME + " TEXT, "
				+ FIELD_TASKEVENT_PROJECTID + " INTEGER, "
				+ FIELD_TASKEVENT_SERVERID + " INTEGER, "
				+ FIELD_TASKEVENT_DUEDATE + " DATE, "
				+ FIELD_TASKEVENT_NOTE + " TEXT, "
				+ FIELD_TASKEVENT_TYPE + " INTEGER, "
				+ FIELD_TASKEVENT_FINISHED + " INTEGER )");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
/*		switch (oldVersion) {
		case 1:			
			db.execSQL( "CREATE TABLE " + TABLE_TASKEVENT
					+ " (_id INTEGER PRIMARY KEY, "
					+ FIELD_TASKEVENT_NAME + " TEXT, "
					+ FIELD_TASKEVENT_PROJECTID + " INTEGER, "
					+ FIELD_TASKEVENT_SERVERID + " INTEGER, "
					+ FIELD_TASKEVENT_DUEDATE + " DATE, "
					+ FIELD_TASKEVENT_NOTE + " TEXT, "
					+ FIELD_TASKEVENT_TYPE + " INTEGER, "
					+ FIELD_TASKEVENT_FINISHED + " INTEGER )");
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
						c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECTS_SERVERID)));
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
						c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECTS_SERVERID)));
			}
			c.close();
			db.close();
			return proj;
		}
	}
	
	public class TaskEventDelegate {
		public void delete(TaskEvent taskevent) {
			if (taskevent.getId() < 0)
				return;
			SQLiteDatabase db = getWritableDatabase();
			db.delete(TABLE_TASKEVENT, "_id = ?", new String[] { String
					.valueOf(taskevent.getId()) });
			db.close();
		}
		
		public void update(TaskEvent taskevent) {
			SQLiteDatabase db = getReadableDatabase();
			ContentValues values = taskevent.getValues();
			db.update(TABLE_TASKEVENT, values, "_id =?", new String[] { String
					.valueOf(taskevent.getId()) });
			db.close();
		}
	
		public long insert(TaskEvent taskevent) {
			SQLiteDatabase db = getWritableDatabase();
			long id = db.insert(TABLE_TASKEVENT, null, taskevent.getValues());
			db.close();
			return id;
		}
	
		public List<TaskEvent> get(long projId) throws ParseException {
			List<TaskEvent> taskevents = new LinkedList<TaskEvent>();
		
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASKEVENT, null, "project_id=" + projId, null, null, null,
					"due DESC", null);

			while (c.moveToNext()) {
				TaskEvent taskevent = new TaskEvent(c.getLong(c.getColumnIndexOrThrow(FIELD_TASKEVENT_ID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASKEVENT_PROJECTID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASKEVENT_SERVERID)),
						c.getInt(c.getColumnIndexOrThrow(FIELD_TASKEVENT_TYPE)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASKEVENT_NAME)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASKEVENT_DUEDATE)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASKEVENT_NOTE)),
						c.getInt(c.getColumnIndexOrThrow(FIELD_TASKEVENT_FINISHED)));
				taskevents.add(taskevent);
			}

			c.close();
			db.close();

			return taskevents;
		}
		
		public List<TaskEvent> get() throws ParseException {
			List<TaskEvent> taskevents = new LinkedList<TaskEvent>();
		
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_TASKEVENT, null, null, null, null, null,
					"due DESC", null);

			while (c.moveToNext()) {
				TaskEvent taskevent = new TaskEvent(c.getLong(c.getColumnIndexOrThrow(FIELD_TASKEVENT_ID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASKEVENT_PROJECTID)),
						c.getLong(c.getColumnIndexOrThrow(FIELD_TASKEVENT_SERVERID)),
						c.getInt(c.getColumnIndexOrThrow(FIELD_TASKEVENT_TYPE)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASKEVENT_NAME)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASKEVENT_DUEDATE)),
						c.getString(c.getColumnIndexOrThrow(FIELD_TASKEVENT_NOTE)),
						c.getInt(c.getColumnIndexOrThrow(FIELD_TASKEVENT_FINISHED)));
				taskevents.add(taskevent);
			}

			c.close();
			db.close();

			return taskevents;
		}
	}
	

}
