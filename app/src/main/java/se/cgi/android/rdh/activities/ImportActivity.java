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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import se.cgi.android.rdh.R;
import se.cgi.android.rdh.data.DatabaseHelper;
import se.cgi.android.rdh.models.WorkOrder;
import se.cgi.android.rdh.utils.Logger;

/***
 * ImportActivity - Activity class for importing data to the database.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class ImportActivity extends AppCompatActivity {
    private static final String TAG = ImportActivity.class.getSimpleName();
    private static final String[] files = {"ALLA", "ARBETSORDER"};
    private DatabaseHelper dbHelper;
    private TextInputLayout til_import_data;
    private AutoCompleteTextView actv_import_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setTitle("Importera Data");

        // Get singleton instance of database
        dbHelper = DatabaseHelper.getInstance(this);

        til_import_data = findViewById(R.id.til_import_data);
        actv_import_data = findViewById(R.id.actv_import_data);

        // Create an adapter for the dropdown list (AutoCompleteTextView)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ImportActivity.this,
                android.R.layout.simple_spinner_dropdown_item, files);

        actv_import_data.setAdapter(adapter);
        actv_import_data.setText(actv_import_data.getAdapter().getItem(0).toString());
        adapter.getFilter().filter(null);
        actv_import_data.dismissDropDown();

        til_import_data.requestFocus();
    }

    public void startImportButtonOnClick(View view) {
        if (dbHelper.getTransCount() > 0) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.transaction_exist), Toast.LENGTH_LONG).show();
            return;
        }
        showImportDialog();
    }

    private void showImportDialog() {
        String item = String.valueOf(actv_import_data.getText());
        String message;

        if (item.equals("ALLA")) {
            message = "Är du säker på att du vill läsa in all data?";
        } else {
            message = "Är du säker på att du vill läsa in " + item + " data?";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ImportActivity.this)
                .setTitle("Fråga")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        importFiles();
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

    private void importFiles() {
        String item = String.valueOf(actv_import_data.getText());
        String file = item + ".txt";

        if (item.equals("ALLA")) { // All files
            for (int i=1; i < files.length; i++) {
                file = files[i] + ".txt";
                item = files[i];
                if (!startImport(item, file)) {
                    return;
                }
            }
        } else { // Only one file
            startImport(item, file);
        }
    }


    private boolean startImport(String item, String file) {
        File dataFilesDir, dataFile;
        InputStream inStream;

        try {
            dataFilesDir = new File(ImportActivity.this.getExternalFilesDir(null), File.separator + getString(R.string.data_files_dir_name));
            if (!dataFilesDir.exists()) {
                if (!dataFilesDir.mkdirs()) {
                    Toast.makeText(getApplicationContext(), "Error: Katalogen för databasfiler kan ej skapas!", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, "Error: Katalogen för databasfiler kan ej skapas!");
                    return false;
                }
            }

            dataFile = new File(ImportActivity.this.getExternalFilesDir(null), File.separator + getString(R.string.data_files_dir_name) + File.separator + file);
            if (!dataFile.exists()) {
                Toast.makeText(getApplicationContext(), "Error: Filen " + file + " saknas i katalogen för databasfiler!", Toast.LENGTH_LONG).show();
                Logger.e(TAG, "Error: Filen " + file + " saknas i katalogen för databasfiler!");
                return false;
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error: " + e.getMessage());
            return false;
        }

        try {
            // Open file
            inStream = new FileInputStream(dataFile);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error: Fel vid öppning av filen " + file + "!", Toast.LENGTH_SHORT).show();
            Logger.e(TAG, "Error: Fel vid öppning av filen " + file + "!");
            return false;
        }

        // Check which file to import
        switch(item) {
            case "ARBETSORDER":
                importWorkOrderData(inStream);
                break;
        }

        return true;
    }

    private void importWorkOrderData(InputStream inStream) {
        String line;
        boolean error = false;

        // Delete all WorkOrder data
        dbHelper.deleteAllWorkOrder();

        Toast.makeText(getApplicationContext(), "Inläsning av ARBETSORDER startar...", Toast.LENGTH_SHORT).show();
        Logger.d(TAG, "Inläsning av ARBETSORDER startar...");

        dbHelper.beginTransaction();
        try {
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream, "ISO8859-1"));
            while ((line = buffer.readLine()) != null) {
                line = replaceCharacters(line);
                String[] columns = line.split("\t");
                if (columns.length != 2) {
                    Logger.e(TAG, "Hoppar över felaktig CSV rad!");
                    continue;
                }

                WorkOrder workOrder = new WorkOrder();

                try {
                    workOrder.setWorkOrderNo(columns[0]);
                    workOrder.setWorkOrderName(columns[1]);
                    dbHelper.createWorkOrder(workOrder);
                } catch(Exception e) {
                    Logger.e(TAG, e.getMessage());
                    error = true;
                    break;
                }
            }
        } catch (IOException e) {
            Logger.e(TAG, e.getMessage());
            error = true;
        }

        if (!error) {
            dbHelper.setTransactionSuccessful();
        }

        dbHelper.endTransaction();

        try {
            inStream.close();
        } catch (IOException e) {
            Logger.e(TAG, e.getMessage());
            error = true;
        }

        dbHelper.closeDB();

        if (!error) {
            Toast.makeText(getApplicationContext(), "Inläsning klar", Toast.LENGTH_SHORT).show();
            Logger.d(TAG, "Inläsning klar");
        } else {
            Toast.makeText(getApplicationContext(), "Error: Felaktig inläsning av ARBETSORDER!", Toast.LENGTH_SHORT).show();
            Logger.e(TAG, "Error: Felaktig inläsning av ARBETSORDER!");
        }
    }

    private String replaceCharacters(String line) {
        if (line != null) {
            line = line.replace('{', 'ä');
            line = line.replace('}', 'å');
            line = line.replace('|', 'ö');
            line = line.replace('[', 'Ä');
            line = line.replace(']', 'Å');
            line = line.replace('\\', 'Ö');
        }
        return line;
    }
}