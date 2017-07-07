package echo.rootstockapp.forms;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import echo.rootstockapp.DbHelper;
import echo.rootstockapp.DebugUtil;
import echo.rootstockapp.MeasurementText;
import java.util.List;
import java.util.ArrayList;

import echo.rootstockapp.R;

public class CaneInfoFragment extends BaseFragment {

    private static String TAG = CaneInfoFragment.class.getSimpleName();
    
    private String run_environment;
    private RelativeLayout caneInfoHolder;
    private MeasurementText observationCaneLength;
    private MeasurementText observationCaneDiameter;
    private CheckBox observationCaneExists;
    private RadioButton observationEarly;
    private RadioButton observationLate;
    private RadioGroup observationEarlyOrLate;
    private DebugUtil debugUtil;
    private Button buttonSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflateFragment(R.layout.cane_info_fragment_layout, inflater, container);

        caneInfoHolder = (RelativeLayout) v.findViewById(R.id.row_cane_measurements);

        observationCaneLength = (MeasurementText) caneInfoHolder.findViewById(R.id.cane_length);
        observationCaneDiameter = (MeasurementText) caneInfoHolder.findViewById(R.id.cane_diameter);
        observationCaneExists = (CheckBox) caneInfoHolder.findViewById(R.id.cane_exists);
        observationEarly = (RadioButton) caneInfoHolder.findViewById(R.id.radio_early);
        observationLate = (RadioButton) caneInfoHolder.findViewById(R.id.radio_late);
        observationEarlyOrLate = (RadioGroup) caneInfoHolder.findViewById(R.id.radio_group_early_or_late);

        buttonSave = (Button) v.findViewById(R.id.button_save);

        buttonSave.setOnClickListener(saveDataOnClickListener);

        debugUtil = new DebugUtil();
        run_environment = getRunEnvironment();

        return v;
    }

    @Override
    public void onBarcodeFound(List<String> identifier){
        super.onBarcodeFound(identifier);
        // Do stuff for this form type
        // like load data from the observation table
        clearInputs();
        loadCaneData(identifier.get(1));
        enableInputs(caneInfoHolder);
        enableFooterButtons(buttonSave);
    }

    private void clearInputs(){
        getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run(){
                observationCaneLength.setText("");
                observationCaneDiameter.setText("");
                observationCaneExists.setChecked(false);
                observationEarlyOrLate.clearCheck();
            }
        });
    }
    
    private void loadCaneData(String barcode){
        DbHelper databaseHelper = new DbHelper(getActivity());
        List<String> data = databaseHelper.lookupIdentifier(barcode);
    }

    final View.OnClickListener saveDataOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view){
            Context c = getActivity();
            String user = c.getSharedPreferences(c.getString(R.string.pref_file), Context.MODE_PRIVATE)
                            .getString(c.getString(R.string.username), null);
            String barcode = getBarcode();
            String stringCaneLength = observationCaneLength.getText().toString();
            String stringCaneDiameter = observationCaneDiameter.getText().toString();
            boolean boolCaneExists = observationCaneExists.isChecked();
            int intEarlyOrLateID = observationEarlyOrLate.getCheckedRadioButtonId();
            String stringEarlyOrLate;

            switch(intEarlyOrLateID){
                case 2131624072: 
                    stringEarlyOrLate = "Late";
                    break;
                case 2131624071:
                    stringEarlyOrLate = "Early";
                    break;
                default:
                    stringEarlyOrLate = "Not selected";
                    break;
            }
            if(user != null){
                debugUtil.logMessage(TAG, "User (" + user + ") wants to save data for (" + barcode + ")", run_environment);
                debugUtil.logMessage(TAG, "Data for (" + barcode + "): <" + stringCaneLength + 
                                            "," + stringCaneDiameter + "," + boolCaneExists + 
                                            ", { \"id\": \"" + intEarlyOrLateID + "\", \"name\":, \"" + stringEarlyOrLate +"\"}>", 
                                            run_environment);
                List<String> data = new ArrayList<String>();
                data.add(barcode);
                data.add(stringCaneLength);
                data.add(stringCaneDiameter);
                data.add(String.valueOf(boolCaneExists));
                data.add("{ \"id\": \"" + intEarlyOrLateID + "\", \"name\":, \"" + stringEarlyOrLate +"\"}");

                DbHelper databaseHelper = new DbHelper(getActivity());
                databaseHelper.
            } else {
                showToastNotification("Username required to save data");
            }
        }
    };   
}