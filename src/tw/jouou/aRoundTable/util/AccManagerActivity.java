package tw.jouou.aRoundTable.util;

import tw.jouou.aRoundTable.R;
import tw.jouou.aRoundTable.util.DBUtils;
import tw.jouou.aRoundTable.bean.User;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class AccManagerActivity extends Activity {
	
	private DBUtils dbUtils;
	private static List<User> users;
	private User u;

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    	TextView text=(TextView)findViewById(R.string.hello);
        	if (dbUtils == null) {
        		dbUtils = new DBUtils(this);
        	}
        	users = dbUtils.userDelegate.get();
        	System.out.println(users.isEmpty());
        	if(!users.isEmpty()){
        		String result = users.get(0).getToken();
            	text.setText("userToken:"+result);
        	}else{
        		Toast popup =  Toast.makeText(AccManagerActivity.this, "資料庫內無帳號，導引至認證頁面", Toast.LENGTH_SHORT);
                popup.show();
            	Uri uri = Uri.parse("http://hime.loli.tw/login");
            	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            	startActivity(intent);
        }
    }
	
	
	@Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
		Uri uri = intent.getData();
		String result = uri.getQueryParameter("");
		Toast popup2 =  Toast.makeText(AccManagerActivity.this, "取得Token:"+result, Toast.LENGTH_SHORT);
	    popup2.show();
		u = new User();
		u.setToken(result);
		dbUtils = new DBUtils(this);
		dbUtils.userDelegate.insert(u);
		dbUtils.close();
	}
	
}
