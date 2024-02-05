package se.cgi.android.rdh.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

import se.cgi.android.rdh.R;

/***
 * FileUtil - Class with static methods for file utils.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public final class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();

    // Checks if external storage is writable
    public static boolean checkIfExternalStorageWritable(Context context) {
        if (isExternalStorageWritable() == false) {
            Toast.makeText(context, "Error: Externt minne saknas eller ej skrivbar", Toast.LENGTH_LONG).show();
            Logger.e(TAG, "Error: Externt minne saknas eller ej skrivbar");
            return false;
        }
        return true;
    }

    // Checks if a volume containing external storage is available for read and write.
    public static boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // Creates application directories
    public static void createAppDirs(Context context) {
        File outDir, inDir, dataFilesDir, formatFilesDir;

        dataFilesDir = new File(context.getExternalFilesDir(null), File.separator + context.getString(R.string.data_files_dir_name));
        if (!dataFilesDir.exists()) {
            if (!dataFilesDir.mkdirs()) {
                Toast.makeText(context, "Error: Katalogen för databasfiler kan ej skapas", Toast.LENGTH_LONG).show();
                Logger.e(TAG, "Error: Katalogen för databasfiler kan ej skapas");
            }
        }

        outDir = new File(context.getExternalFilesDir(null), File.separator + context.getString(R.string.rdh_out_dir_name));
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                Toast.makeText(context, "Error: Katalogen för UT-filer kan ej skapas", Toast.LENGTH_LONG).show();
                Logger.e(TAG, "Error: Katalogen för UT-filer kan ej skapas");
            }
        }

        inDir = new File(context.getExternalFilesDir(null), File.separator + context.getString(R.string.rdh_in_dir_name));
        if (!inDir.exists()) {
            if (!inDir.mkdirs()) {
                Toast.makeText(context, "Error: Katalogen för IN-filer kan ej skapas", Toast.LENGTH_LONG).show();
                Logger.e(TAG, "Error: Katalogen för UT-filer kan ej skapas");
            }
        }
    }
}
