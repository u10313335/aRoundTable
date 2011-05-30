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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class InviteMemberActivity extends Activity {
	
	private DBUtils dbUtils;
	private static List<User> users;
	private Bundle bunde;
	private String projname;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitemember);

        findViews();
        
        bunde = this.getIntent().getExtras();
        projname = bunde.getString("projname");
        projname_display.setText("Project name:"+projname);
        
        ArrayAdapter<String> ad = new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1,member_names);
        
        ArrayAdapter<String> contactsname = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,getContactsEmail());
        
    	member_name.setAdapter(contactsname);

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
					if(response.getStatusLine().getStatusCode() == 200){
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
	
	private TextView projname_display;
	private TextView invite;
	private AutoCompleteTextView member_name;
	private ListView memberlist;
	private ImageButton add_member;
	private Button confirm;
	private Button cancel;
	private String[] member_names = new String[]{"jack","michruo","albb","sol","bearRu"};
	
	private void findViews()
	{
		projname_display = (TextView)findViewById(R.id.projname_display);
		invite = (TextView)findViewById(R.id.invite);
		member_name = (AutoCompleteTextView)findViewById(R.id.member_name);
		add_member = (ImageButton)findViewById(R.id.add_member);
		memberlist = (ListView)findViewById(R.id.memberlist);
		confirm = (Button)findViewById(R.id.confirm);
		cancel = (Button)findViewById(R.id.cancel);
	}
	
	public String[] getContactsEmail() {
		
		  ContentResolver contentResolver = this.getContentResolver();
		
		  String[] projection = new String[]{Contacts.People.NAME,Contacts.People.NUMBER};
		
		  Cursor cursor = contentResolver.query(Contacts.People.CONTENT_URI, projection, null, null, Contacts.People.DEFAULT_SORT_ORDER);
		  
		  String[] contactsName = new String[cursor.getCount()];
		  
		  for (int i = 0; i < cursor.getCount(); i++) {
		
		       cursor.moveToPosition(i);
		
		       contactsName[i] = cursor.getString(0);
		     }
		        return contactsName;
		  }

	
}
