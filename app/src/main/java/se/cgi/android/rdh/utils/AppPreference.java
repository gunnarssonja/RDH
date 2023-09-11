package se.cgi.android.rdh.utils;

import android.content.Context;
import android.content.SharedPreferences;

/***
 * AppPreference - Class for application preference handling.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class AppPreference {
    private static final String TAG = AppPreference.class.getSimpleName();
    private static AppPreference sInstance;
    private SharedPreferences sharedPreferences;

    private AppPreference(Context context) {
        sharedPreferences = context.getSharedPreferences("AppPreference",Context.MODE_PRIVATE);
    }

    public static synchronized AppPreference getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AppPreference(context.getApplicationContext());
        }
        return sInstance;
    }

    public void saveString(String key, String value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }

    public void saveBoolean(String key, Boolean value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
    }

    public String getString(String key) {
        if (sharedPreferences!= null) {
            return sharedPreferences.getString(key, "");
        }
        return "";
    }

    public Boolean getBoolean(String key) {
        if (sharedPreferences!= null) {
            return sharedPreferences.getBoolean(key, false);
        }
        return false;
    }
}

