package se.cgi.android.rdh.activities;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

import se.cgi.android.rdh.BuildConfig;
import se.cgi.android.rdh.R;
import se.cgi.android.rdh.utils.AppPreference;
import se.cgi.android.rdh.utils.FileUtil;

/*
 * Application: RDH
 * Description: The RDH application for handheld computers is used for spare parts handling.
 * Note       : The application is developed for handheld computers from Zebra with Android 10.
 *
 * @author  Janne Gunnarsson CGI
 * @version 0.1
 * @since   2023-07-31
 */

/**
 * MainActivity - The main window in the RDH application.
 */
public class MainActivity extends NonBcrActivity {
    private AppPreference appPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setTitle("RDH - Huvudmeny");
        createAppDirs();

        // Get singleton instance of AppPreference
        appPreference = AppPreference.getInstance(this);

        // Create default values if preference file not exist
        if (!preferenceFileExist("AppPreference")) {
            setDefaultPreference(appPreference);
        }
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
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                storageTakeOutButtonOnClick(this.findViewById(android.R.id.content));
                return true;
            case KeyEvent.KEYCODE_2:
                sendButtonOnClick(this.findViewById(android.R.id.content));
                return true;
            case KeyEvent.KEYCODE_3:
                settingsButtonOnClick(this.findViewById(android.R.id.content));
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public void storageTakeOutButtonOnClick(View view) {
        Intent intent = new Intent(MainActivity.this, StorageTakeOutListActivity.class);
        startActivity(intent);
    }

    public void sendButtonOnClick(View view) {
        Intent intent = new Intent(MainActivity.this, SendActivity.class);
        startActivity(intent);
    }

    public void settingsButtonOnClick(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsMenuActivity.class);
        startActivity(intent);
    }

    private void createAppDirs() {
        if (!FileUtil.checkIfExternalStorageWritable(getApplicationContext())) {
            return;
        }
        FileUtil.createAppDirs(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String versionName = BuildConfig.VERSION_NAME;
        String buildVariant = "";
        Intent intent;

        if (BuildConfig.DEBUG) {
            buildVariant = "(Debug)";
        }

        if (item.getItemId() == R.id.settings) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if(item.getItemId() == R.id.help){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.hjalp_titel))
                    .setMessage(getString(R.string.hjalp_text))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {}
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else if(item.getItemId() == R.id.about){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.om_titel))
                    .setMessage(getString(R.string.app_version_text) + " " + versionName + " " + buildVariant + "\n\n" +
                            getString(R.string.om_text) + "\n\n ") //+
                    //getString(R.string.db_version_text) + " " + DatabaseHelper.DATABASE_VERSION)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {}
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *  Check if Preference file exist in /DATA/data/{application package}/shared_prefs
     * @param fileName preference filename
     */
    private boolean preferenceFileExist(String fileName) {
        File f = new File(getApplicationContext().getApplicationInfo().dataDir +
                "/shared_prefs/" + fileName + ".xml");
        return f.exists();
    }

    /**
     *  Sets some default preference values
     * @param appPreference preference filename
     */
    private void setDefaultPreference(AppPreference appPreference) {
        appPreference.saveString("Place", getString(R.string.Place));
        appPreference.saveBoolean("DataIdentifier", true);
    }
}