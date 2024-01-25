package se.cgi.android.rdh.activities;

import static se.cgi.android.rdh.data.DatabaseHelper.NEW_RECORD;

import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import se.cgi.android.rdh.R;
import se.cgi.android.rdh.data.DatabaseHelper;
import se.cgi.android.rdh.models.Trans;
import se.cgi.android.rdh.models.WorkOrder;

/***
 * TransactionAddEditActivity - Activity class for add, update and delete of transactions.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class TransactionAddEditActivity extends NonBcrActivity {
    private static final String TAG = TransactionAddEditActivity.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private int id, workOrderId;
    private EditText etTransType, etWorkOrderNo, etArticleNo, etQuantity, etDateTime;
    private TextInputLayout tilTransType, tilWorkOrderNo, tilArticleNo, tilQuantity, tilDateTime;
    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button btnSave;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_add_edit);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setTitle("Hantera Transaktion");

        // Get singleton instance of database
        dbHelper = DatabaseHelper.getInstance(this);

        etTransType = findViewById(R.id.et_trans_type);
        etWorkOrderNo = findViewById(R.id.et_trans_work_order_no);
        etArticleNo = findViewById(R.id.et_trans_article_no);
        etQuantity = findViewById(R.id.et_trans_quantity);
        etDateTime = findViewById(R.id.et_trans_date_time);

        tilTransType = findViewById(R.id.til_trans_type);
        tilWorkOrderNo = findViewById(R.id.til_trans_work_order_no);
        tilArticleNo = findViewById(R.id.til_trans_article_no);
        tilQuantity = findViewById(R.id.til_trans_quantity);
        tilDateTime = findViewById(R.id.til_trans_date_time);

        btnSave = findViewById(R.id.btn_save);
        Button btn_delete = findViewById(R.id.btn_delete);

        getAndSetIntentData();

        //TODO: Vilka skall kunna ändras
        //etWorkOrderNo.addTextChangedListener(new TransactionAddEditActivity.ValidateTextWatcher(etWorkOrderNo));
        //etArticleNo.addTextChangedListener(new TransactionAddEditActivity.ValidateTextWatcher(etArticleNo));

        if ("edit".equals(action)) {
            etTransType.setEnabled(false);
            etWorkOrderNo.setEnabled(false);
            etArticleNo.setEnabled(false);
            etQuantity.setEnabled(false);
            etDateTime.setEnabled(false);
            btnSave.setEnabled(false);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("edit".equals(action)) { // Update record
                    if (!validateMandatoryFields()) {
                        return;
                    }
                    if (!updateTrans()) {
                        return;
                    }
                }
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TransactionAddEditActivity.this)
                        .setTitle("Ta bort")
                        .setMessage("Vill du ta bort posten?")
                        .setCancelable(true)
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper.deleteTransById(id);
                                Intent intent = new Intent(TransactionAddEditActivity.this, TransactionListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
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
        });
    }

    void getAndSetIntentData() {
        // Edit - from click on work order in list view
        if (getIntent().hasExtra("trans_id")){
            action = getIntent().getStringExtra("action");
            id = getIntent().getIntExtra("trans_id", 0) ;
            etTransType.setText(getIntent().getStringExtra("trans_type"));
            workOrderId = getIntent().getIntExtra("trans_work_order_id", 0);
            etWorkOrderNo.setText(getIntent().getStringExtra("trans_work_order_no"));
            etArticleNo.setText(getIntent().getStringExtra("trans_article_no"));
            etQuantity.setText(String.valueOf(getIntent().getIntExtra("trans_quantity", 0)));
            etDateTime.setText(getIntent().getStringExtra("trans_date_time"));
            etQuantity.requestFocus();
        }
    }

    // Live validation of text fields
    private class ValidateTextWatcher implements TextWatcher {
        private final View view;
        private ValidateTextWatcher(View view) {
            this.view = view;
        }
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            switch(view.getId()) {
                case R.id.et_work_order_no:
                    validateWorkOrderNo();
                    break;
                case R.id.et_work_order_name:
                    validateArticleNo();
                    break;
            }
        }
    }

    private boolean validateWorkOrderNo() {
        if (TextUtils.isEmpty(String.valueOf(etWorkOrderNo.getText()).trim())) {
            tilWorkOrderNo.setError(getResources().getString(R.string.work_order_no_missing));
            tilWorkOrderNo.requestFocus();
            return false;
        } else if (etWorkOrderNo.length() > WorkOrder.MaxWorkOrderNoLength) {
            tilWorkOrderNo.setError(getResources().getString(R.string.work_order_no_to_large));
            tilWorkOrderNo.requestFocus();
            return false;
        } else {
            tilWorkOrderNo.setError(null);
        }
        return true;
    }

    private boolean validateArticleNo() {
        if (TextUtils.isEmpty(String.valueOf(etArticleNo.getText()).trim())) {
            tilArticleNo.setError(getResources().getString(R.string.article_no_missing));
            tilArticleNo.requestFocus();
            return false;
        } else if (etArticleNo.length() > Trans.MaxArticleNoLength) {
            tilArticleNo.setError(getResources().getString(R.string.article_no_to_large));
            tilArticleNo.requestFocus();
            return false;
        } else {
            tilArticleNo.setError(null);
        }
        return true;
    }

    private boolean validateMandatoryFields() {
        if (!validateWorkOrderNo()) {
            return false;
        }
        if (!validateArticleNo()) {
            return false;
        }
        return true;
    }

    private boolean updateTrans() {
        try {
            Trans trans = new Trans();
            trans.setId(id);
            trans.setTransType(String.valueOf(etTransType.getText()));
            trans.setWorkOrderId(workOrderId);
            trans.setWorkOrderNo(String.valueOf(etWorkOrderNo.getText()));
            trans.setArticleNo(String.valueOf(etArticleNo.getText()));
            trans.setQuantity(Integer.parseInt(String.valueOf(etQuantity.getText())));
            trans.setDateTime(String.valueOf(etDateTime.getText()));
            dbHelper.updateTrans(trans);
            Intent intent = new Intent(TransactionAddEditActivity.this, TransactionListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch(Exception e) {
            handleException(e);
        }
        return true;
    }

    private void handleException(Exception e) {
        if (Objects.requireNonNull(e.getMessage()).contains("Transaktionstyp")) {
            tilTransType.setError(e.getMessage());
            tilTransType.requestFocus();
        } else if (Objects.requireNonNull(e.getMessage()).contains("Arbetsorder")) {
            tilWorkOrderNo.setError(e.getMessage());
            tilWorkOrderNo.requestFocus();
        } else if (Objects.requireNonNull(e.getMessage()).contains("Fbet")){
            tilArticleNo.setError(e.getMessage());
            tilArticleNo.requestFocus();
        } else {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}