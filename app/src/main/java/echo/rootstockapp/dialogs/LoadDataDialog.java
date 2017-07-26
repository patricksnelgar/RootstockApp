package echo.rootstockapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import echo.rootstockapp.DbHelper;
import echo.rootstockapp.DebugUtil;
import echo.rootstockapp.R;

import static echo.rootstockapp.MainActivity.AUTHORIZATION_KEY;


public class LoadDataDialog extends DialogFragment implements DbHelper.DbProgressListener {

    private final String TAG = LoadDataDialog.class.getSimpleName();
    private final String all_observations_codes = "1,2,3,6,7";
    public static String ACTION_KEY = "action";
    public static String TITLE_KEY = "title";

    private DebugUtil debugUtil;
    private Dialog dialog;

    private String API_URL;
    private String run_environment;

    private Spinner spinnerSite;
    private Spinner spinnerBlock;
    private TextView textResponseMessage;
    private TextView textTitle;
    private ProgressBar progressBar;
    private TextView textProgressbarMessage;
    private RelativeLayout progressBarHolder;
    private boolean isFirstSelection = true; // Need this to prevent data loading when spinner is created
    private boolean lockResponses = false;
    private String site = null;
    private String block = null;
    private AsyncHttpClient asyncHttpClient;
    private DbHelper databaseHelper;

    private ACTIONS formAction;

    List<String> listSites = new ArrayList<String>(Arrays.asList("Choose a site"));
    List<String> listCodes = new ArrayList<String>(Arrays.asList("null"));
    List<String> listBlocks = new ArrayList<String>(Arrays.asList("Choose a block"));

    ArrayAdapter<String> adapterSites;
    ArrayAdapter<String> adapterBlocks;

    public enum ACTIONS {
        IDENTIFIERS,
        OBSERVATIONS
    }

    private OnIdentifierDataReceivedListener onIdentifierDataReceivedListener;

    public interface OnIdentifierDataReceivedListener {
        public boolean writeIdentifierData(File dataFile);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            onIdentifierDataReceivedListener = (OnIdentifierDataReceivedListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement method writeIdentifierData(File f)");
        }
    }

    public static LoadDataDialog newInstance(ACTIONS action, String title){
        LoadDataDialog d = new LoadDataDialog();
        Bundle argsObs = new Bundle();
        argsObs.putSerializable(LoadDataDialog.ACTION_KEY, action);
        argsObs.putString(LoadDataDialog.TITLE_KEY, title);
        d.setArguments(argsObs);

        return d;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.addHeader("Authorization", AUTHORIZATION_KEY);

        databaseHelper = new DbHelper(getActivity().getApplicationContext(), this);

        debugUtil = new DebugUtil();

        Context c = getActivity();
        SharedPreferences prefs = c.getSharedPreferences(c.getString(R.string.pref_file), Context.MODE_PRIVATE);

        run_environment = prefs.getString(c.getString(R.string.env), null);
        API_URL = prefs.getString(c.getString(R.string.api), null);

        dialog = super.onCreateDialog(savedInstanceState);
        dialog.setContentView(R.layout.load_data_layout);

        spinnerSite = (Spinner) dialog.findViewById(R.id.spinner_site_picker);
        spinnerBlock = (Spinner) dialog.findViewById(R.id.spinner_block_picker);
        textResponseMessage = (TextView) dialog.findViewById(R.id.text_response_message);
        progressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);
        progressBarHolder = (RelativeLayout) dialog.findViewById(R.id.progress_holder);
        textProgressbarMessage = (TextView) dialog.findViewById(R.id.text_progress_bar_label);
        textTitle = (TextView) dialog.findViewById(R.id.headerTitle);

        ((Button) dialog.findViewById(R.id.button_cancel)).setOnClickListener(onCancelClickListener);
        ((Button) dialog.findViewById(R.id.button_load)).setOnClickListener(onLoadDataClickListener);

        loadSites();
        
        adapterSites = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, listSites);
        adapterSites.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSite.setAdapter(adapterSites);
        spinnerSite.setOnItemSelectedListener(onSiteSelectedListener);

        
        adapterBlocks = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, listBlocks);
        adapterBlocks.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBlock.setAdapter(adapterBlocks);
        spinnerBlock.setOnItemSelectedListener(onBlockSelectedListerner);

        Bundle bundle = getArguments();
        if(bundle != null) {
            setDialogTitle(bundle.getString(TITLE_KEY, "Error"));
            formAction = (ACTIONS) bundle.getSerializable(ACTION_KEY);

        }


        return dialog;
    }

    private void loadSites(){
        // Can change to loading from API call
        hideResponses();
        setResponseTextNegative("Loading sites...");
        asyncHttpClient.get(API_URL+"/fdc/sites", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response){
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Got sites: " + responseString, run_environment);
                if(statusCode == 200 && responseString.length() > 0){
                    hideResponses();
                    try {
                        //JSONObject parsedResponse = new JSONObject(responseString);  
                        JSONArray tok = new JSONArray(responseString);
                        debugUtil.logMessage(TAG, "Array contains (" + tok.length() + ")", run_environment);
                        for(int i = 0; i < tok.length(); i++){
                            JSONObject _obj = tok.getJSONObject(i);
                            debugUtil.logMessage(TAG, "code: " + _obj.getString("code"), run_environment);
                            addSite(_obj.getString("code"), _obj.getString("name"));
                        }
                        //debugUtil.logMessage(TAG, "("+ parsedResponse.names().toString() + ")", run_environment);                 
                        //addSite(parsedResponse.getString("code"));
                    } catch(Exception e){
                        debugUtil.logMessage(TAG, "couldnt parse string into JSON: " + e.getLocalizedMessage(), run_environment);
                        setResponseTextNegative("Failed to load sites");
                        lockResponses = true;
                    }
                } else
                    setResponseTextNegative("Failed to load sites");
            }

            @Override 
            public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error){
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Failed with code: " + statusCode, DebugUtil.LOG_LEVEL_ERROR, run_environment);
                setResponseTextNegative("Failed to load sites");
            }
        });
    }

    private void addSite(String siteCode, String siteName){
        listCodes.add(siteCode);
        listSites.add(siteName);
        adapterSites.notifyDataSetChanged();
    }

    private void loadBlocksFromSiteCode(String siteCode){
        // Can change to loading from API call
        hideResponses();
        debugUtil.logMessage(TAG, "User wants data from: " + siteCode, run_environment);
        setResponseTextNegative("Loading blocks...");
        asyncHttpClient.get(API_URL+"/fdc/blocks?site=" + siteCode, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response){
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Got response: " + responseString, run_environment);
                if(statusCode == 200 && responseString.length() > 0){
                    hideResponses();
                    try {
                        //JSONObject parsedResponse = new JSONObject(responseString);  
                        JSONArray tok = new JSONArray(responseString);
                        debugUtil.logMessage(TAG, "Array contains (" + tok.length() + ")", run_environment);
                        for(int i = 0; i < tok.length(); i++){
                            JSONObject _obj = tok.getJSONObject(i);
                            debugUtil.logMessage(TAG, "code: " + _obj.getString("block"), run_environment);
                            addBlock(_obj.getString("block"));
                        }
                        //debugUtil.logMessage(TAG, "("+ parsedResponse.names().toString() + ")", run_environment);                 
                        //addSite(parsedResponse.getString("code"));
                    } catch(Exception e){
                        debugUtil.logMessage(TAG, "couldnt parse string into JSON: " + e.getLocalizedMessage(), run_environment);
                        setResponseTextNegative("Failed to load sites");
                    }
                } else
                    setResponseTextNegative("Failed to load sites");
            }

            @Override 
            public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error){
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "Failed with code: " + statusCode, DebugUtil.LOG_LEVEL_ERROR, run_environment);
                setResponseTextNegative("Failed to load sites");
            }
        });       
    }

    private void addBlock(String blockNum){
        listBlocks.add(blockNum);
        adapterBlocks.notifyDataSetChanged();
    }

    private void makeDataRequest(){
        if(site == null || block == null){
            setResponseTextNegative("Specify a site and a block to load data for.");
            return;
        }

        hideResponses();

        String url = API_URL + "/fdc/";
        //debugUtil.logMessage(TAG, "Form action is (" + formAction + ")", run_environment);
        switch(formAction){
            case IDENTIFIERS:
                url += "download/" + site + "/" + block;
                break;
            case OBSERVATIONS:
                url += "observations/" + site + "/" + block + "/" + all_observations_codes;
                break;
        }

        //debugUtil.logMessage(TAG, "URL is: <" + url + ">", run_environment);

        // build query for kakapo        
        asyncHttpClient.get(url, new AsyncHttpResponseHandler(){
            
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response){
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "onSuccess ["+statusCode+"]: response size<" + responseString.length() + ">", run_environment);
                if(statusCode == 200 && responseString.length() > 0){
                    debugUtil.logMessage(TAG, "Response is: <" + responseString + ">", run_environment);
                    try {
                        JSONObject json = new JSONObject(responseString);
                        String path = json.getString("path");
                        int rows = json.getInt("rows");
                        if(rows <= 0) {
                            setResponseTextNegative("No records found");

                        } else {
                            debugUtil.logMessage(TAG, "File path <" + path + ">", run_environment);
                            saveFile(path);
                        }
                    } catch (Exception e){
                        debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error){
                debugUtil.logMessage(TAG, "onFailure ["+statusCode+"]: response size<" + response.length + ">", run_environment);
            }
        });
        
    }

    private void saveFile(String path){

        setProgressText("Downloading data: ");        

        asyncHttpClient.get("http://dev.kakapo.pfr.co.nz:8600/"+path, new FileAsyncHttpResponseHandler(getActivity().getApplicationContext()){
            
            @Override
            public void onSuccess(int statusCode, Header[] headers, File response){
                debugUtil.logMessage(TAG, "File get code: " + statusCode, run_environment);
                
                if(statusCode == 200 && response!=null){
                    switch(formAction){
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
            public void onFailure(int statusCode, Header[] headers, Throwable error, File response){
                debugUtil.logMessage(TAG, "File get code: " + statusCode, DebugUtil.LOG_LEVEL_ERROR, run_environment);
            }

            @Override
            public void onProgress(long written, long size){
                updateProgress((int) written, (int)size);
            }
        });

    }

    private void setDialogTitle(final String title){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textTitle.setText(title);
            }
        });
    }

    private void hideResponses(){
        textResponseMessage.setVisibility(View.GONE);
        progressBarHolder.setVisibility(View.GONE);
    }
    
    @Override
    public void updateProgress(int progress, int total){
        progressBar.setMax(total);
        progressBar.setProgress(progress);
    }
    
    @Override
    public void setProgressText(final String t){
        if(lockResponses) return;
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
    public void setResponseTextPositive(final String message){
        if(lockResponses) return;
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
    public void setResponseTextNegative(final String message){
        if(lockResponses) return;
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
    public void onDestroy(){
        super.onDestroy();
        if(databaseHelper != null)
            databaseHelper.close();
    }

    final AdapterView.OnItemSelectedListener onSiteSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int index, long id) {
            debugUtil.logMessage(TAG, "User selected site at: <" + index + ">",  run_environment);
            // User selected the default 'no choice' option.
            if(index == 0) {
                site = null;
                return;
            }

            site =  listCodes.get(index);
            listBlocks.clear(); 
            listBlocks.add("Choose a block");      
            loadBlocksFromSiteCode(site);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    final AdapterView.OnItemSelectedListener onBlockSelectedListerner = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int index, long id){

            debugUtil.logMessage(TAG, "User selected block at: <" + index + ">",  run_environment);
            if(index == 0){
                block = null;
                return;
            }

            block = listBlocks.get(index);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent){

        }
    };

    final View.OnClickListener onCancelClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v){
            dialog.dismiss();
        }
    };

    final View.OnClickListener onLoadDataClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            makeDataRequest();
        }
    };
}