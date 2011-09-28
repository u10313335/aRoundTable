package tw.jouou.aRoundTable.lite.lib;

import net.emome.hamiapps.sdk.LicenseService;
import net.emome.hamiapps.sdk.SDKService;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class Receiver extends BroadcastReceiver
{
	private static final String TAG;
	
	public static final String ACTION_DOWNLOAD_AM;
	public static final String ACTION_UPDATE_AM;
	public static final String ACTION_REMOTE_LICENSE_CHECK;
	
	private void cancelNotification(Context context)
	{
		Log.d(TAG, "cancelNotification()");
		
		NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);	
		notificationMgr.cancel(HamiService.NOTIFICATION_ID);
	}
	
	private void startUpdateAMActivity(Context context)
	{
		Log.d(TAG, "startUpdateAMActivity()");
		
		Intent i = SDKService.getUpdateAMIntent(context);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}

	private void startRemoteLicenseCheckActivity(Context context)
	{
		Log.d(TAG, "startRemoteLicenseCheckActivity()");
		
		Intent i = LicenseService.getRemoteLicenseCheckIntent(context);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
	    
	private void startDownloadAM(Context context)
	{
		Log.d(TAG, "startDownloadAM()");
		
		String url = SDKService.getAMDownloadURL(context);
       	Uri uri = Uri.parse(url);
    	Intent i = new Intent(Intent.ACTION_VIEW, uri);
		i.addCategory(Intent.CATEGORY_BROWSABLE);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "onReceive():action=" + intent.getAction());
		
		String action = intent.getAction();
		if(action.equals(ACTION_DOWNLOAD_AM))
		{	
			cancelNotification(context);
			startDownloadAM(context);
		}
		else if(action.equals(ACTION_UPDATE_AM))
		{
			cancelNotification(context);
			startUpdateAMActivity(context);
		}
		else if(action.equals(ACTION_REMOTE_LICENSE_CHECK))
		{
			cancelNotification(context);
			startRemoteLicenseCheckActivity(context);
		}
	}
	
	static
	{
		TAG = "Receiver";
		
		ACTION_DOWNLOAD_AM = "net.emome.hamiapps.sample01.ACTION_DOWNLOAD_AM";
		ACTION_UPDATE_AM = "net.emome.hamiapps.sample01.ACTION_UPDATE_AM"; 
		ACTION_REMOTE_LICENSE_CHECK = "net.emome.hamiapps.sample01.ACTION_REMOTE_LICENSE_CHECK";
	}
}
