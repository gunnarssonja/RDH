package se.cgi.android.rdh.activities;

import androidx.core.app.NavUtils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import se.cgi.android.rdh.R;
import se.cgi.android.rdh.utils.AppPreference;

/**
 * SettingsActivity - Activity class for application settings.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class SettingsActivity extends NonBcrActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private AppPreference appPreference;
    private TextInputLayout til_place;
    private EditText et_place;
    private String place;
    private Boolean dataIdentifier;
    private CheckBox chk_data_identifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Inställningar");

        // Get singleton instance of AppPreference
        appPreference = AppPreference.getInstance(this);
        place = appPreference.getString("Place");
        dataIdentifier = appPreference.getBoolean("DataIdentifier");

        til_place = findViewById(R.id.til_place);
        et_place = findViewById(R.id.et_place);
        et_place.setText(place);

        chk_data_identifier = findViewById(R.id.chk_data_identifiers);
        chk_data_identifier.setChecked(dataIdentifier);
        et_place.requestFocus();
    }

    public void btnSaveSettings(View v) {
        // Validate mandatory fields
        if (!validateMandatoryFields()) {
            return;
        }

        // Save preferences
        appPreference.saveString("Place", String.valueOf(et_place.getText()).toUpperCase().trim());
        if (chk_data_identifier.isChecked()) {
            appPreference.saveBoolean("DataIdentifier", true);
        } else {
            appPreference.saveBoolean("DataIdentifier", false);
        }

        Toast.makeText(getApplicationContext(), "Inställningarna sparas", Toast.LENGTH_SHORT).show();
    }

    // Validate mandatory fields
    private boolean validateMandatoryFields() {
        // Validate Place
        if (TextUtils.isEmpty(String.valueOf(et_place.getText()).trim())) {
            til_place.setError("Plats är obligatorisk");
            til_place.requestFocus();
            return false;
        } else if (et_place.length() > 2) {
            til_place.setError("Plats får bara innehålla 2 tecken");
            til_place.requestFocus();
            return false;
        } else if (et_place.length() < 2 ) {
            til_place.setError("Plats måste innehålla 2 tecken");
            til_place.requestFocus();
            return false;
        } else {
            til_place.setError(null);
        }
        return true;
    }

    @Override
    public void onBackPressed()
    {
        NavUtils.navigateUpFromSameTask(this);
    }
}