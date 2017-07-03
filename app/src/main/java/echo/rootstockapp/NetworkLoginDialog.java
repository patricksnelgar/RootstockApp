package echo.rootstockapp;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class NetworkLoginDialog extends DialogFragment{

    private final String TAG = NetworkLoginDialog.class.getSimpleName();
    private final String authourizationKey = "ApiKey handheld:k7anf9hqphs0zjunodtlfgg3kozbt8lstufdsp2r257edvjr2d";

    private Dialog      dialog;
    private EditText    editUsername;
    private EditText    editPassword;
    private TextView    textResponse;
    private Button      buttonLogin;
    private Button      buttonCancel;

    private DebugUtil   debugUtil;

    private String      authenticateResponse;
    private String      API_URL;
    private String      run_environment;    

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

        return dialog;
    }

    private void makeAuthenticateRequest(final String username, final String password) {

        if(username.length() == 0 || password.length() == 0 || username == null || password == null){
            Toast.makeText(getContext(), "Credentials cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Authorization", authourizationKey);
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
                textResponse.setTextColor(R.color.colorTextError);
                textResponse.setText("Error logging in");                    
            }
        });
    }

    private void setResponse(String r){
        authenticateResponse = r;
    }

    public void setConfig(String api, String run){
        API_URL         = api;
        run_environment = run;
    }

    final View.OnClickListener buttonLoginOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v){
            textResponse.setText("");
            final String username = editUsername.getText().toString();
            final String password = editPassword.getText().toString();
            
            makeAuthenticateRequest(username, password);
        }
    };

    final View.OnClickListener buttonCancelOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v){
            dialog.dismiss();
        }
    };
}