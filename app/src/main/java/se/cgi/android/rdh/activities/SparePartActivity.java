package se.cgi.android.rdh.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NavUtils;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import se.cgi.android.rdh.R;
import se.cgi.android.rdh.data.DatabaseHelper;
import se.cgi.android.rdh.models.Trans;
import se.cgi.android.rdh.models.TransType;
import se.cgi.android.rdh.notifications.Sound;
import se.cgi.android.rdh.utils.AppPreference;
import se.cgi.android.rdh.utils.BcrDataHelper;
import se.cgi.android.rdh.utils.Logger;
import se.cgi.android.rdh.utils.Utils;

/***
 * SparePartActivity - Activity class for spare part out take.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class SparePartActivity extends BcrActivity {
    private static final String TAG = SparePartActivity.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private AppPreference appPreference;
    private Sound sound;
    private String action;
    private String workOrderId, workOrderNo, workOrderName;
    Boolean checkDataIdentifier;
    private Button btnSave;
    private EditText etWorkOrderName, etArticleNo, etQuantity;
    private TextInputLayout tilWorkOrderName, tilArticleNo, tilQuantity;
    private TextView tvSavedRecords;
    private ListView lv_status_saved_records;
    private List<Trans> transList = new ArrayList<>();
    private ArrayList<String> listData = new ArrayList<>();
    private ArrayAdapter<String> listAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spare_part);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setTitle("Uttag Reservdelar");

        getAndSetIntentData();

        // Get singleton instance of database
        dbHelper = DatabaseHelper.getInstance(this);

        // Get singleton instance of sound
        sound = Sound.getInstance(getApplicationContext());

        // Get singleton instance of AppPreference
        appPreference = AppPreference.getInstance(this);

        checkDataIdentifier = appPreference.getBoolean("DataIdentifier");

        tilWorkOrderName = findViewById(R.id.til_spare_part_work_order_name);
        tilArticleNo = findViewById(R.id.til_spare_part_article_no);
        tilQuantity = findViewById(R.id.til_spare_part_quantity);
        etWorkOrderName = findViewById(R.id.et_spare_part_work_order_name);
        etArticleNo = findViewById(R.id.et_spare_part_article_no);
        etQuantity = findViewById(R.id.et_spare_part_quantity);
        btnSave = findViewById(R.id.btn_spare_part_save);
        lv_status_saved_records = findViewById(R.id.lv_status_saved_records);
        tvSavedRecords = findViewById(R.id.tv_status_saved_records);

        etWorkOrderName.setText(workOrderName);
        etWorkOrderName.setEnabled(false);
        tilArticleNo.requestFocus();

        etArticleNo.addTextChangedListener(new SparePartActivity.ValidationTextWatcher(etArticleNo));
        etQuantity.addTextChangedListener(new SparePartActivity.ValidationTextWatcher(etQuantity));

        listAdapter = new ArrayAdapter<>(SparePartActivity.this, android.R.layout.simple_list_item_1, listData);
        lv_status_saved_records.setAdapter(listAdapter);

        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // Set button focus to default value again
                btnSave.setFocusableInTouchMode(false);

                // Validate mandatory fields
                if (!validateMandatoryFields()) {
                    return;
                }
                // Save transaction
                if (!saveTransaction()) {
                    return;
                }
            }
        });
    }

    void getAndSetIntentData() {
        if (getIntent().hasExtra("work_order_id")){
            action = getIntent().getStringExtra("action");
            workOrderId = getIntent().getStringExtra("work_order_id") ;
            workOrderNo = getIntent().getStringExtra("work_order_no");
            workOrderName = getIntent().getStringExtra("work_order_name");
        }
    }

    // Live validation of text fields (KeyEvent is not handled here)
    private class ValidationTextWatcher implements TextWatcher {
        private final View view;
        private ValidationTextWatcher(View view) {
            this.view = view;
        }
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch(view.getId()) {
                case R.id.et_spare_part_article_no:
                    validateArticleNo();
                    break;
                case R.id.et_spare_part_quantity:
                    validateQuantity();
                    break;
            }
        }
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
        } else if (!validateFbetStartRule(String.valueOf(etArticleNo.getText()))) {
            tilArticleNo.setError(getResources().getString(R.string.article_no_start_rule));
            tilArticleNo.requestFocus();
            return false;
        } else {
            tilArticleNo.setError(null);
        }
        return true;
    }

    private boolean validateQuantity() {
        if (TextUtils.isEmpty(String.valueOf(etQuantity.getText()).trim())) {
            tilQuantity.setError(getResources().getString(R.string.quantity_missing));
            tilQuantity.requestFocus();
            return false;
        } else if (etQuantity.length() > Trans.MaxQuantityLength) {
            tilQuantity.setError(getResources().getString(R.string.quantity_to_large));
            tilQuantity.requestFocus();
            return false;
        } else {
            tilQuantity.setError(null);
        }
        return true;
    }

    private Boolean validateFbetStartRule(String articleNo) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
/*** TODO: Kontroll eller inte
        if (articleNo != null && !articleNo.equals("")) {
            if (articleNo.startsWith("M") || articleNo.startsWith("F")) {
                if (articleNo.length() > 1) {
                    // Remove M/F
                    String number = articleNo.substring(1);
                    if (pattern.matcher(number).matches()) { // TODO: Hantering av bindestreck i FBET
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
 ***/
    return true;
    }

    private boolean validateMandatoryFields() {
        if (!validateArticleNo()) {
            return false;
        }
        if (!validateQuantity()) {
            return false;
        }
        return true;
    }

    public boolean saveTransaction() {
        Trans trans;
        long numberOfRecords;

        try {
            // Check if max number of transactions
            if (dbHelper.getTransCount() >= Trans.MaxNumberOfTrans) {
                sound.playExclamationTone();
                throw new Exception(getResources().getString(R.string.max_number_of_transactions));
            }

            // Create transaction record (uppercase except signature)
            trans = new Trans();
            trans.setId(DatabaseHelper.NEW_RECORD);
            trans.setTransType(TransType.TRANS_TYPE_RDH_OUT);
            trans.setWorkOrderId(Integer.parseInt(String.valueOf(workOrderId)));
            trans.setWorkOrderNo(workOrderNo);
            trans.setArticleNo(String.valueOf(etArticleNo.getText()).toUpperCase().trim());
            trans.setQuantity(Integer.parseInt(String.valueOf(etQuantity.getText())));
            trans.setDateTime(Utils.getDateTimeWithoutSeparators());

            // Check if record fully populated
            trans.isFullyPopulated();

            // Check if trans already exist
            /***
            if (dbHelper.checkIfTransRecordExist(trans)) {
                sound.playExclamationTone();
                throw new Exception("Transpost finns redan!\nSänd data till PC och töm transregistret innan du fortsätter.");
            }
             ***/

            // Save trans record
            dbHelper.createTrans(trans);
            showTransRecordsInListView(trans);
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
            return false;
        }

        // Play success tone
        sound.playSuccess();

        Toast.makeText(getApplicationContext(), "Transpost skapad", Toast.LENGTH_SHORT).show();
        clearFields();
        return true;
    }

    private void clearFields() {
        etArticleNo.setText("");
        tilArticleNo.setError(null);
        etQuantity.setText("");
        tilQuantity.setError(null);
        etArticleNo.requestFocus();
    }

    private void showTransRecordsInListView(Trans trans) {
        transList.add(trans);
        listData.clear();

        if (transList != null) {
            for (Trans t : transList) {
                listData.add("Fbet: " + t.getArticleNo() + ", " + "Antal: " + t.getQuantity());
            }
        }
        Collections.reverse(listData);
        listAdapter.notifyDataSetChanged();
        tvSavedRecords.setText("Sparade poster (" + listData.size() + "):");
    }

    // Handles different types of barcode information
    void handleScannedData() {
        if (checkDataIdentifier == true) { // Check for dataidentifiers
            if (BcrDataHelper.isArticleNo(scannedData)) { // Article Number
                etArticleNo.requestFocus();
                etArticleNo.setText(scannedData.substring(BcrDataHelper.DID_ARTICLE_NO.length()));
                BaseInputConnection inputConnection = new BaseInputConnection(etArticleNo, true);
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
            } else if (BcrDataHelper.isQuantity(scannedData)) { // Quantity, Note: nextFocusDown is set in the xml-file to set focus to button
                btnSave.setFocusableInTouchMode(true); // Set button focus to true so we can move to button
                etQuantity.requestFocus();
                etQuantity.setText(scannedData.substring(BcrDataHelper.DID_QUANTITY.length()));
                BaseInputConnection inputConnection = new BaseInputConnection(etQuantity, true);
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
            }
        } else { // No check for dataidentifiers
            if (etArticleNo.hasFocus()) {
                etArticleNo.requestFocus();
                etArticleNo.setText(scannedData);
                BaseInputConnection inputConnection = new BaseInputConnection(etArticleNo, true);
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
            } else {
                etQuantity.requestFocus();
                etQuantity.setText(scannedData);
                btnSave.setFocusableInTouchMode(true); // Set button focus to true so we can move to button
                BaseInputConnection inputConnection = new BaseInputConnection(etQuantity, true);
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        NavUtils.navigateUpFromSameTask(this);
    }
}