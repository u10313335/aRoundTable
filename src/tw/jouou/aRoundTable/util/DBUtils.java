package tw.jouou.aRoundTable.util;

import tw.jouou.aRoundTable.bean.User;
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

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_USERS
				+ " (_id INTEGER PRIMARY KEY, "
				+ FIELD_USERS_TOKEN + " TEXT )");
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
	
		public User insert(User user) {
			SQLiteDatabase db = getWritableDatabase();

			long id = db.insert(TABLE_USERS, null, user.getValues());
			db.close();
			user.setId(id);
			return user;
		}
	
		public List<User> get() {
			List<User> users = new LinkedList<User>();
		
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TABLE_USERS, null, null, null, null, null,
					FIELD_USERS_TOKEN + " ASC");

			while (c.moveToNext()) {
				User user = new User();

				user.setId(c.getLong(c.getColumnIndexOrThrow(FIELD_USERS_ID)));
				user.setToken(c.getString(c
					.getColumnIndexOrThrow(FIELD_USERS_TOKEN)));
			
				users.add(user);
			}

			c.close();
			db.close();

			return users;
		}
	}

}
