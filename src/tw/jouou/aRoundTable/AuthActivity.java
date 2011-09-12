package tw.jouou.aRoundTable;

import tw.jouou.aRoundTable.lib.ArtApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthActivity extends Activity {
	
	private SharedPreferences mPrefs;
	
	
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
						mPrefs = PreferenceManager.getDefaultSharedPreferences(AuthActivity.this);
				        mPrefs.edit().putString("TOKEN", token).commit();
				        mPrefs.edit().putBoolean("AUTHORIZED", true).commit();
						setResult(RESULT_OK);
						finish();
			        }else
			        	view.loadUrl(url);
			        return false; // then it is not handled by default action
			   }
			});
	}
}
