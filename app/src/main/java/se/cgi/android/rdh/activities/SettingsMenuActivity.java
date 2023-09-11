package se.cgi.android.rdh.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import se.cgi.android.rdh.R;

/**
 * SettingsMenuActivity - Activity class for settings menu.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class SettingsMenuActivity extends NonBcrActivity {

    //private static final String TAG = SettingsMenuActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setTitle("Inställningar");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set focus to current view instead of button
        View current = this.findViewById(android.R.id.content);
        if (current != null) {
            current.setFocusable(true);
            current.requestFocus();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                showWorkOrderListButtonOnClick(this.findViewById(android.R.id.content));
                return true;
            case KeyEvent.KEYCODE_2:
                importDataButtonOnClick(this.findViewById(android.R.id.content));
                return true;
            case KeyEvent.KEYCODE_3:
                exportDataButtonOnClick(this.findViewById(android.R.id.content));
                return true;
            case KeyEvent.KEYCODE_4:
                showTransactionsButtonOnClick(this.findViewById(android.R.id.content));
                return true;
            case KeyEvent.KEYCODE_5:
                clearAllTransactionsButtonOnClick(this.findViewById(android.R.id.content));
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public void showWorkOrderListButtonOnClick(View view) {
        Intent intent = new Intent(SettingsMenuActivity.this, WorkOrderListActivity.class);
        startActivity(intent);
    }

    public void importDataButtonOnClick(View view) {
        Intent intent = new Intent(SettingsMenuActivity.this, ImportActivity.class);
        startActivity(intent);
    }

    public void exportDataButtonOnClick(View view) {
       Intent intent = new Intent(SettingsMenuActivity.this, ExportActivity.class);
       startActivity(intent);
    }

    public void showTransactionsButtonOnClick(View view) {
        Intent intent = new Intent(SettingsMenuActivity.this, TransactionListActivity.class);
        startActivity(intent);
    }

    public void clearAllTransactionsButtonOnClick(View view) {
        Intent intent = new Intent(SettingsMenuActivity.this, ClearAllTransactionsActivity.class);
        startActivity(intent);
    }
}