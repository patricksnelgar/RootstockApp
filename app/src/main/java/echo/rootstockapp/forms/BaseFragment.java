package echo.rootstockapp.forms;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import echo.rootstockapp.DebugUtil;
import echo.rootstockapp.MeasurementText;
import echo.rootstockapp.R;

public class BaseFragment extends Fragment {

    private static String TAG = BaseFragment.class.getSimpleName();

    private RelativeLayout header; 
    private RelativeLayout footer;
    private TextView textFieldBarcode;
    private DebugUtil debugUtil;
    private String run_environment;
    private Button buttonSave;
    
   
    protected View inflateFragment(int resId, LayoutInflater inflator, ViewGroup container) {
        View v = inflator.inflate(resId, container,false);

        header = (RelativeLayout) v.findViewById(R.id.include_header);
        footer = (RelativeLayout) v.findViewById(R.id.include_footer);
        textFieldBarcode = (TextView) header.findViewById(R.id.barcode);
        textFieldBarcode.setOnClickListener(barcodeOnClickListener);
        buttonSave = (Button) footer.findViewById(R.id.button_save);

        Context c = getActivity();

        run_environment = c.getSharedPreferences(c.getString(R.string.pref_file), Context.MODE_PRIVATE)
                                .getString(c.getString(R.string.env), null);
        debugUtil = new DebugUtil();

        return v;
    }

    public void onBarcodeFound(final List<String> identifier){
        enableFooter();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run(){
                ((TextView) header.findViewById(R.id.barcode)).setText(identifier.get(1));
                ((TextView) header.findViewById(R.id.FPI)).setText(identifier.get(5));
                ((TextView) header.findViewById(R.id.cultivar)).setText(identifier.get(6));
            }
        });
    }

    private boolean isInputField(View _view){
        
        if(_view instanceof MeasurementText) return true;
        if(_view instanceof CheckBox) return true;
        if(_view instanceof EditText) return true;
        //if(_view instanceof Spinner) return true;

        return false;
    }

    public void showView(final ViewGroup v){
        getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run(){
                v.setVisibility(View.VISIBLE);
            }
        });
    }

    public void enableInputs(final ViewGroup v){
        getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run(){

                for(int i = 0; i < v.getChildCount(); i++){
                    View _child = v.getChildAt(i);
                    //debugUtil.logMessage(TAG, "View is type: (" + _child.getClass().getSimpleName() + ")", run_environment);
                    if(_child instanceof ViewGroup) {
                        //_child.setBackgroundColor(Color.parseColor("#fafafa"));
                        enableInputs((ViewGroup) _child);
                    } else if(isInputField(_child))
                        _child.setEnabled(true);
                }
            }
        });
    }

    public void enableComponent(final View v){
        getActivity().runOnUiThread(new Runnable(){ 
            @Override
            public void run() {
                v.setEnabled(true);
            }
        });
    }
    
    private void enableFooter(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonSave.setEnabled(true);
            }
        });      
    }

    public String getRunEnvironment(){ 
        return run_environment; 
    }

    public String getUser(){
        Context c = getActivity();
        return c.getSharedPreferences(c.getString(R.string.pref_file), Context.MODE_PRIVATE).getString(c.getString(R.string.username), null);
    }

    public String getBarcode() { 
        return ((TextView) header.findViewById(R.id.barcode)).getText().toString(); 
    }

    public void registerSaveButton(View.OnClickListener listener){
        buttonSave.setOnClickListener(listener);
    }

    public void showToastNotification(final String message){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    final View.OnClickListener barcodeOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view){
            debugUtil.logMessage(TAG, "## show user list of barcodes / FPIs", run_environment);
        }
    };
}