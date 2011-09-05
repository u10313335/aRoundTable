package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthActivity extends Activity {

	@Override
	protected void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		 WebView webview = new WebView(this);
		 setContentView(webview);
		 webview.loadUrl(ArtApi.getLoginUrl());
		 webview.setWebViewClient(new WebViewClient() {
			    public boolean shouldOverrideUrlLoading(WebView view, String url){
			        if(url.startsWith("art://done")){
			        	Uri uri = Uri.parse(url);
					 	String token = uri.getQueryParameter("");
					    User user = new User(token);
					    DBUtils	dbUtils = new DBUtils(AuthActivity.this);
						dbUtils.userDelegate.insert(user);
						dbUtils.close();
						setResult(RESULT_OK);
						finish();
			        }else
			        	view.loadUrl(url);
			        return false; // then it is not handled by default action
			   }
			});
	}
}
