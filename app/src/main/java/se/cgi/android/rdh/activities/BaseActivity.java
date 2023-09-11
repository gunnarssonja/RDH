package se.cgi.android.rdh.activities;

import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

/***
 * BaseActivity - Base class for activities.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class BaseActivity extends AppCompatActivity {

    // Handles certain key events
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ESCAPE:
                super.onBackPressed();
                return true;
            case KeyEvent.KEYCODE_F1:
                //Intent intent = new Intent(this, TransactionListActivity.class);
                //intent.putExtra("Function_Key", "F1");
                //startActivity(intent);
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
}
