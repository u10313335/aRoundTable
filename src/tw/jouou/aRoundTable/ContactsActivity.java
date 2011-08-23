package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.Project;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class ContactsActivity extends Activity {
	private Project project;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		project = (Project) getIntent().getExtras().get("proj");
		
		setContentView(R.layout.contacts);
		findViewById(R.id.actbtn_add_member).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ContactsActivity.this, InviteMemberActivity.class);
				intent.putExtra("proj", project);
				startActivity(intent);
			}
		});
	}
}
