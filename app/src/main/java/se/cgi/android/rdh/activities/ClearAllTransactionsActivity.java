package se.cgi.android.rdh.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import se.cgi.android.rdh.R;
import se.cgi.android.rdh.data.DatabaseHelper;
import se.cgi.android.rdh.notifications.Sound;

public class ClearAllTransactionsActivity extends NonBcrActivity {
    //private static final String TAG = ClearAllTransactionsActivity.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private Sound sound;
    private long numberOfRecords = 0;
    private EditText et_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_all_transactions);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setTitle("Rensa Transaktioner");

        // Get singleton instance of sound
        sound = Sound.getInstance(getApplicationContext());

        et_count = findViewById(R.id.et_clear_all_transactions_count);
        showNumberOfTrans();
        et_count.requestFocus();
    }

    public void btn_clear_all_transactions(View v) {
        if (numberOfRecords > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ClearAllTransactionsActivity.this)
                    .setTitle("Rensa")
                    .setMessage("Vill du rensa bort alla transaktionsposter?")
                    .setCancelable(true)
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbHelper.deleteAllTrans();
                            sound.playSuccess();
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
        } else {
            Toast.makeText(getApplicationContext(), "Det finns inga poster att rensa!", Toast.LENGTH_LONG).show();
        }
    }

    private void showNumberOfTrans() {
        // Get singleton instance of database
        dbHelper = DatabaseHelper.getInstance(this);
        numberOfRecords = dbHelper.getTransCount();
        et_count.setText(String.valueOf(numberOfRecords));
    }
}