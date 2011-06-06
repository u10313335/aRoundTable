package tw.jouou.aRoundTable;

import java.util.List;

import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class InviteMemberActivity extends Activity {
	
	private DBUtils dbUtils;
	private static List<User> users;
	private Bundle bunde;
	private String projname;
	private static String TAG = "InviteMemberActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitemember);

        findViews();
        
        bunde = this.getIntent().getExtras();
        projname = bunde.getString("projname");
        projname_display.setText("Project name:"+projname);
        
        ArrayAdapter<String> ad = new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1, member_names);

    	memberlist.setAdapter(ad);
        
    	cancel.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View arg0) {
    			InviteMemberActivity.this.finish();
    		}
        });
    	
    	confirm.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View arg0) {

    		}
        });
        

        
    }
	
	private TextView projname_display;
	private TextView invite;
	private EditText member_name;
	private ListView memberlist;
	private ImageButton add_member;
	private Button confirm;
	private Button cancel;
	private String[] member_names = new String[]{"jack","michruo","albb","sol","bearRu"};
	
	private void findViews()
	{
		projname_display = (TextView)findViewById(R.id.projname_display);
		invite = (TextView)findViewById(R.id.invite);
		member_name = (EditText)findViewById(R.id.member_name);
		add_member = (ImageButton)findViewById(R.id.add_member);
		memberlist = (ListView)findViewById(R.id.memberlist);
		confirm = (Button)findViewById(R.id.confirm);
		cancel = (Button)findViewById(R.id.cancel);
	}
	
}
