package se.cgi.android.rdh.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import se.cgi.android.rdh.R;
import se.cgi.android.rdh.data.DatabaseHelper;
import se.cgi.android.rdh.models.Trans;
import se.cgi.android.rdh.models.WorkOrder;
import se.cgi.android.rdh.notifications.Sound;
import se.cgi.android.rdh.utils.FileUtil;
import se.cgi.android.rdh.utils.Logger;

public class SendActivity extends AppCompatActivity {
    private static final String TAG = SendActivity.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private Sound sound;
    private long numberOfTransRecords = 0;
    private File outDir, excelFile;
    private Button btnCreateFile;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setTitle("Sänd");
        dbHelper = DatabaseHelper.getInstance(this);
        sound = Sound.getInstance(getApplicationContext());
        tvStatus = findViewById(R.id.tv_send_status);
        btnCreateFile = findViewById(R.id.btn_create_excel_file);
        showNumberOfTrans();
        showNumberOfCreatedRecords(0);
        tvStatus.setText("Sätt handdatorn i dockan och tryck på sänd\n");
        btnCreateFile.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.send_menu, menu);
        return true;
    }

    public void btn_create_excel_file(View v) {
        if (numberOfTransRecords > 0) {
            try {
                // Check if external memory is available for writing
                if (!FileUtil.checkIfExternalStorageWritable(getApplicationContext())) {
                    return;
                }

                // Check if OUT-directory exist
                outDir = new File(SendActivity.this.getExternalFilesDir(null), File.separator + getString(R.string.rdh_out_dir_name));
                if (!outDir.exists()) {
                    if (!outDir.mkdirs()) {
                        Toast.makeText(getApplicationContext(), "Error: Katalogen för Excel-filer kan ej skapas!", Toast.LENGTH_LONG).show();
                        Logger.e(TAG, "Error: Katalogen för Excel-filer kan ej skapas!");
                        return;
                    }
                }

                // Check if Excel-file already exist
                excelFile = new File(outDir.toString() + File.separator + getString(R.string.excel_out_file_name));
                if (excelFile.exists()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this)
                            .setTitle("Skapa fil")
                            .setMessage("Filen finns redan, vill du skriva över den?")
                            .setCancelable(true)
                            .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!excelFile.delete()) {
                                        Toast.makeText(getApplicationContext(), "Error: Excel-filen kan ej raderas!", Toast.LENGTH_LONG).show();
                                        Logger.e(TAG, "Error: Excel-filen kan ej raderas!");
                                        return;
                                    }
                                    btnCreateFile.setEnabled(false);
                                    createExcelFile();
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
                } else {
                    btnCreateFile.setEnabled(false);
                    createExcelFile();
                }
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Logger.e(TAG, "Error: " +  e.getMessage());
            }
        } else {
            Toast.makeText(getApplicationContext(), "Det finns inga poster att skriva till filen!", Toast.LENGTH_LONG).show();
        }
    }

    private void createExcelFile() {
        try {
            List<Trans> transList;
            transList = dbHelper.getAllTransListByWorkOrderNo();
            FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
            int numberOfExcelRecords = 0;

            HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
            HSSFSheet hssfSheet = hssfWorkbook.createSheet("Förrådsuttag");

            // Insert all transactions
            numberOfExcelRecords = insertAllTransRecords(hssfWorkbook, hssfSheet, transList);

            excelFile.createNewFile();
            fileOutputStream = new FileOutputStream(excelFile);
            hssfWorkbook.write(fileOutputStream);

            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }

            showNumberOfCreatedRecords(numberOfExcelRecords);
            tvStatus.append("Filen är skapad\n");

            sound.playSuccess();

            // Ask if transactions shall be cleared
            clear_all_transactions();

            btnCreateFile.setEnabled(true);

        } catch(Exception e) {
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Logger.e(TAG, "Error: " +  e.getMessage());
        }
    }

    private int insertAllTransRecords( HSSFWorkbook hssfWorkbook, HSSFSheet hssfSheet, List<Trans> transList)  throws IOException {
        HSSFRow hssfRow;
        HSSFCell hssfCell;
        HSSFFont font = hssfWorkbook.createFont();
        HSSFCellStyle style = hssfWorkbook.createCellStyle();
        String oldWorkOrderNo;
        int rowNumber = 1, transRecords = 0;

        hssfSheet.setColumnWidth(0, WorkOrder.MaxWorkOrderNoLength * 256);
        hssfSheet.setColumnWidth(1, Trans.MaxArticleNoLength * 256);
        hssfSheet.setColumnWidth(2, Trans.MaxQuantityLength * 256);

        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style.setFont(font);

        hssfRow = hssfSheet.createRow(0);
        hssfCell = hssfRow.createCell(0);
        hssfCell.setCellValue("Order");
        hssfCell.setCellStyle(style);
        hssfCell = hssfRow.createCell(1);
        hssfCell.setCellValue("Fbet");
        hssfCell.setCellStyle(style);
        hssfCell = hssfRow.createCell(2);
        hssfCell.setCellValue("Antal");
        hssfCell.setCellStyle(style);
        oldWorkOrderNo = "";

        for(Trans t: transList) {
            hssfRow = hssfSheet.createRow(rowNumber);
            hssfCell = hssfRow.createCell(0);

            if (!oldWorkOrderNo.equals(t.getWorkOrderNo())) {
                hssfCell.setCellValue(t.getWorkOrderNo());
                oldWorkOrderNo = t.getWorkOrderNo();
            }
            else {
                hssfCell.setCellValue("");
            }

            hssfCell = hssfRow.createCell(1);
            hssfCell.setCellValue(t.getArticleNo());
            hssfCell = hssfRow.createCell(2);
            hssfCell.setCellValue(t.getQuantity());
            rowNumber++;
            transRecords++;
        }

        return transRecords;
    }

    public void clear_all_transactions() {
        if (numberOfTransRecords > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this)
                    .setTitle("Filen är skapad")
                    .setMessage("Vill du rensa bort alla transaktionsposter?\n\nSvara JA om du skall skicka filen till PC och NEJ om du skall fortsätta samla data.")
                    .setCancelable(true)
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbHelper.deleteAllTrans();
                            showNumberOfTrans();
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
    }

    private void showNumberOfTrans() {
        numberOfTransRecords = dbHelper.getTransCount();
        EditText et_send_transactions_count = findViewById(R.id.et_send_transactions_count);
        et_send_transactions_count.setText(String.valueOf(numberOfTransRecords));
    }

    private void showNumberOfCreatedRecords(int numberOfCreatedRecords) {
        EditText et_send_excel_count = findViewById(R.id.et_send_excel_count);
        et_send_excel_count.setText(String.valueOf(numberOfCreatedRecords));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.remove_excel_file) {
            outDir = new File(SendActivity.this.getExternalFilesDir(null), File.separator + getString(R.string.rdh_out_dir_name));
            if (outDir.exists()) {
                // Check if Excel-file exist
                excelFile = new File(outDir.toString() + File.separator + getString(R.string.excel_out_file_name));
                if (excelFile.exists()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this)
                            .setTitle("Ta bort Excel-fil")
                            .setMessage("Är du säker på att du vill ta bort filen?")
                            .setCancelable(true)
                            .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!excelFile.delete()) {
                                        Toast.makeText(getApplicationContext(), "Error: Excel-filen kan ej raderas!", Toast.LENGTH_LONG).show();
                                        Logger.e(TAG, "Error: Excel-filen kan ej raderas!");
                                    } else {
                                        Toast.makeText(getBaseContext(), "Filen är borttagen", Toast.LENGTH_LONG).show();
                                    }
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
                } else {
                    Toast.makeText(getBaseContext(), "Det finns ingen fil att ta bort", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getBaseContext(), "Det finns ingen utkatalog för Excel-filer", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}