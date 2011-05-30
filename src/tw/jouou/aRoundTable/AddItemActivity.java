package tw.jouou.aRoundTable;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class AddItemActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additem);
        
        findViews();
        setListeners();
        
    }

	
	  private Button single;
      private Button assignment;
      private Button event;
      private TextView title;
      private EditText title_context;
      private TextView time;
      private Button one_day;
      private Button seven_day;
      private Button n_day;
      private ImageButton date;
      private Button dependent;
      private Button customize;
      private EditText dependency;
      private TextView owner;
      private TextView owner_context;
      private EditText owneradd_context;
      private Editable owner_name;
      private String owner_name_temp = "";
      private ImageButton owner_add;
      private TextView remarks;
      private EditText remarks_context;
      private Button additem_finish;
      private Button additem_cancel;
      
      
      private void findViews()
      {
    	  
    	  single = (Button)findViewById(R.id.single);
          assignment = (Button)findViewById(R.id.assignment);
          event = (Button)findViewById(R.id.event);
          title = (TextView)findViewById(R.id.title);
          title_context = (EditText)findViewById(R.id.title_context); 
          time = (TextView)findViewById(R.id.time);
          one_day = (Button)findViewById(R.id.one_day);
          seven_day = (Button)findViewById(R.id.seven_day);
          n_day = (Button)findViewById(R.id.n_day);
          date = (ImageButton)findViewById(R.id.date);
          dependent = (Button)findViewById(R.id.dependent);
          customize = (Button)findViewById(R.id.customize);
          dependency = (EditText)findViewById(R.id.dependency);
          owner = (TextView)findViewById(R.id.owner);
          owner_context = (TextView)findViewById(R.id.owner_context);
          owneradd_context = (EditText)findViewById(R.id.owneradd_context);
          
          owner_add = (ImageButton)findViewById(R.id.owner_add);
          remarks = (TextView)findViewById(R.id.remarks);
          remarks_context = (EditText)findViewById(R.id.remarks_context);
          additem_finish = (Button)findViewById(R.id.additem_finish);
          additem_cancel = (Button)findViewById(R.id.additem_cancel);
    			
    		
      }
      
     private void setListeners()
      
      {
    	  additem_cancel.setOnClickListener(cancel);
    	  owner_add.setOnClickListener(addowner);
      }
      
      
      public OnClickListener cancel = new OnClickListener(){
  		
  		public void onClick(View v){
  			
  			
  			AddItemActivity.this.finish();
  			
  			
  		}
  		
  	  };
  	  
  	  public OnClickListener addowner = new OnClickListener(){
  		
  		public void onClick(View v){
  			
  			owner_name = owneradd_context.getText();
  			
  			owner_name_temp = owner_name_temp + owner_name;
  			
  			owner_context.setText("  "+owner_name_temp);
  			
  			owner_context.setTextColor(0xff00ff00);
  			
  		}
  		
  	  };
  	  
  	  
      
      

  	
}