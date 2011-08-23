package tw.jouou.aRoundTable;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

public class InviteMemberActivity extends Activity implements OnClickListener {
	private AutoCompleteTextView email_field;
	private ArrayAdapter<String> arrayAdapter;
	private static final int REQUEST_PICK_EMAIL = 1;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_member);
        
        ((ListView) findViewById(R.id.listview_invite_queue)).setAdapter(arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1));
        
        findViewById(R.id.btn_invite).setOnClickListener(this);
        findViewById(R.id.btn_from_contacts).setOnClickListener(this);
        findViewById(R.id.actbtn_clear).setOnClickListener(this);
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
			startActivityForResult(new Intent(Intent.ACTION_PICK, Email.CONTENT_URI), REQUEST_PICK_EMAIL);
			break;
		}
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		switch (reqCode) {
		case REQUEST_PICK_EMAIL:
			if (resultCode == Activity.RESULT_OK) {
				Cursor cursor =  getContentResolver().query(data.getData(), null, null,null, null);
				if(cursor.moveToNext()) {
					String email = cursor.getString(cursor.getColumnIndex(Email.DATA));
					arrayAdapter.add(email);
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
				.setAdapter(new ArrayAdapter<String>(InviteMemberActivity.this, R.layout.email_autocomplete_item, emails));
		}
	}
}
