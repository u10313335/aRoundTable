package tw.jouou.aRoundTable;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.stmt.QueryBuilder;

import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ContactsActivity extends Activity {
	private Project project;
	private Handler handler = new Handler();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		project = (Project) getIntent().getExtras().get("proj");
		
		setContentView(R.layout.contacts);
		ContactsAdapter adapter = new ContactsAdapter();
		((ListView) findViewById(R.id.members_list)).setAdapter(adapter);
		((ListView) findViewById(R.id.members_list)).setOnItemClickListener(adapter);
		findViewById(R.id.actbtn_add_member).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ContactsActivity.this, InviteMemberActivity.class);
				intent.putExtra("proj", project);
				startActivity(intent);
			}
		});
	}
	
	private class ContactsAdapter extends BaseAdapter implements OnItemClickListener{
		private List<User> members;
		
		public ContactsAdapter() {
			super();
			try {
				DBUtils dbUtils = DBUtils.getInstance(ContactsActivity.this);
				QueryBuilder<User, Integer> queryBuilder = dbUtils.userDao.queryBuilder();
				queryBuilder.where().eq("project_id", project.getServerId());
				members = dbUtils.userDao.query(queryBuilder.prepare());
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public int getCount() {
			return members.size();
		}

		@Override
		public Object getItem(int position) {
			return members.get(position);
		}

		@Override
		public long getItemId(int position) {
			return members.get(position).id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final User member = members.get(position);
			if(convertView == null) {
				convertView = ContactsActivity.this.getLayoutInflater().inflate(R.layout.contacts_item, parent, false);
			}
			((TextView) convertView.findViewById(R.id.username)).setText(member.name);
			((TextView) convertView.findViewById(R.id.email)).setText(member.email);
			final ImageView avatarImageView = (ImageView) convertView.findViewById(R.id.avatar);
			new Thread(new Runnable() {	
				@Override
				public void run() {
					final Bitmap avatar = member.getGravatar();
					handler.post(new Runnable(){
						@Override
						public void run() {
							avatarImageView.setImageBitmap(avatar);	
						}
					});
				}
			}).start();
			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			Intent mailIntent = new Intent(android.content.Intent.ACTION_SEND);
            mailIntent.setType("plain/text");
            mailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{((User) getItem(position)).email});
            mailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, String.format("[%s]", project.getName()));
            startActivity(mailIntent);
		}
	}
}
