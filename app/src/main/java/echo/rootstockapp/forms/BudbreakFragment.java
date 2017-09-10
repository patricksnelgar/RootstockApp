package echo.rootstockapp.forms;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import echo.rootstockapp.DebugUtil;
import echo.rootstockapp.R;
import echo.rootstockapp.dialogs.DateSelectionDialog;

public class BudbreakFragment extends BaseFragment implements View.OnClickListener {

    private final String TAG = BudbreakFragment.class.getSimpleName();

    private String runEnvironment;
    private DebugUtil debugUtil;
    final View.OnClickListener saveDataOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            String user = getUser();
            debugUtil.logMessage(TAG, "User (" + user + ") wants to save data", runEnvironment);
        }
    };
    private RelativeLayout budBreakForm;
    private TextView textVineBudBreakStart;
    private TextView textCaneBudBreakStart;
    private TextView textCaneBudBreakFinish;
    private TextView textVineFloweringStart;
    private TextView textCaneFloweringStart;
    private TextView textCaneFloweringFinish;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflateFragment(R.layout.budbreak_flowering_layout, inflater, container);

        budBreakForm = (RelativeLayout) v.findViewById(R.id.form_budbreak_flowering);

        textVineBudBreakStart = (TextView) v.findViewById(R.id.text_vine_budbreak_start);
        textCaneBudBreakStart = (TextView) v.findViewById(R.id.text_cane_budbreak_start);
        textCaneBudBreakFinish = (TextView) v.findViewById(R.id.text_cane_budbreak_finish);
        textVineFloweringStart = (TextView) v.findViewById(R.id.text_vine_flowering_start);
        textCaneFloweringStart = (TextView) v.findViewById(R.id.text_cane_flowering_start);
        textCaneFloweringFinish = (TextView) v.findViewById(R.id.text_cane_flowering_finish);

        runEnvironment = getRunEnvironment();
        debugUtil = new DebugUtil();

        registerSaveButton(saveDataOnClickListener);

        return v;
    }

    @Override
    public void onBarcodeFound(List<String> identifier) {
        super.onBarcodeFound(identifier);

        clearInputs(budBreakForm);
        enableInputs(budBreakForm);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                budBreakForm.setBackgroundColor(Color.parseColor("#fafafa"));
            }
        });

        textVineBudBreakStart.setOnClickListener(this);
        textCaneBudBreakStart.setOnClickListener(this);
        textCaneBudBreakFinish.setOnClickListener(this);
        textVineFloweringStart.setOnClickListener(this);
        textCaneFloweringStart.setOnClickListener(this);
        textCaneFloweringFinish.setOnClickListener(this);

        populateDataFields(loadCaneObservationsById(identifier.get(0)));

    }

    @Override
    void populateDataFields(List<String[]> _observations) {
        if (_observations == null) return;

        for (final String[] measurement : _observations) {
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (measurement[0]) {
                        case "1":
                            textCaneBudBreakStart.setText(parseDate(measurement[1]));
                            break;
                        case "2":
                            textCaneBudBreakFinish.setText(parseDate(measurement[1]));
                            break;
                        case "3":
                            textCaneFloweringStart.setText(parseDate(measurement[1]));
                            break;
                        case "6":
                            textCaneFloweringFinish.setText(parseDate(measurement[1]));
                            break;
                        default:
                            debugUtil.logMessage(TAG, "Invalid or out of context ID (" + measurement[0] + ")", runEnvironment);
                            break;
                    }
                }
            });
        }
    }

    private String parseDate(String date) {
        try {
            String[] dateSplit = date.split("-");
            String returnDate = dateSplit[2] + "/" + dateSplit[1] + "/" + dateSplit[0];
            debugUtil.logMessage(TAG, "output date (" + returnDate + ")", runEnvironment);
            return returnDate;
        } catch (Exception e) {
            debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, runEnvironment);
            return null;
        }
    }

    @Override
    public void onClick(View view) {
        debugUtil.logMessage(TAG, "View clicked (" + view.getTag().toString() + ")", runEnvironment);

        DateSelectionDialog datePicker = new DateSelectionDialog();

        Bundle args = new Bundle();
        args.putString(getString(R.string.start_date), ((TextView) view).getText().toString());
        datePicker.setArguments(args);

        datePicker.setActivatorView(view);
        datePicker.show(getActivity().getFragmentManager(), "datePicker");

    }
}