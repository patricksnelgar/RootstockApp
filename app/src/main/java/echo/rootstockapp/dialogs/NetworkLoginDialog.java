package echo.rootstockapp.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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
    private EditText editUsername;
    private EditText editPassword;
    private TextView textResponse;
    private DebugUtil debugUtil;
    private String API_URL;
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
    private Dialog dialog;
    final View.OnClickListener buttonCancelOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        debugUtil = new DebugUtil();

        dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.login_dialog_layout);


        editUsername = (EditText) dialog.findViewById(R.id.username);
        editPassword = (EditText) dialog.findViewById(R.id.password);
        textResponse = (TextView) dialog.findViewById(R.id.textResponse);
        Button buttonLogin = (Button) dialog.findViewById(R.id.buttonLogin);
        Button buttonCancel = (Button) dialog.findViewById(R.id.buttonCancel);

        buttonLogin.setOnClickListener(buttonLoginOnClickListener);
        buttonCancel.setOnClickListener(buttonCancelOnClickListener);

        run_environment = getString(R.string.run_environment);
        API_URL = getString(R.string.API_URL);
        AUTHORIZATION_KEY = getString(R.string.authorization_key);

        return dialog;
    }

    private void makeAuthenticateRequest(final String username, final String password) {

        if (username.length() == 0 || password.length() == 0) {
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

        client.post(API_URL + "/authenticate", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Status: " + statusCode + " response: " + responseString, run_environment);
                debugUtil.logMessage(TAG, "Contains error: " + responseString.contains("error"), run_environment);
                if (statusCode == 200 && !responseString.contains("error")) {
                    textResponse.setTextColor(getActivity().getResources().getColor(R.color.colorTextSuccess, null));
                    textResponse.setText(R.string.network_login_success_text);
                } else {
                    textResponse.setTextColor(getActivity().getResources().getColor(R.color.colorTextError, null));
                    textResponse.setText(R.string.network_login_fail_text);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error) {
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Status: " + statusCode + " response: " + responseString, DebugUtil.LOG_LEVEL_ERROR, run_environment);
                textResponse.setTextColor(getActivity().getResources().getColor(R.color.colorTextError, null));
                textResponse.setText(R.string.network_login_fail_text);
            }
        });
    }
}