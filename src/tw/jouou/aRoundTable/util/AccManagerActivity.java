/*Sol:it seems no longer needed.*/
package tw.jouou.aRoundTable.util;

import tw.jouou.aRoundTable.R;
import tw.jouou.aRoundTable.util.DBUtils;
import tw.jouou.aRoundTable.bean.User;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class AccManagerActivity extends Activity {
	
	private DBUtils dbUtils;
	private List<User> users;
	private User user;
	private String token;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        	if (dbUtils == null) {
        		dbUtils = new DBUtils(this);
        	}
        	users = dbUtils.userDelegate.get();
        	if(!users.isEmpty()){
        		return;
        	}else{
            	Uri uri = Uri.parse("http://api.hime.loli.tw/login");
            	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            	startActivity(intent);
        	}
    }
	
	
	@Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
	 	Uri uri = intent.getData();
	 	token = uri.getQueryParameter("");
	    user = new User();
	    user.setToken(token);
		dbUtils.userDelegate.insert(user);
		dbUtils.close();
		Toast popup =  Toast.makeText(this,"認證完成，請按返回鍵回到程式", Toast.LENGTH_SHORT);
	    popup.show();
		Log.i("Sol","it's about to finish");
		//this.finish();
	}
	
	
	/*@Override
	 protected void onDestroy() {
	    super.onDestroy();
	    //Kill myself
	    android.os.Process.killProcess(android.os.Process.myPid());
	 }*/
}