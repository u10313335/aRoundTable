package tw.jouou.aRoundTable;


import java.util.ArrayList;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.JoinStatus;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

public class InviteMemberActivity extends Activity implements OnClickListener {
	private Project project;
	private AutoCompleteTextView email_field;
	private ArrayAdapter<String> arrayAdapter;
	private ArrayList<String> emailsToInvite;
	private static final int REQUEST_PICK_EMAIL = 1;
	private static final String TAG = "InviteMemberActivity";

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if((project = (Project) getIntent().getExtras().get("proj")) == null){
        	Log.e(TAG, "No project specified, exiting...");
        	finish();
        }
        
        setContentView(R.layout.invite_member);
        
        ListView inviteQueue = (ListView) findViewById(R.id.listview_invite_queue);
        emailsToInvite = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emailsToInvite);
        inviteQueue.setAdapter(arrayAdapter);
        registerForContextMenu(inviteQueue);
        
        findViewById(R.id.btn_invite).setOnClickListener(this);
        findViewById(R.id.btn_from_contacts).setOnClickListener(this);
        findViewById(R.id.actbtn_clear).setOnClickListener(this);
        findViewById(R.id.actbtn_finish).setOnClickListener(this);
        email_field = (AutoCompleteTextView) findViewById(R.id.email_field);
        (new PrepareCursorTask()).execute((Void[]) null);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.actbtn_clear:
			arrayAdapter.clear();
			break;
		case R.id.btn_invite:
			String email = email_field.getText().toString();
			if(arrayAdapter.getPosition(email) == -1)
				arrayAdapter.add(email);
			break;
		case R.id.btn_from_contacts:
			startActivityForResult(new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI), REQUEST_PICK_EMAIL);
			break;
		case R.id.actbtn_finish:
			String[] emails = new String[emailsToInvite.size()];
			emailsToInvite.toArray(emails);
			(new AddMenbersTask()).execute(emails);
			break;
		}
	}
	
	@Override
	 public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add("Delete");
	 }
	
	@Override
	 public boolean onContextItemSelected(MenuItem item) {
		int position = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
		arrayAdapter.remove(arrayAdapter.getItem(position));
		return true;
	 }
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		switch (reqCode) {
		case REQUEST_PICK_EMAIL:
			if (resultCode == Activity.RESULT_OK) {
				Cursor cursor =  getContentResolver().query(data.getData(), new String[] {Contacts._ID}, null,null, null);
				if(!cursor.moveToNext())
					return;
				
				String id = cursor.getString(0);
				cursor = getContentResolver().query(Email.CONTENT_URI, new String[] {Email.DATA}, Email.CONTACT_ID + " = ?", new String[] {id}, null);
				int count = cursor.getCount();
				if(count == 1){
					cursor.moveToFirst();
					arrayAdapter.add(cursor.getString(0));
				}else if (count > 1){
					final String emails[] = new String[count];
					int i = 0;
					while(cursor.moveToNext()){
						emails[i++] = cursor.getString(0);
					}
					(new AlertDialog.Builder(InviteMemberActivity.this)).setItems(emails, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							arrayAdapter.add(emails[which]);
						}
					}).show();
				}
			}
			break;
		}
	}
	
	private class PrepareCursorTask extends AsyncTask<Void, Void, Cursor>{

		@Override
		protected Cursor doInBackground(Void... params) {
			Cursor cursor = InviteMemberActivity.this.getContentResolver().query(Email.CONTENT_URI, new String[] {Email.DATA}, null, null, null);
			return cursor;
		}
		
		@Override
		protected void onPostExecute(Cursor cursor) {
			String[] emails = new String[cursor.getCount()];
			int i = 0;
			
			while(cursor.moveToNext()) {
				emails[i++] = cursor.getString(0);
			}
			
			((AutoCompleteTextView) findViewById(R.id.email_field))
				.setAdapter(new ArrayAdapter<String>(InviteMemberActivity.this, R.layout.email_autocomplete_item, 	emails));
		}
	}
	
	private class AddMenbersTask extends AsyncTask<String, Void, JoinStatus[]>{
		private ProgressDialog dialog;
		private Exception exception;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(InviteMemberActivity.this);
			dialog.setMessage(getString(R.string.processing));
			dialog.show();
		}
		
		@Override
		protected JoinStatus[] doInBackground(String... emails) {
			try {
				return ArtApi.getInstance(InviteMemberActivity.this).addMember(project.getServerId(), emails);
			} catch (ServerException e) {
				exception = e;
			} catch (ConnectionFailException e) {
				exception = e;
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(JoinStatus[] joinStatus){
			dialog.dismiss();
			
			if(exception != null){
				if(exception instanceof ServerException){
					Toast.makeText(InviteMemberActivity.this, "Server exception: "+exception.getMessage(), Toast.LENGTH_SHORT).show();
				}else if(exception instanceof ConnectionFailException){
					Toast.makeText(InviteMemberActivity.this, "Network failed, please try later", Toast.LENGTH_SHORT).show();
					exception.printStackTrace();
				}
				return;
			}
			
			//TODO: Better join status display
			int success=0, invited=0, failed=0;
			for(JoinStatus j : joinStatus){
				switch(j){
				case SUCCESS:
					success++;
					break;
				case INVITED:
					invited++;
					break;
				case FAILED:
					failed++;
					break;
				}
			}
			
			Toast.makeText(InviteMemberActivity.this, "success:"+success+", invited:"+invited+"failed: "+failed, Toast.LENGTH_SHORT).show();
			finish();
		}
	}
}
