package echo.rootstockapp.forms;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import echo.rootstockapp.DebugUtil;
import android.app.DatePickerDialog;

import echo.rootstockapp.R;
import echo.rootstockapp.dialogs.DateSelectionDialog;
import java.util.List;

public class BudBreakFragment extends BaseFragment implements View.OnClickListener {
    
    private final String TAG = BudBreakFragment.class.getSimpleName();

    private String run_environment;
    private DebugUtil debugUtil;

    private RelativeLayout budBreakForm;

    private TextView textVineBudBreakStart;
    private TextView textCaneBudBreakStart;
    private TextView textCaneBudBreakFinish;
    private TextView textVineFloweringStart;
    private TextView textCaneFloweringStart;
    private TextView textCaneFloweringFinish;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View v = inflateFragment(R.layout.budbreak_flowering_layout, inflater, container);

        budBreakForm = (RelativeLayout) v.findViewById(R.id.form_budbreak_flowering);

        textVineBudBreakStart   = (TextView) v.findViewById(R.id.text_vine_budbreak_start);
        textCaneBudBreakStart   = (TextView) v.findViewById(R.id.text_cane_budbreak_start);
        textCaneBudBreakFinish  = (TextView) v.findViewById(R.id.text_cane_budbreak_finish);
        textVineFloweringStart  = (TextView) v.findViewById(R.id.text_vine_flowering_start);
        textCaneFloweringStart  = (TextView) v.findViewById(R.id.text_cane_flowering_start);
        textCaneFloweringFinish = (TextView) v.findViewById(R.id.text_cane_flowering_finish);

        run_environment = getRunEnvironment();
        debugUtil = new DebugUtil();

        registerSaveButton(saveDataOnClickListener);

        return v;
    }

    @Override
    public void onBarcodeFound(List<String> identifier){
        super.onBarcodeFound(identifier);
        
        enableInputs(budBreakForm);

        textVineBudBreakStart.setOnClickListener(this);
        textCaneBudBreakStart.setOnClickListener(this);
        textCaneBudBreakFinish.setOnClickListener(this);
        textVineFloweringStart.setOnClickListener(this);
        textCaneFloweringStart.setOnClickListener(this);
        textCaneFloweringFinish.setOnClickListener(this);


    }

    @Override
    public void onClick(View view){
        debugUtil.logMessage(TAG, "View clicked (" + view.getTag().toString() + ")", run_environment);

        DialogFragment datePicker = new DateSelectionDialog(view);
        datePicker.show(getActivity().getFragmentManager(), "datePicker");
        
    }

    final View.OnClickListener saveDataOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view){

            String user = getUser();
            debugUtil.logMessage(TAG, "User (" + user + ") wants to save data", run_environment);
        }
    };
}