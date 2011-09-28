package tw.jouou.aRoundTable.lite.lib;

import tw.jouou.aRoundTable.lite.R;
import net.emome.hamiapps.sdk.LicenseService;
import net.emome.hamiapps.sdk.exception.AMNeedUpdateException;
import net.emome.hamiapps.sdk.exception.AMNotFoundException;
import net.emome.hamiapps.sdk.exception.NoIMEIException;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HamiService extends IntentService
{
	private static final String TAG;
	
	public static final int NOTIFICATION_ID = 1;
	
	private LicenseService mLicenseService;
	
	private void needRemoteLicenseCheck()
    {	
		Log.d(TAG, "needRemoteLicenseCheck()");
		
		NotificationManager notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);	
       	Notification n = new Notification();
       	n.icon = android.R.drawable.stat_sys_warning;
       	
       	Intent intent = new Intent(Receiver.ACTION_REMOTE_LICENSE_CHECK);
        intent.setClassName(getPackageName(), Receiver.class.getName());
        
        String title = getString(R.string.app_name);
       	String caption = getString(R.string.notify_remote_license_cehck);

       	n.setLatestEventInfo(this, title, caption, PendingIntent.getBroadcast(this, 0, intent, 0));
		n.when = System.currentTimeMillis();
       	notificationMgr.notify(NOTIFICATION_ID, n);
    }
    
	private void needUpdateAM()
	{
		Log.d(TAG, "needUpdateAM()");
		
		NotificationManager notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);	
       	Notification n = new Notification();
       	n.icon = android.R.drawable.stat_sys_warning;
       	
       	Intent intent = new Intent(Receiver.ACTION_UPDATE_AM);
        intent.setClassName(getPackageName(), Receiver.class.getName());
        
       	String title = getString(R.string.app_name);
       	String caption = getString(R.string.notify_update_am);
       	
       	n.setLatestEventInfo(this, title, caption, PendingIntent.getBroadcast(this, 0, intent, 0));
		n.when = System.currentTimeMillis();
       	notificationMgr.notify(NOTIFICATION_ID, n);
    }
	
	private void needDownloadAM()
	{
		Log.d(TAG, "needDownloadAM()");
		
		NotificationManager notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);	
       	Notification n = new Notification();
       	n.icon = android.R.drawable.stat_sys_warning;
       	
       	Intent intent = new Intent(Receiver.ACTION_DOWNLOAD_AM);
        intent.setClassName(getPackageName(), Receiver.class.getName());
       	
       	String title = getString(R.string.app_name);
       	String caption = getString(R.string.notify_download_am);
       	
		n.setLatestEventInfo(this, title, caption, PendingIntent.getBroadcast(this, 0, intent, 0));
		n.when = System.currentTimeMillis();
       	notificationMgr.notify(NOTIFICATION_ID, n);
	}
	
	public HamiService() 
	{
		super(TAG);
	}
	
	@Override
	public void onCreate()
	{
		Log.d(TAG, "onCreate()");
		
		mLicenseService = new LicenseService(this.getApplicationContext());
		
        super.onCreate();
    }

	@Override
	public void onDestroy()
	{
		Log.d(TAG, "onDestroy()");
		
		mLicenseService.destroy();
		
		super.onDestroy();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) 
	{
		Log.d(TAG, "onHandleIntent()");
		
		NotificationManager notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);	
		notificationMgr.cancel(NOTIFICATION_ID);
		
		try 
		{
			boolean result = mLicenseService.hasLocalLicense();
			if(result == false)
			{
				needRemoteLicenseCheck();
			}
			else
			{
				//TODO: do work.
			}
		} 
		catch(AMNotFoundException e) 
		{
			needDownloadAM();
		} 
		catch(AMNeedUpdateException e)
		{
			needUpdateAM();
		} 
		catch(NoIMEIException e)
		{
		}
	}
	
	static
	{
		TAG = "SampleService";
	}
}

        