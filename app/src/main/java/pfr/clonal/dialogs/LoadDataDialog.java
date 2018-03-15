package pfr.clonal.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import pfr.clonal.DatabaseHelper;
import pfr.clonal.DebugUtil;
import pfr.clonal.R;


public class LoadDataDialog extends DialogFragment implements DatabaseHelper.DatabaseProgressListener {

    public static String ACTION_KEY = "action";
    public static String TITLE_KEY = "title";
    private final String TAG = LoadDataDialog.class.getSimpleName();    
    List<String> listSites = new ArrayList<>(Collections.singletonList("Choose a site"));
    List<String> listCodes = new ArrayList<>(Collections.singletonList("null"));
    List<String> listBlocks = new ArrayList<>(Collections.singletonList("Choose a block"));
    ArrayAdapter adapterSites;
    ArrayAdapter<String> adapterBlocks;
    private DebugUtil debugUtil;
    private Dialog dialog;
    final View.OnClickListener onCancelClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    };
    private String apiUrl;
    private String runEnvironment;
    private TextView textResponseMessage;
    private TextView textTitle;
    private ProgressBar progressBar;
    private TextView textProgressbarMessage;
    private RelativeLayout progressBarHolder;
    private boolean lockResponses = false;
    private String site = null;
    private String block = null;
    final AdapterView.OnItemSelectedListener onBlockSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int index, long id) {

            debugUtil.logMessage(TAG, "User selected block at: <" + index + ">", runEnvironment);
            if (index == 0) {
                block = null;
                return;
            }

            block = listBlocks.get(index);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private DatabaseHelper databaseHelper;
    private ACTIONS formAction;
    private AsyncHttpClient asyncHttpClient;
    final AdapterView.OnItemSelectedListener onSiteSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int index, long id) {
            debugUtil.logMessage(TAG, "User selected site at: <" + index + ">", runEnvironment);
            // User selected the default 'no choice' option.
            if (index == 0) {
                site = null;
                return;
            }

            site = listCodes.get(index);
            listBlocks.clear();
            listBlocks.add("Choose a block");
            loadBlocksFromSiteCode(site);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    final View.OnClickListener onLoadDataClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            makeDataRequest();
        }
    };

    public static LoadDataDialog newInstance(ACTIONS action, String title) {
        LoadDataDialog d = new LoadDataDialog();
        Bundle argsObs = new Bundle();
        argsObs.putSerializable(LoadDataDialog.ACTION_KEY, action);
        argsObs.putString(LoadDataDialog.TITLE_KEY, title);
        d.setArguments(argsObs);
        return d;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String authorizationKey = getActivity().getString(R.string.authorization_key);
        apiUrl = getActivity().getString(R.string.API_URL);
        runEnvironment = getActivity().getString(R.string.run_environment);

        asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.addHeader("Authorization", authorizationKey);

        databaseHelper = new DatabaseHelper(getActivity().getApplicationContext(), this);

        debugUtil = new DebugUtil();

        dialog = super.onCreateDialog(savedInstanceState);
        // Remove the annoying white space where the default header is
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.load_data_layout);


        Spinner spinnerSite = (Spinner) dialog.findViewById(R.id.spinner_site_picker);
        Spinner spinnerBlock = (Spinner) dialog.findViewById(R.id.spinner_block_picker);
        textResponseMessage = (TextView) dialog.findViewById(R.id.text_response_message);
        progressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);
        progressBarHolder = (RelativeLayout) dialog.findViewById(R.id.progress_holder);
        textProgressbarMessage = (TextView) dialog.findViewById(R.id.text_progress_bar_label);
        textTitle = (TextView) dialog.findViewById(R.id.headerTitle);

        dialog.findViewById(R.id.button_cancel).setOnClickListener(onCancelClickListener);
        dialog.findViewById(R.id.button_load).setOnClickListener(onLoadDataClickListener);

        loadSites();

        adapterSites = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listSites);
        adapterSites.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSite.setAdapter(adapterSites);
        spinnerSite.setOnItemSelectedListener(onSiteSelectedListener);


        adapterBlocks = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listBlocks);
        adapterBlocks.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBlock.setAdapter(adapterBlocks);
        spinnerBlock.setOnItemSelectedListener(onBlockSelectedListener);

        Bundle bundle = getArguments();
        if (bundle != null) {
            //setDialogTitle(bundle.getString(TITLE_KEY, "Error"));
            formAction = (ACTIONS) bundle.getSerializable(ACTION_KEY);
        }

        return dialog;
    }

    private void loadSites() {
        // Can change to loading from API call
        hideResponses();
        setResponseTextNegative("Loading sites...");
        asyncHttpClient.get(apiUrl + "/fdc/sites", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Got sites: " + responseString, runEnvironment);
                if (statusCode == 200 && responseString.length() > 0) {
                    hideResponses();
                    try {
                        //JSONObject parsedResponse = new JSONObject(responseString);
                        JSONArray tok = new JSONArray(responseString);
                        debugUtil.logMessage(TAG, "Array contains (" + tok.length() + ")", runEnvironment);
                        for (int i = 0; i < tok.length(); i++) {
                            JSONObject _obj = tok.getJSONObject(i);
                            debugUtil.logMessage(TAG, "code: " + _obj.getString("code"), runEnvironment);
                            addSite(_obj.getString("code"), _obj.getString("name"));
                        }
                        //debugUtil.logMessage(TAG, "("+ parsedResponse.names().toString() + ")", runEnvironment);
                        //addSite(parsedResponse.getString("code"));
                    } catch (Exception e) {
                        debugUtil.logMessage(TAG, "couldn't parse string into JSON: " + e.getLocalizedMessage(), runEnvironment);
                        setResponseTextNegative("Failed to load sites");
                        lockResponses = true;
                    }
                } else
                    setResponseTextNegative("Failed to load sites");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error) {
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Failed with code: " + statusCode + "\nMessage: " + responseString, DebugUtil.LOG_LEVEL_ERROR, runEnvironment);
                setResponseTextNegative("Failed to load sites");
            }
        });
    }

    private void hideResponses() {
        textResponseMessage.setVisibility(View.GONE);
        progressBarHolder.setVisibility(View.GONE);
    }

    private void addSite(String siteCode, String siteName) {
        listCodes.add(siteCode);
        listSites.add(siteName);
        adapterSites.notifyDataSetChanged();
    }

    private void setDialogTitle(final String title) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textTitle.setText(title);
            }
        });
    }

    private void loadBlocksFromSiteCode(String siteCode) {
        // Can change to loading from API call
        hideResponses();
        debugUtil.logMessage(TAG, "User wants data from: " + siteCode, runEnvironment);
        setResponseTextNegative("Loading blocks...");
        asyncHttpClient.get(apiUrl + "/fdc/blocks?site=" + siteCode, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Got response: " + responseString, runEnvironment);
                if (statusCode == 200 && responseString.length() > 0) {
                    hideResponses();
                    try {
                        //JSONObject parsedResponse = new JSONObject(responseString);
                        JSONArray tok = new JSONArray(responseString);
                        debugUtil.logMessage(TAG, "Array contains (" + tok.length() + ")", runEnvironment);
                        for (int i = 0; i < tok.length(); i++) {
                            JSONObject _obj = tok.getJSONObject(i);
                            debugUtil.logMessage(TAG, "code: " + _obj.getString("block"), runEnvironment);
                            addBlock(_obj.getString("block"));
                        }
                        //debugUtil.logMessage(TAG, "("+ parsedResponse.names().toString() + ")", runEnvironment);
                        //addSite(parsedResponse.getString("code"));
                    } catch (Exception e) {
                        debugUtil.logMessage(TAG, "couldn't parse string into JSON: " + e.getLocalizedMessage(), runEnvironment);
                        setResponseTextNegative("Failed to load sites");
                    }
                } else
                    setResponseTextNegative("Failed to load sites");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error) {
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Failed with code: " + statusCode + "\nMessage: " + responseString, DebugUtil.LOG_LEVEL_ERROR, runEnvironment);
                setResponseTextNegative("Failed to load sites");
            }
        });
    }

    private void addBlock(String blockNum) {
        listBlocks.add(blockNum);
        adapterBlocks.notifyDataSetChanged();
    }

    private void makeDataRequest() {
        if (site == null || block == null) {
            setResponseTextNegative("Specify a site and a block to load data for.");
            return;
        }

        hideResponses();

        String url = apiUrl + "/fdc/";
        //debugUtil.logMessage(TAG, "Form action is (" + formAction + ")", runEnvironment);
        switch (formAction) {
            case IDENTIFIERS:
                url += "download/" + site + "/" + block;
                break;
            case OBSERVATIONS:
                String all_observations_codes = "1,2,3,6,7";
                url += "observations/" + site + "/" + block + "/" + all_observations_codes;
                break;
        }

        //debugUtil.logMessage(TAG, "URL is: <" + url + ">", runEnvironment);

        // build query for Kakapo
        asyncHttpClient.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "onSuccess [" + statusCode + "]: response size<" + responseString.length() + ">", runEnvironment);
                if (statusCode == 200 && responseString.length() > 0) {
                    debugUtil.logMessage(TAG, "Response is: <" + responseString + ">", runEnvironment);
                    try {
                        JSONObject json = new JSONObject(responseString);
                        String path = json.getString("path");
                        int rows = json.getInt("rows");
                        if (rows <= 0) {
                            setResponseTextNegative("No records found");

                        } else {
                            debugUtil.logMessage(TAG, "File path <" + path + ">", runEnvironment);
                            saveFile(path);
                        }
                    } catch (Exception e) {
                        debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, runEnvironment);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error) {
                debugUtil.logMessage(TAG, "onFailure [" + statusCode + "]: response size<" + response.length + ">", runEnvironment);
            }
        });

    }

    private void saveFile(String path) {

        setProgressText("Downloading data: ");

        asyncHttpClient.get("http://dev.kakapo.pfr.co.nz:8600/" + path, new FileAsyncHttpResponseHandler(getActivity().getApplicationContext()) {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, File response) {
                debugUtil.logMessage(TAG, "File get code: " + statusCode, DebugUtil.LOG_LEVEL_ERROR, runEnvironment);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File response) {
                debugUtil.logMessage(TAG, "File get code: " + statusCode, runEnvironment);

                if (statusCode == 200 && response != null) {
                    switch (formAction) {
                        case IDENTIFIERS:
                            databaseHelper.insertIdentifiers(response);
                            break;
                        case OBSERVATIONS:
                            databaseHelper.insertObservations(response);
                            break;
                    }
                }
            }

            @Override
            public void onProgress(long written, long size) {
                updateProgress((int) written, (int) size);
            }
        });

    }

    @Override
    public void updateProgress(int progress, int total) {
        progressBar.setMax(total);
        progressBar.setProgress(progress);
    }

    @Override
    public void setProgressText(final String t) {
        if (lockResponses) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textResponseMessage.setVisibility(View.INVISIBLE);
                progressBarHolder.setVisibility(View.VISIBLE);
                textProgressbarMessage.setText(t);
            }
        });
    }

    @Override
    public void setResponseTextPositive(final String message) {
        if (lockResponses) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideResponses();
                textResponseMessage.setTextColor(getActivity().getResources().getColor(R.color.colorTextSuccess, null));
                textResponseMessage.setVisibility(View.VISIBLE);
                textResponseMessage.setText(message);
            }
        });

    }

    @Override
    public void setResponseTextNegative(final String message) {
        if (lockResponses) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideResponses();
                textResponseMessage.setTextColor(getActivity().getResources().getColor(R.color.colorTextError, null));
                textResponseMessage.setVisibility(View.VISIBLE);
                textResponseMessage.setText(message);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null)
            databaseHelper.close();
    }

    public enum ACTIONS {
        IDENTIFIERS,
        OBSERVATIONS
    }
}