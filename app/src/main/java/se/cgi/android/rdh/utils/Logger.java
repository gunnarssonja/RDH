package se.cgi.android.rdh.utils;

import android.util.Log;

import se.cgi.android.rdh.BuildConfig;

/***
 * Logger - Class with static methods for logging.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class Logger {

    // Debug message
    public static void d(String tag, String data){
        if(BuildConfig.DEBUG) {
            Log.d(tag, data);
        }
    }

    // Info message
    public static void i(String tag, String data){
        if(BuildConfig.DEBUG) {
            Log.i(tag, data);
        }
    }

    // Error message
    public static void e(String tag, String data){
        if(BuildConfig.DEBUG) {
            Log.e(tag, data);
        }
    }

    // Error message
    public static void e(String tag, String data, Throwable t){
        if(BuildConfig.DEBUG) {
            Log.e(tag, data, t);
        }
    }

    // Verbose message
    public static void v(String tag, String data){
        if(BuildConfig.DEBUG) {
            Log.v(tag, data);
        }
    }

    // Warning message
    public static void w(String tag, String data){
        if(BuildConfig.DEBUG) {
            Log.w(tag, data);
        }
    }

    // What a Terrible Failure message
    public static void wtf(String tag, String data){
        if(BuildConfig.DEBUG) {
            Log.wtf(tag, data);
        }
    }

    // Check if debug is enabled
    public static boolean isDebugEnabled(String tag) {
        return BuildConfig.DEBUG;
    }
}

