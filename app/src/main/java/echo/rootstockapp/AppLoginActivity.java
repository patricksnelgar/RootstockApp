package echo.rootstockapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AppLoginActivity extends Activity {

    private final String TAG = AppLoginActivity.class.getSimpleName();
    private DebugUtil debugUtil = new DebugUtil();
    private String run_environment;
    private TextView textPinMessage;
    private TextView textUsernameMessage;
    private EditText editPin;
    private EditText editUsername;
    private boolean validPin = false;
    private boolean validUsername = false;
    final View.OnClickListener loginOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            debugUtil.logMessage(TAG, "Verifying info", run_environment);
            try {
                int configPin = getResources().getInteger(R.integer.PIN);
                int inputPin = Integer.parseInt((((EditText) findViewById(R.id.pin)).getText().toString()));

                if (configPin == inputPin) {
                    validPin = true;
                    textPinMessage.setVisibility(View.INVISIBLE);
                } else {
                    validPin = false;
                    textPinMessage.setTextColor(getResources().getColor(R.color.colorTextError, null));
                    textPinMessage.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
                textPinMessage.setTextColor(getResources().getColor(R.color.colorTextError, null));
                textPinMessage.setVisibility(View.VISIBLE);
            }

            try {
                String username = ((EditText) findViewById(R.id.username)).getText().toString();
                debugUtil.logMessage(TAG, "Username is: <" + username + "> length=" + username.length(), run_environment);
                if (username.length() <= 0) {
                    validUsername = false;
                    textUsernameMessage.setTextColor(getResources().getColor(R.color.colorTextError, null));
                    textUsernameMessage.setVisibility(View.VISIBLE);
                } else {
                    validUsername = true;
                    textUsernameMessage.setVisibility(View.INVISIBLE);
                }
            } catch (Exception e) {
                debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
            }

            if (validPin && validUsername) {
                debugUtil.logMessage(TAG, "User verified", run_environment);
                editPin.setEnabled(false);
                editUsername.setEnabled(false);

                SharedPreferences prefs = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(getString(R.string.username), editUsername.getText().toString());
                editor.commit();

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_login_layout);

        run_environment = getResources().getString(R.string.run_environment);

        editPin = (EditText) findViewById(R.id.pin);
        editUsername = (EditText) findViewById(R.id.username);
        final Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
        textPinMessage = (TextView) findViewById(R.id.textPinMessage);
        textUsernameMessage = (TextView) findViewById(R.id.textUsernameMessage);

        editPin.setShowSoftInputOnFocus(false);
        editUsername.setShowSoftInputOnFocus(false);
        buttonLogin.setOnClickListener(loginOnClickListener);

        editPin.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Check key event code, call verify with the view for pin checking
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    buttonLogin.callOnClick();
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    textPinMessage.setVisibility(View.INVISIBLE);

                }
                return false;
            }
        });

        editUsername.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Check key event code,
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    buttonLogin.callOnClick();
                    return true;
                }

                return false;
            }
        });

    }
}