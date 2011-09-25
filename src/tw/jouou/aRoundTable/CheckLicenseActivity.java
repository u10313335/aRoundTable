package tw.jouou.aRoundTable;

import net.emome.hamiapps.sdk.ForwardActivity;

public class CheckLicenseActivity extends ForwardActivity
{
	@SuppressWarnings("unchecked")
	@Override
	public Class getTargetActivity() 
	{
		return MainActivity.class;
	}
	
	@Override
	public boolean passOnUnavailableDataNetwork()
	{
		return true;
	}
}
