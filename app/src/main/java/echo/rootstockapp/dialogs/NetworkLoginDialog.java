package echo.rootstockapp.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import echo.rootstockapp.DebugUtil;
import echo.rootstockapp.R;

public class NetworkLoginDialog extends DialogFragment {

    private final String TAG = NetworkLoginDialog.class.getSimpleName();

    private Dialog      dialog;
    final View.OnClickListener buttonCancelOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    };
    private EditText    editUsername;
    private EditText    editPassword;
    private TextView    textResponse;
    private Button      buttonLogin;
    private Button      buttonCancel;
    private DebugUtil   debugUtil;
    private String      authenticateResponse;
    private String      API_URL;
    private String run_environment;
    private String AUTHORIZATION_KEY;
    final View.OnClickListener buttonLoginOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            textResponse.setText("");
            final String username = editUsername.getText().toString();
            final String password = editPassword.getText().toString();

            makeAuthenticateRequest(username, password);
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        debugUtil = new DebugUtil();

        dialog = super.onCreateDialog(savedInstanceState);
        dialog.setContentView(R.layout.login_dialog_layout);

        editUsername =  (EditText) dialog.findViewById(R.id.username);
        editPassword =  (EditText) dialog.findViewById(R.id.password);
        textResponse =  (TextView) dialog.findViewById(R.id.textResponse);
        buttonLogin  =  (Button) dialog.findViewById(R.id.buttonLogin);
        buttonCancel =  (Button) dialog.findViewById(R.id.buttonCancel);

        buttonLogin.setOnClickListener(buttonLoginOnClickListener);
        buttonCancel.setOnClickListener(buttonCancelOnClickListener);

        Context c = getActivity();
        SharedPreferences prefs = c.getSharedPreferences(c.getString(R.string.pref_file), Context.MODE_PRIVATE);

        run_environment = getString(R.string.run_environment);
        API_URL = getString(R.string.API_URL);
        AUTHORIZATION_KEY = getString(R.string.authorization_key);

        return dialog;
    }

    private void makeAuthenticateRequest(final String username, final String password) {

        if(username.length() == 0 || password.length() == 0 || username == null || password == null){
            Toast.makeText(getContext(), "Credentials cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (AUTHORIZATION_KEY == null) {
            debugUtil.logMessage(TAG, "No API authorization key", DebugUtil.LOG_LEVEL_ERROR, run_environment);
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Authorization", AUTHORIZATION_KEY);
        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);

        client.post(API_URL+"/authenticate", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response){
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Status: " + statusCode + " response: " + responseString, run_environment);
                debugUtil.logMessage(TAG, "Contains error: " + responseString.contains("error"), run_environment);
                if(statusCode == 200 && !responseString.contains("error")) {
                    textResponse.setTextColor(getActivity().getResources().getColor(R.color.colorTextSuccess, null));
                    textResponse.setText("Login successful");
                } else {
                    textResponse.setTextColor(getActivity().getResources().getColor(R.color.colorTextError, null));
                    textResponse.setText("Error logging in");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,  byte[] response, Throwable error){
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Status: " + statusCode + " response: " + responseString, DebugUtil.LOG_LEVEL_ERROR, run_environment);
                textResponse.setTextColor(getActivity().getResources().getColor(R.color.colorTextError, null));
                textResponse.setText("Error logging in");
            }
        });
    }

    private void setResponse(String r){
        authenticateResponse = r;
    }
}