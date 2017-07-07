package echo.rootstockapp.forms;

import android.R.color;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import echo.rootstockapp.DebugUtil;
import java.util.List;
import android.widget.TextView;

import echo.rootstockapp.MeasurementText;

import echo.rootstockapp.R;

public class BaseFragment extends Fragment {

    private static String TAG = BaseFragment.class.getSimpleName();

    private RelativeLayout header; 
    private DebugUtil debugUtil;
    private String run_environment;
    
   
    protected View inflateFragment(int resId, LayoutInflater inflator, ViewGroup container) {
        View v = inflator.inflate(resId, container, false);

        header = (RelativeLayout) v.findViewById(R.id.include_header);
        
        Context c = getActivity();

        run_environment = c.getSharedPreferences(c.getString(R.string.pref_file), Context.MODE_PRIVATE)
                                .getString(c.getString(R.string.env), null);
        debugUtil = new DebugUtil();

        return v;
    }

    public void onBarcodeFound(final List<String> identifier){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run(){
                ((TextView) header.findViewById(R.id.barcode)).setText(identifier.get(1));
                ((TextView) header.findViewById(R.id.FPI)).setText(identifier.get(5));
                ((TextView) header.findViewById(R.id.cultivar)).setText(identifier.get(6));
            }
        });
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
                v.setBackgroundColor(Color.parseColor("#fafafa"));
                for(int i = 0; i < v.getChildCount(); i++){
                    View _child = v.getChildAt(i);
                    if(_child instanceof ViewGroup) enableInputs((ViewGroup) _child);
                    else if(!_child.isEnabled()) _child.setEnabled(true);
                }
            }
        });
    }
    
    public void enableFooterButtons(final Button... buttons){

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run(){
                for(Button b : buttons){
                    b.setEnabled(true);
                }
            }
        });
        
    }

    public String getRunEnvironment(){ return run_environment; }

    public String getBarcode() { 
        return ((TextView) header.findViewById(R.id.barcode)).getText().toString(); 
    }
    
    public void showToastNotification(final String message){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}