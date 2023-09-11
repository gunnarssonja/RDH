package se.cgi.android.rdh.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import se.cgi.android.rdh.R;
import se.cgi.android.rdh.utils.Logger;

/***
 *  BcrActivity - Barcode and RFID handling
 *
 *  @author  Janne Gunnarsson CGI
 *
 */
public abstract class BcrActivity extends BaseActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    protected String scannedData;
    protected String decodedSource;

    BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Bundle b = intent.getExtras();

            //  This is useful for debugging to verify the format of received intents from DataWedge
            //for (String key : b.keySet())
            //{
            //    Logger.v(LOG_TAG, key);
            //}

            if (action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
                //  Received a barcode scan
                try {
                    scannedData = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
                    decodedSource = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_source));
                    switch (decodedSource) {
                        case "scanner":
                            handleScannedData();
                            break;
                        case "rfid":
                            //handleRfidData();
                            break;
                    }
                } catch (Exception e) {
                    Logger.e(TAG, e.getMessage());
                }
            }
        }
    };

    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        registerReceiver(myBroadcastReceiver, filter);
    }

    public void onStop() {
        super.onStop();
        unregisterReceiver(myBroadcastReceiver);
    }

    abstract void handleScannedData();
    //abstract void handleRfidData();
}
