package tw.jouou.aRoundTable;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class InviteMemberActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitemember);
        
        
        findViews();
        
        Bundle bunde = this.getIntent().getExtras();
        String projname = bunde.getString("projname");
        projname_display.setText("Project name:"+projname);
        
        ArrayAdapter<String> ad=new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1,member_names);

    	memberlist.setAdapter(ad);
        
        
        
        
        
        
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
