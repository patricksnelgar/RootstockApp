package echo.rootstockapp.forms;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import android.widget.Spinner;
import android.widget.TextView;
import echo.rootstockapp.DbHelper;
import echo.rootstockapp.DebugUtil;
import echo.rootstockapp.MeasurementText;
import java.util.Arrays;
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
    private Spinner observationCaneType;
    private DebugUtil debugUtil;
    private Button buttonSave;
    private TextView textCm;
    private TextView textMm;

    private ArrayAdapter<String> adapterCaneType;
    private List<String> listCaneTypes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflateFragment(R.layout.cane_details_layout, inflater, container);

        caneInfoHolder = (RelativeLayout) v.findViewById(R.id.form_cane_details);

        observationCaneLength = (MeasurementText) caneInfoHolder.findViewById(R.id.cane_length);
        observationCaneDiameter = (MeasurementText) caneInfoHolder.findViewById(R.id.cane_diameter);
        observationCaneExists = (CheckBox) caneInfoHolder.findViewById(R.id.cane_exists);
        observationCaneType = (Spinner) caneInfoHolder.findViewById(R.id.cane_type);

        textMm = (TextView) caneInfoHolder.findViewById(R.id.text_mm);
        textCm = (TextView) caneInfoHolder.findViewById(R.id.text_cm);

        buttonSave = (Button) v.findViewById(R.id.button_save);

        observationCaneType.setEnabled(false);

        listCaneTypes = Arrays.asList(getActivity().getResources().getStringArray(R.array.cane_type));
        adapterCaneType = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, listCaneTypes);
        observationCaneType.setAdapter(adapterCaneType);
        adapterCaneType.notifyDataSetChanged();

        registerSaveButton(saveDataOnClickListener);

        registerListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){


                caneInfoHolder.setBackgroundColor(Color.parseColor("#fafafa"));
            }
        });

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
        enableComponent(observationCaneType);

        // work around for units textViews being hidden when a barcode is scanned
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                textMm.bringToFront();
                textCm.bringToFront();
                caneInfoHolder.setBackgroundColor(Color.parseColor("#fafafa"));
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        
    }

    private void clearInputs(){
        getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run(){
                observationCaneLength.setText("");
                observationCaneDiameter.setText("");                
                //observationEarlyOrLate.clearCheck();
                //reset other entries to default
            }
        });
    }
    
    private void loadCaneData(String barcode){
        DbHelper databaseHelper = new DbHelper(getActivity());
        List<String> data = databaseHelper.lookupObservationsForBarcode(barcode);
        if(data != null)
            debugUtil.logMessage(TAG, "Got observations: (" + data.toString() + ")", run_environment);
        else debugUtil.logMessage(TAG, "No observations found for (" + barcode + ")", run_environment);
        if(databaseHelper != null) databaseHelper.close();
    }

    final View.OnClickListener saveDataOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view){
            String user = getUser();
            String barcode = getBarcode();
            String stringCaneLength = observationCaneLength.getText().toString();
            String stringCaneDiameter = observationCaneDiameter.getText().toString();
            boolean boolCaneExists = observationCaneExists.isChecked();
            
            if(user != null){
                debugUtil.logMessage(TAG, "User (" + user + ") wants to save data for (" + barcode + ")", run_environment);
                
                List<String> data = new ArrayList<String>();
                data.add(barcode);
                data.add(stringCaneLength);
                data.add(stringCaneDiameter);
                data.add(String.valueOf(boolCaneExists));
               

                DbHelper databaseHelper = new DbHelper(getActivity());
                databaseHelper.saveCaneData(data);
                if(databaseHelper != null) databaseHelper.close();
            } else {
                showToastNotification("Username required to save data");
            }
        }
    };   
}