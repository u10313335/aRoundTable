package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.lib.ArtApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class AuthActivity extends Activity {
	
	private SharedPreferences mPrefs;
	
	
	@Override
	protected void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Toast.makeText(this, R.string.welcome_message, Toast.LENGTH_SHORT).show();
		 WebView webview = new WebView(this);
		 setContentView(webview);
		 webview.loadUrl(ArtApi.getLoginUrl());
		 webview.setWebViewClient(new WebViewClient() {
			    public boolean shouldOverrideUrlLoading(WebView view, String url){
			        if(url.startsWith("art://done")){
			        	Uri uri = Uri.parse(url);
						mPrefs = PreferenceManager.getDefaultSharedPreferences(AuthActivity.this);
						
						//TODO: Make token keys constants
				        mPrefs.edit()
				        	.putString("TOKEN", uri.getQueryParameter("token"))
				        	.putInt("UID", Integer.parseInt(uri.getQueryParameter("uid")))
				        	.commit();
				        
						setResult(RESULT_OK);
						finish();
			        }else
			        	view.loadUrl(url);
			        return false; // then it is not handled by default action
			   }
			});
	}
}
