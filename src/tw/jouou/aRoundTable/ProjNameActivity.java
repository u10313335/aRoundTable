package tw.jouou.aRoundTable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ProjNameActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.projname);
           
        findViews();
        setListeners();
  
    }
	
	private TextView projname_input;
	private EditText projname;
	private Button next;
	
	
	private void findViews()
	{
		projname_input = (TextView)findViewById(R.id.projname_input_prompt);
		projname = (EditText)findViewById(R.id.projname_input);
		next = (Button)findViewById(R.id.next);
		
	}
	
	private void setListeners()
	{
			next.setOnClickListener( new OnClickListener(){
            
            public void onClick(View v){
            	
            	Intent intent = new Intent();
    			intent.setClass(ProjNameActivity.this, InviteMemberActivity.class);
    			Bundle bundle = new Bundle();
    			bundle.putString("projname", projname.getText().toString());
    			intent.putExtras(bundle);
    			startActivity(intent);
            	
            }
        });
	}

}
