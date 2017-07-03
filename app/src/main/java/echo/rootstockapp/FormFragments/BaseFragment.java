package echo.rootstockapp;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BaseFragment extends Fragment {

    protected View inflateFragment(int resId, LayoutInflater inflator, ViewGroup container) {
        View v = inflator.inflate(resId, container, false);
        return v;
    }
}