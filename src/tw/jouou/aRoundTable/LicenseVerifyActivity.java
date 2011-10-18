package tw.jouou.aRoundTable;

import net.fet.android.license.sdk.LicenseToolkit;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * S Mart (Chinese) --
 * 修改Android SDK 的Sample: LunarLander.
 * 加入了S Mart SDK的License Verification.
 * 建議參考 Developer Guide
 * 
 * 當App開啟時，會先開啟此Activity做License驗證
 * 若License驗證通過，再導向LunarLander這個Activity
 * 
 * !! 因此App需S Mart授權，如直接執行該APP將無法使用 !!
 * 
 * 
 * S Mart (English) --
 * Modify the Sample of Android SDK: LunarLander.
 * Add the License Verification of S Mart SDK.
 * Refer to the Developer Guide.
 * 
 * When the App start up, it will run this Activity for checking License Verification.
 * If pass , and then start the LunarLander.
 * 
 * !! The app cannot be used if running directly without license from S Mart. !!
 */
public class LicenseVerifyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // S Mart (Chinese) -- 進行License驗證
        // S Mart (English) -- Do the License Verification.
        boolean pass = LicenseToolkit.acquireClientAndLicense(this,
                LicenseToolkit.BLOCK_MODE);

        // S Mart (Chinese) -- 如果驗證通過，則開啟MainActivity，並關閉自己
        // S Mart (English) -- if pass the verification, start LunarLander and finish this activity.
        if (pass) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
