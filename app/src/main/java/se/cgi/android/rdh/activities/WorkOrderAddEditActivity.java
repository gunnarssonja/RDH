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
import se.cgi.android.rdh.models.WorkOrder;

/***
 * WorkOrderAddEditActivity - Activity class for add, update and delete of work order.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class WorkOrderAddEditActivity extends NonBcrActivity {
    private static final String TAG = WorkOrderAddEditActivity.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private int id;
    private EditText etWorkOrderNo, etWorkOrderName;
    private TextInputLayout tilWorkOrderNo, tilWorkOrderName;
    private String action;
    private String comingFrom;
    private Button btnSave;
    private Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order_add_edit);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setTitle("Hantera Arbetsorder");

        // Get singleton instance of database
        dbHelper = DatabaseHelper.getInstance(this);

        etWorkOrderNo = findViewById(R.id.et_work_order_no);
        etWorkOrderName = findViewById(R.id.et_work_order_name);
        tilWorkOrderNo = findViewById(R.id.til_work_order_no);
        tilWorkOrderName = findViewById(R.id.til_work_order_name);

        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);

        getAndSetIntentData();

        etWorkOrderNo.addTextChangedListener(new ValidateTextWatcher(etWorkOrderNo));
        etWorkOrderName.addTextChangedListener(new ValidateTextWatcher(etWorkOrderName));

        if ("edit".equals(action)) {
            etWorkOrderNo.setEnabled(false);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("add".equals(action)) { // Add new record
                    if (!validateMandatoryFields()) {
                        return;
                    }
                    if (!addWorkOrder()) {
                        return;
                    }
                } else { // Update record
                    if (!validateMandatoryFields()) {
                        return;
                    }
                    if (!updateWorkOrder()) {
                        return;
                    }
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbHelper.checkIfWorkOrderTransExists(String.valueOf(etWorkOrderNo.getText()))) {
                    tilWorkOrderNo.setError(getResources().getString(R.string.work_order_has_transactions));
                    etWorkOrderNo.requestFocus();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(WorkOrderAddEditActivity.this)
                        .setTitle("Ta bort")
                        .setMessage("Vill du ta bort posten?")
                        .setCancelable(true)
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper.deleteWorkOrderById(id);
                                Intent intent = new Intent(WorkOrderAddEditActivity.this, WorkOrderListActivity.class);
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
        if (getIntent().hasExtra("work_order_id")){
            action = getIntent().getStringExtra("action");
            id = getIntent().getIntExtra("work_order_id", 0) ;
            etWorkOrderNo.setText(getIntent().getStringExtra("work_order_no"));
            etWorkOrderName.setText(getIntent().getStringExtra("work_order_name"));
            etWorkOrderName.requestFocus();
        } else {  // Add - from menu "Ny arbetsorder"
            action = getIntent().getStringExtra("action");
            comingFrom = getIntent().getStringExtra("comingFrom");
            btnDelete.setEnabled(false);
            etWorkOrderNo.requestFocus();
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
                    validateWorkOrderName();
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

    private boolean validateWorkOrderName() {
        if (TextUtils.isEmpty(String.valueOf(etWorkOrderName.getText()).trim())) {
            tilWorkOrderName.setError(getResources().getString(R.string.work_order_name_missing));
            tilWorkOrderName.requestFocus();
            return false;
        } else if (etWorkOrderName.length() > WorkOrder.MaxWorkOrderNameLength) {
            tilWorkOrderName.setError(getResources().getString(R.string.work_order_name_to_large));
            tilWorkOrderName.requestFocus();
            return false;
        } else {
            tilWorkOrderName.setError(null);
        }
        return true;
    }

    private boolean validateMandatoryFields() {
        if (!validateWorkOrderNo()) {
            return false;
        }
        if (!validateWorkOrderName()) {
            return false;
        }
        return true;
    }

    private boolean addWorkOrder() {
        if (dbHelper.checkIfWorkOrderExists(String.valueOf(etWorkOrderNo.getText()))) {
            tilWorkOrderNo.setError("Arbetsorder finns redan i databasen");
            etWorkOrderNo.requestFocus();
            return false;
        }

        try {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setId(NEW_RECORD);
            workOrder.setWorkOrderNo(String.valueOf(etWorkOrderNo.getText()));
            workOrder.setWorkOrderName(String.valueOf(etWorkOrderName.getText()));
            dbHelper.createWorkOrder(workOrder);
            Intent intent;
            if (comingFrom.equals("StorageTakeOutActivity")) {
                intent = new Intent(WorkOrderAddEditActivity.this, StorageTakeOutListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else {
                intent = new Intent(WorkOrderAddEditActivity.this, WorkOrderListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);
            finish();
        } catch(Exception e){
            handleException(e);
        }
        return true;
    }

    private boolean updateWorkOrder() {
        try {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setId(id);
            workOrder.setWorkOrderNo(String.valueOf(etWorkOrderNo.getText()));
            workOrder.setWorkOrderName(String.valueOf(etWorkOrderName.getText()));
            dbHelper.updateWorkOrder(workOrder);
            Intent intent = new Intent(WorkOrderAddEditActivity.this, WorkOrderListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch(Exception e) {
            handleException(e);
        }
        return true;
    }

    private void handleException(Exception e) {
        if (Objects.requireNonNull(e.getMessage()).contains("Arbetsorder")) {
            tilWorkOrderNo.setError(e.getMessage());
            tilWorkOrderNo.requestFocus();
        } else if(Objects.requireNonNull(e.getMessage()).contains("Namn")){
            tilWorkOrderName.setError(e.getMessage());
            tilWorkOrderName.requestFocus();
        } else {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}