package echo.rootstockapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

public class MeasurementText extends EditText {

    private String measurementId;

    public MeasurementText (Context context, AttributeSet att){
        super(context, att);
        TypedArray a = context.getTheme().obtainStyledAttributes(att, R.styleable.MeasurementText, 0, 0);
        try{
            measurementId = a.getString(R.styleable.MeasurementText_measurement_id);
        } finally {
            a.recycle();
        }
    }

    public String getMeasurementId(){
        return measurementId;
    }
}