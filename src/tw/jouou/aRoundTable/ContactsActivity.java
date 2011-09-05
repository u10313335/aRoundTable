package tw.jouou.aRoundTable;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.stmt.QueryBuilder;

import tw.jouou.aRoundTable.bean.Member;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ContactsActivity extends Activity {
	private Project project;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		project = (Project) getIntent().getExtras().get("proj");
		
		setContentView(R.layout.contacts);
		
		((ListView) findViewById(R.id.members_list)).setAdapter(new ContactsAdapter());
		findViewById(R.id.actbtn_add_member).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ContactsActivity.this, InviteMemberActivity.class);
				intent.putExtra("proj", project);
				startActivity(intent);
			}
		});
	}
	
	private class ContactsAdapter extends BaseAdapter{
		private List<Member> members;
		
		public ContactsAdapter() {
			super();
			try {
				DBUtils dbUtils =  new DBUtils(ContactsActivity.this);
				QueryBuilder<Member, Integer> queryBuilder = dbUtils.memberDao.queryBuilder();
				queryBuilder.where().eq("project_id", project.getServerId());
				members = dbUtils.memberDao.query(queryBuilder.prepare());
				
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
			Member member = members.get(position);
			if(convertView == null) {
				convertView = ContactsActivity.this.getLayoutInflater().inflate(R.layout.contacts_item, parent, false);
			}
			((TextView) convertView.findViewById(R.id.username)).setText(member.name);
			((TextView) convertView.findViewById(R.id.email)).setText(member.email);
			return convertView;
		}
	}
}
