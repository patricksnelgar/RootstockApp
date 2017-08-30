package echo.rootstockapp.forms;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import echo.rootstockapp.DbHelper;
import echo.rootstockapp.DebugUtil;
import echo.rootstockapp.R;
import echo.rootstockapp.views.MeasurementText;

/**
 * Fragment that displays input fields for gathering cane level information
 */
public class CaneInfoFragment extends BaseFragment {

    private static String TAG = CaneInfoFragment.class.getSimpleName();

    private String run_environment;
    private RelativeLayout caneInfoHolder;
    private MeasurementText observationCaneLength;
    private MeasurementText observationCaneDiameter;
    private CheckBox observationCaneExists;
    private Spinner observationCaneType;
    private DebugUtil debugUtil;

    final View.OnClickListener saveDataOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            String user = getUser();
            String barcode = getBarcode();
            String stringCaneLength = observationCaneLength.getText().toString();
            String stringCaneDiameter = observationCaneDiameter.getText().toString();
            boolean boolCaneExists = observationCaneExists.isChecked();

            if (user != null) {
                debugUtil.logMessage(TAG, "User (" + user + ") wants to save data for (" + barcode + ")", run_environment);

                List<String> data = new ArrayList<>();
                data.add(barcode);
                data.add(stringCaneLength);
                data.add(stringCaneDiameter);
                data.add(String.valueOf(boolCaneExists));


                DbHelper databaseHelper = new DbHelper(getActivity());
                databaseHelper.saveCaneData(data);
                databaseHelper.close();
            } else {
                showToastNotification("Username required to save data");
            }
        }
    };
    private TextView textCm;
    private TextView textMm;

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

        observationCaneType.setEnabled(false);

        List<String> listCaneTypes = Arrays.asList(getActivity().getResources().getStringArray(R.array.cane_type));
        ArrayAdapter<String> adapterCaneType = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, listCaneTypes);
        observationCaneType.setAdapter(adapterCaneType);
        adapterCaneType.notifyDataSetChanged();

        registerSaveButton(saveDataOnClickListener);

        debugUtil = new DebugUtil();
        run_environment = getRunEnvironment();

        return v;
    }

    @Override
    public void onBarcodeFound(List<String> identifier) {
        super.onBarcodeFound(identifier);

        // Do stuff for this form type
        // like load data from the observation table
        clearInputs();
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

        populateDataFields(loadCaneObservationsById(identifier.get(0)));
    }

    private void clearInputs() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                observationCaneLength.setText("");
                observationCaneDiameter.setText("");
                observationCaneType.setSelection(0);
                //observationEarlyOrLate.clearCheck();
                //reset other entries to default
            }
        });
    }

    private void populateDataFields(final List<String[]> _observations) {
        if (_observations == null) return;

        for (String[] measurement : _observations) {

            /*
              Switch on the measurement _ID

              1 - BB start date (cane)
              2 - BB Finish date (cane)
              3 - Flower start date (cane)
              4 -
              5 -
              6 - Flower finish date (cane)
              7 - General comment
              ?
            */
            switch (measurement[0]) {

                case "7":
                    // Need to implement comment field first
                    debugUtil.logMessage(TAG, "Comment is (" + measurement[1] + ")", run_environment);
                    break;
                default:
                    debugUtil.logMessage(TAG, "Invalid or out of context ID (" + measurement[0] + ")", run_environment);
                    break;
            }
        }
    }
}