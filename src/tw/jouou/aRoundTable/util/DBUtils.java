package tw.jouou.aRoundTable.util;

import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.bean.Project;
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

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_USERS
				+ " (_id INTEGER PRIMARY KEY, "
				+ FIELD_USERS_TOKEN + " TEXT )");
	
		db.execSQL( "CREATE TABLE " + TABLE_PROJECTS
				+ " (_id INTEGER PRIMARY KEY, "
				+ FIELD_PROJECTS_NAME + " TEXT )" );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		switch (oldVersion) {
		/*case 1:
			db.execSQL("ALTER TABLE " + TABLE_HOSTS + " ADD COLUMN "
					+ FIELD_HOSTS_ENCODING + " TEXT DEFAULT 'GBK'");*/
		}
	}
	
	public UsersDelegate userDelegate = new UsersDelegate();
	
	
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
			long id = db.insert(TABLE_USERS, null, user.getValues());
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
	
		public void insert(Project proj) {
			SQLiteDatabase db = getWritableDatabase();
			long id = db.insert(TABLE_PROJECTS, null, proj.getValues());
			db.close();
		}
	
		public List<Project> get() {
			List<Project> projs = new LinkedList<Project>();
		
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_PROJECTS, null, null, null, null, null,
					FIELD_PROJECTS_ID + " DSC");

			while (c.moveToNext()) {
				Project proj = new Project(c.getLong(c.getColumnIndexOrThrow(FIELD_PROJECTS_ID)),
						c.getString(c.getColumnIndexOrThrow(FIELD_PROJECTS_NAME)));

				projs.add(proj);
			}

			c.close();
			db.close();

			return projs;
		}
	}

}
