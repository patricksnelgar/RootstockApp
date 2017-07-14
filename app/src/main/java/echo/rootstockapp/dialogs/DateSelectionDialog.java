package echo.rootstockapp.dialogs;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

public class DateSelectionDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private TextView activatorView;

    public DateSelectionDialog(View v){
        activatorView = (TextView) v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker dateView, int year, int month, int day){
        
        // Increment as returned value is 0 indexed
        month++;
        updateView(day,month,year);
    }

    private void updateView(final int day, final int month, final int year){

        getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run(){
                activatorView.setText(day+"/"+month+"/"+year);
            }
        });
    }
}