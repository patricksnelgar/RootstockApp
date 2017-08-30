package echo.rootstockapp.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

import echo.rootstockapp.DebugUtil;
import echo.rootstockapp.R;

public class DateSelectionDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private final String TAG = DateSelectionDialog.class.getSimpleName();
    private TextView activatorView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int day, month, year;
        // Initialize the date picker to have today's date unless otherwise edited.
        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        // args have been set giving initial date
        if (getArguments() != null) {
            try {
                String[] split = getArguments().getString(getString(R.string.start_date)).split("/");
                if (split != null) {
                    // parse the split date into Integer format
                    // format: dd/mm/yyyy (2/11/2017)
                    day = Integer.parseInt(split[0]);
                    month = Integer.parseInt(split[1]) - 1; // Months are 0 indexed
                    year = Integer.parseInt(split[2]);
                }
            } catch (Exception e) {
                new DebugUtil().logMessage(TAG, "Error parsing date: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, getString(R.string.run_environment));
            }
        }
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void setActivatorView(View v) {
        activatorView = (TextView) v;
    }

    @Override
    public void onDateSet(DatePicker dateView, int year, int month, int day) {

        // Increment as returned value is 0 indexed
        month++;
        updateView(day, month, year);
    }

    private void updateView(final int day, final int month, final int year) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String date = day + "/" + month + "/" + year;
                activatorView.setText(date);
            }
        });
    }


}