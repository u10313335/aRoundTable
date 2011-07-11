package tw.jouou.aRoundTable;


import java.util.ArrayList;
import java.util.List;

import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class InviteMemberActivity extends Activity {
	
	private DBUtils dbUtils;
	private static List<User> users;
	private Bundle bunde;
	private String projname;
	private static String TAG = "InviteMemberActivity";
	private String[] member_names = new String[]{"jack","michruo","albb","sol","bearRu"};
	
	private AutoCompleteTextView member_email;
	private TextView projname_display;
	private TextView invite;
	private ListView memberlist;
	private ImageButton add_member;
	private Button confirm;
	private Button cancel;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitemember);

        findViews();
        
        bunde = this.getIntent().getExtras();
        projname = bunde.getString("projname");
        projname_display.setText("Project name:"+projname);
        
        ArrayAdapter<String> contactsname = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,member_names);
        
    	memberlist.setAdapter(contactsname);
        
    	// email auto-complete
    	// TODO:make it much more faster
    	ArrayAdapter<String> member_email_adapter = new ArrayAdapter<String> (
    				this, android.R.layout.simple_spinner_item, getContactsName());
    	member_email_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    member_email.setAdapter(member_email_adapter);
    	
    	cancel.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View arg0) {
    			InviteMemberActivity.this.finish();
    		}
        });
    	
    	confirm.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View arg0) {
    			// TODO:insert invite memeber here
    		}
        });

    }

	private void findViews() {
		projname_display = (TextView)findViewById(R.id.projname_display);
		invite = (TextView)findViewById(R.id.invite);
		member_email = (AutoCompleteTextView)findViewById(R.id.member_email);
		add_member = (ImageButton)findViewById(R.id.add_member);
		memberlist = (ListView)findViewById(R.id.memberlist);
		confirm = (Button)findViewById(R.id.confirm);
		cancel = (Button)findViewById(R.id.cancel);
	}
	
	// get user's phone contacts
	public List<String> getContactsName() {
        int i = 0;
        List<String> contactsName = new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query (ContactsContract.Contacts.CONTENT_URI,
        		null, null, null, null);
    
        if (cur.getCount() > 0) {
        	while (cur.moveToNext()) {
        		String id = cur.getString (
        				cur.getColumnIndex(ContactsContract.Contacts._ID));
        		Cursor emailCur = cr.query (
        				ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
        				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
        				new String[]{id}, null);
        		while (emailCur.moveToNext()) { 
        			contactsName.add(emailCur.getString(
        				emailCur.getColumnIndex( ContactsContract.CommonDataKinds.Email.DATA ) ) );
        				i++;
        		}
        	}
        }   
    	return contactsName;  
    }
}
