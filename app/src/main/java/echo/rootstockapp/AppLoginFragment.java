package echo.rootstockapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import echo.rootstockapp.AppLoginFragment.OnLoginVerifyListener;

public  class AppLoginFragment extends Fragment{
    
    private final String TAG = AppLoginFragment.class.getSimpleName();
    private DebugUtil debugUtil = new DebugUtil();
    private String run_environment;
    private TextView textPinMessage;
    private TextView textUsernameMessage;
    private EditText editPin;
    private EditText editUsername;
    private ImageView iconLockState;
    private boolean validPin = false;
    private boolean validUsername = false;

    private OnLoginVerifyListener loginVerifyListener;

    public interface OnLoginVerifyListener {
        public void onLoginVerify(boolean verified);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            loginVerifyListener = (OnLoginVerifyListener) activity;
        } catch (ClassCastException c){
            throw new ClassCastException(activity.toString() + " must implement OnLoginVerifyListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View v = inflater.inflate(R.layout.app_login_layout, container,false);
        
        run_environment = getActivity().getResources().getString(R.string.run_environment);

        editPin = (EditText) v.findViewById(R.id.pin);
        editUsername = (EditText) v.findViewById(R.id.username);
        final Button buttonLogin = (Button) v.findViewById(R.id.buttonLogin);  
        iconLockState = (ImageView) v.findViewById(R.id.iconLockState);
        textPinMessage = (TextView) v.findViewById(R.id.textPinMessage);
        textUsernameMessage = (TextView) v.findViewById(R.id.textUsernameMessage);

        editPin.setShowSoftInputOnFocus(false);
        editUsername.setShowSoftInputOnFocus(false);
        buttonLogin.setOnClickListener(loginOnClickListener);

        editPin.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if(hasFocus){
                    textPinMessage.setVisibility(View.INVISIBLE);
                    if(validPin && validUsername){
                        iconLockState.setImageResource(R.drawable.ic_lock_black_24dp);
                    }
                }
            }
        });      
        
        editPin.setOnKeyListener(new View.OnKeyListener(){

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                // Check key event code, call verify with the view for pin checking
                if(event.getAction() == KeyEvent.ACTION_DOWN  && keyCode == KeyEvent.KEYCODE_ENTER){
                    buttonLogin.callOnClick();
                    return true;
                } else if(event.getAction() == KeyEvent.ACTION_DOWN){
                    textPinMessage.setVisibility(View.INVISIBLE);     
                              
                }
                return false;
            }
        });

        editUsername.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                // Check key event code, 
                if(event.getAction() == KeyEvent.ACTION_DOWN  && keyCode == KeyEvent.KEYCODE_ENTER){
                    buttonLogin.callOnClick();
                    return true;
                }

                return false;
            }
        });

        return v;
    }

    final View.OnClickListener loginOnClickListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v){
            debugUtil.logMessage(TAG, "Verifying info", run_environment);
            try {
                int configPin = getActivity().getResources().getInteger(R.integer.PIN);
                int inputPin = Integer.parseInt((((EditText) getActivity().findViewById(R.id.pin)).getText().toString()));
                
                if(configPin == inputPin){
                    validPin = true;
                    textPinMessage.setVisibility(View.INVISIBLE);
                } else {
                    validPin = false;
                    loginVerifyListener.onLoginVerify(false);
                    textPinMessage.setTextColor(getActivity().getResources().getColor(R.color.colorTextError, null));
                    textPinMessage.setVisibility(View.VISIBLE);
                } 
            } catch (Exception e) {
                debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
                textPinMessage.setTextColor(getActivity().getResources().getColor(R.color.colorTextError, null));
                textPinMessage.setVisibility(View.VISIBLE);
            }

            try {
                String username = ((EditText) getActivity().findViewById(R.id.username)).getText().toString();
                debugUtil.logMessage(TAG, "Username is: <" + username + "> length=" + username.length(), run_environment);
                 if(username.length() <= 0){
                    validUsername = false;
                    textUsernameMessage.setTextColor(getActivity().getResources().getColor(R.color.colorTextError, null));
                    textUsernameMessage.setVisibility(View.VISIBLE);                    
                } else {                    
                    validUsername = true;
                    textUsernameMessage.setVisibility(View.INVISIBLE);
                }
            } catch (Exception e) {
                debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
            }
           
            if(validPin && validUsername){
                // TODO: implement storing username in the preferences
                debugUtil.logMessage(TAG, "User verified", run_environment);
                editPin.setEnabled(false);
                editUsername.setEnabled(false);
                iconLockState.setImageResource(R.drawable.ic_lock_open_black_24dp);
                loginVerifyListener.onLoginVerify(true);
            } 
        }
    };    
}