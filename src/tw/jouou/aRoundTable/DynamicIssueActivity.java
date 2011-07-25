package tw.jouou.aRoundTable;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DynamicIssueActivity extends Activity {
	
	private ListView dynamic_issue_list;
	
	private ImageView image;
	
	private TextView name;
	
	private TextView title;
	
	private Button apply;
	
	private Button like;
	
	private String[] titles = new String[]{"真無聊","想要買台MBP","Dr.pepper!!","哇哈哈哈","Mac OS Lion!!"};
	
	private String[] names = new String[]{"Jack:" ,"bearRu:" ,"albb:" ,"michruo:" ,"sol:" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
	    	super.onCreate(savedInstanceState);
	        
	        setContentView(R.layout.dynamic_issue);
	        
	        dynamic_issue_list = (ListView)findViewById(R.id.dynamic_issue_list);
	        
	        image = (ImageView)findViewById(R.id.picture);
	        
	        name = (TextView)findViewById(R.id.name);
	        
	        title = (TextView)findViewById(R.id.title);
	        
	        apply = (Button)findViewById(R.id.apply);
	        
	        like = (Button)findViewById(R.id.like);
	        
	        
	        
	        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
	        
	        for(int i=0;i<titles.length;i++)
	        {
	        	HashMap<String, Object> map = new HashMap<String, Object>();
	        	map.put("Name",names[i]);
	        	map.put("title", titles[i]);
	        	listItem.add(map);
	        }
	        
	        //context of items of the ListView
	        SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem, 
	                R.layout.dynamic_issue_item,    
	                new String[] {"Name","title"}, 
	                new int[] {R.id.name,R.id.title}
	            );
	       
	        dynamic_issue_list.setAdapter(listItemAdapter);
	            
	            
	}
	
	

}
