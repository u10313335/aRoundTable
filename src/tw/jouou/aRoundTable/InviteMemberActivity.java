package tw.jouou.aRoundTable;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class InviteMemberActivity extends Activity {
	
	private DBUtils dbUtils;
	private static List<User> users;
	private Bundle bunde;
	private String projname;
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
        
        ArrayAdapter<String> ad = new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1,member_names);
    	memberlist.setAdapter(ad);
        
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
    			
    			if (dbUtils == null) {
    	    		dbUtils = new DBUtils(InviteMemberActivity.this);
    	    	}
    			
    	    	users = dbUtils.userDelegate.get();
    	    	String token = users.get(0).getToken();
    	        dbUtils.close();
    			
    			HttpPost request = new HttpPost("http://api.hime.loli.tw/projects");
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("token", token));
				params.add(new BasicNameValuePair("project[name]", projname));
				
 
				try {
					request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					HttpResponse response = new DefaultHttpClient().execute(request);
					if(response.getStatusLine().getStatusCode() == 200) {
						String post_json = EntityUtils.toString(response.getEntity());
						JSONObject json1 = new JSONObject(post_json);
						JSONObject json2 = new JSONObject(json1.getString("project"));
						String result = json2.getString("id");
						Toast.makeText(InviteMemberActivity.this, post_json, Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					Toast.makeText(InviteMemberActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
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
