package se.cgi.android.rdh.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import se.cgi.android.rdh.R;
import se.cgi.android.rdh.data.DatabaseHelper;
import se.cgi.android.rdh.models.WorkOrder;
import se.cgi.android.rdh.utils.Logger;
import se.cgi.android.rdh.utils.Utils;

/***
 * ExportActivity - Activity class for exporting data from the database.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class ExportActivity extends AppCompatActivity {
    private static final String TAG = ExportActivity.class.getSimpleName();
    private static final String[] files = {"ALLA", "ARBETSORDER"};
    private DatabaseHelper dbHelper;
    private final char separator = '\t';
    private TextInputLayout til_export_data;
    private AutoCompleteTextView actv_export_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setTitle("Exportera Data");

        // Get singleton instance of database
        dbHelper = DatabaseHelper.getInstance(this);

        til_export_data = findViewById(R.id.til_export_data);
        actv_export_data = findViewById(R.id.actv_export_data);

        // Create an adapter for the dropdown list (AutoCompleteTextView)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ExportActivity.this,
                android.R.layout.simple_spinner_dropdown_item, files);
        actv_export_data.setAdapter(adapter);
        actv_export_data.setText(actv_export_data.getAdapter().getItem(0).toString());
        adapter.getFilter().filter(null);
        actv_export_data.dismissDropDown();

        til_export_data.requestFocus();
    }

    public void startExportButtonOnClick(View view) {
        showExportDialog();
    }

    private void showExportDialog() {
        String item = String.valueOf(actv_export_data.getText());
        String message;

        if (item.equals("ALLA")) {
            message = "Är du säker på att du vill exportera all data?";
        } else {
            message = "Är du säker på att du vill exportera " + item + " data?";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ExportActivity.this)
                .setTitle("Fråga")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        exportFiles();
                    }
                })
                .setNegativeButton("Nej", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void exportFiles() {
        String item = String.valueOf(actv_export_data.getText());
        String file = item + ".txt";

        if (item.equals("ALLA")) { // All files
            for (int i=1; i < files.length; i++) {
                file = files[i] + ".txt";
                item = files[i];
                if (!startExport(item, file)) {
                    return;
                }
            }
        } else { // Only one file
            startExport(item, file);
        }
    }

    private boolean startExport(String item, String file) {
        File dataFilesDir, dataFile, dataFileOld;
        OutputStream outputStream;

        try {
            // Check if directory with data files exist
            dataFilesDir = new File(ExportActivity.this.getExternalFilesDir(null), File.separator + getString(R.string.data_files_dir_name));
            if (!dataFilesDir.exists()) {
                if (!dataFilesDir.mkdirs()) {
                    Toast.makeText(getApplicationContext(), "Error: Katalogen för databasfiler kan ej skapas", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, "Error: Katalogen för databasfiler kan ej skapas");
                    return false;
                }
            }

            // Check if file already exist, if exist rename it to OLD_file
            dataFile = new File(ExportActivity.this.getExternalFilesDir(null), File.separator + getString(R.string.data_files_dir_name) + File.separator + file);
            if (dataFile.exists()) {
                Toast.makeText(getApplicationContext(), "Filen " + file + " finns redan, byter namn till OLD_" + file, Toast.LENGTH_LONG).show();
                dataFileOld = new File(ExportActivity.this.getExternalFilesDir(null), File.separator + getString(R.string.data_files_dir_name) + File.separator + "OLD_" + file);
                if (dataFileOld.exists()) {
                    if (!dataFileOld.delete()) {
                        Toast.makeText(getApplicationContext(), "Error: Kan ej radera filen OLD_" + file, Toast.LENGTH_LONG).show();
                        Logger.e(TAG, "Error: Kan ej radera filen OLD_" + file);
                        return false;
                    }
                }
                if (!dataFile.renameTo(dataFileOld)) {
                    Toast.makeText(getApplicationContext(), "Error: Kan ej byta namn på filen " + file, Toast.LENGTH_LONG).show();
                    Logger.e(TAG, "Error: Kan ej byta namn på filen " + file);
                    return false;
                }
            }
            if (!dataFile.createNewFile()) {
                Toast.makeText(getApplicationContext(), "Error: Kan ej skapa filen " + file, Toast.LENGTH_LONG).show();
                Logger.e(TAG, "Error: Kan ej skapa filen " + file);
                return false;
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error: " + e.getMessage());
            return false;
        }

        try {
            // Open file
            outputStream = new FileOutputStream(dataFile);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error: Fel vid öppning av filen " + file, Toast.LENGTH_SHORT).show();
            Logger.e(TAG, "Error: Fel vid öppning av filen " + file);
            return false;
        }

        // Check which table to export
        switch(item) {
            case "ARBETSORDER":
                exportWorkOrderData(outputStream);
                break;

        }

        return true;
    }

    private void exportWorkOrderData(OutputStream outputStream) {
        List<WorkOrder> workOrderList;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        boolean error = false;

        workOrderList = dbHelper.getAllWorkOrderList();

        Toast.makeText(getApplicationContext(), "Export av ARBETSORDER startar...", Toast.LENGTH_SHORT).show();
        Logger.d(TAG, "Export av ARBETSORDER startar...");

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, "ISO8859-1"));
            for (WorkOrder workOrder : workOrderList) {
                bw.append(Utils.rightPadSpaces(workOrder.getWorkOrderNo(), WorkOrder.MaxWorkOrderNoLength));
                bw.append(separator);
                bw.append(Utils.rightPadSpaces(workOrder.getWorkOrderName(), WorkOrder.MaxWorkOrderNameLength));
                bw.append("\r\n");
            }
            bw.close();
            outputStream.close();
        } catch (IOException e) {
            Logger.e(TAG, e.getMessage());
            error = true;
        }

        dbHelper.closeDB();

        if (!error) {
            Toast.makeText(getApplicationContext(), "Export klar", Toast.LENGTH_SHORT).show();
            Logger.d(TAG, "Export klar");
        } else {
            Toast.makeText(getApplicationContext(), "Error: Fel vid export av ARBETSORDER", Toast.LENGTH_SHORT).show();
            Logger.e(TAG, "Error: Fel vid export av ARBETSORDER");
        }
    }
}