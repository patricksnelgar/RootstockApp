package pfr.clonal.forms;

import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import pfr.clonal.DatabaseHelper;
import pfr.clonal.DebugUtil;
import pfr.clonal.R;
import pfr.clonal.views.MeasurementText;

public abstract class BaseFragment extends Fragment {

    private static final String TAG = BaseFragment.class.getSimpleName();

    private RelativeLayout headerLayout;
    private DebugUtil debugUtil;
    private String runEnvironment;
    final View.OnClickListener barcodeOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            debugUtil.logMessage(TAG, "## show user list of barcodes / FPIs", runEnvironment);
        }
    };
    private Button buttonSave;

    protected View inflateFragment(int resId, LayoutInflater inflater, ViewGroup container) {
        View v = inflater.inflate(resId, container, false);

        headerLayout = (RelativeLayout) v.findViewById(R.id.include_header);
        RelativeLayout footer = (RelativeLayout) v.findViewById(R.id.include_footer);
        TextView textFieldBarcode = (TextView) headerLayout.findViewById(R.id.barcode);
        textFieldBarcode.setOnClickListener(barcodeOnClickListener);
        buttonSave = (Button) footer.findViewById(R.id.button_save);

        Context c = getActivity();

        runEnvironment = c.getString(R.string.run_environment);
        debugUtil = new DebugUtil();

        return v;
    }

    public void onBarcodeFound(final List<String> identifier) {
        enableFooter();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) headerLayout.findViewById(R.id.barcode)).setText(identifier.get(1));
                ((TextView) headerLayout.findViewById(R.id.FPI)).setText(identifier.get(5));
                ((TextView) headerLayout.findViewById(R.id.cultivar)).setText(identifier.get(6));
            }
        });
    }

    private void enableFooter() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonSave.setEnabled(true);
            }
        });
    }

    private boolean isInputField(View _view) {

        return isTextInput(_view) || _view instanceof CheckBox || _view instanceof EditText;

    }

    private boolean isTextInput(View inputField) {
        return inputField instanceof TextView || inputField instanceof MeasurementText;
    }

    public void enableInputs(final ViewGroup v) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < v.getChildCount(); i++) {
                    View _child = v.getChildAt(i);
                    //debugUtil.logMessage(TAG, "View is type: (" + _child.getClass().getSimpleName() + ")", runEnvironment);
                    if (_child instanceof ViewGroup) {
                        //_child.setBackgroundColor(Color.parseColor("#fafafa"));
                        enableInputs((ViewGroup) _child);
                    } else if (isInputField(_child))
                        _child.setEnabled(true);
                }
            }
        });
    }

    public void enableComponent(final View v) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.setEnabled(true);
            }
        });
    }

    public List<String[]> loadCaneObservationsById(String _ID) {
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        return databaseHelper.getCaneObservationById(_ID);
    }

    public String getRunEnvironment() {
        return runEnvironment;
    }

    public String getUser() {
        Context c = getActivity();
        return c.getSharedPreferences(c.getString(R.string.pref_file), Context.MODE_PRIVATE).getString(c.getString(R.string.username), null);
    }

    public String getBarcode() {
        return ((TextView) headerLayout.findViewById(R.id.barcode)).getText().toString();
    }

    public void registerSaveButton(View.OnClickListener listener) {
        buttonSave.setOnClickListener(listener);
    }

    @SuppressWarnings("SameParameterValue")
    public void showToastNotification(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void clearInputs(final ViewGroup container) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < container.getChildCount(); i++) {
                    View v = container.getChildAt(i);
                    if (v instanceof ViewGroup)
                        clearInputs((ViewGroup) v);
                    else {
                        if (isTextInput(v))
                            ((TextView) v).setText("");
                        else if (v instanceof Spinner)
                            ((Spinner) v).setSelection(0);
                        else if (v instanceof CheckBox)
                            ((CheckBox) v).setChecked(true);
                    }

                }
            }
        });
    }

    abstract void populateDataFields(List<String[]> data);
}