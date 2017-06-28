package echo.rootstockapp;

import android.R.layout;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.os.Bundle;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.json.JSONObject;


public class LoadDataDialog extends DialogFragment {

    private final String TAG = LoadDataDialog.class.getSimpleName();
    private final String authourizationKey = "ApiKey handheld:k7anf9hqphs0zjunodtlfgg3kozbt8lstufdsp2r257edvjr2d";

    private DebugUtil debugUtil;
    private Dialog dialog;

    private String API_URL;
    private String run_environment;

    private Spinner spinnerSite;
    private Spinner spinnerBlock;
    private TextView textResponseMessage;
    private ProgressBar progressBarQuery;
    private boolean isFirstSelection = true; // Need this to prevent data loading when spinner is created
    private String site = null;
    private String block = null;
    private AsyncHttpClient asyncHttpClient;

    List<String> listSites = new ArrayList<String>(Arrays.asList("Choose a site"));
    List<String> listBlocks = new ArrayList<String>(Arrays.asList("Choose a block"));

    ArrayAdapter<String> adapterSites;
    ArrayAdapter<String> adapterBlocks;

    private OnIdentifierDataReceivedListener onIdentifierDataReceivedListener;

    public interface OnIdentifierDataReceivedListener {
        public boolean writeIdentifierData(File dataFile);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            onIdentifierDataReceivedListener =(OnIdentifierDataReceivedListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement method writeIdentifierData(File f)");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.addHeader("Authorization", authourizationKey);

        debugUtil = new DebugUtil();

        dialog = super.onCreateDialog(savedInstanceState);
        dialog.setContentView(R.layout.load_data_layout);

        spinnerSite = (Spinner) dialog.findViewById(R.id.spinner_site_picker);
        spinnerBlock = (Spinner) dialog.findViewById(R.id.spinner_block_picker);
        textResponseMessage = (TextView) dialog.findViewById(R.id.text_response_message);
        progressBarQuery = (ProgressBar) dialog.findViewById(R.id.progress_bar_query);

        ((Button) dialog.findViewById(R.id.button_cancel)).setOnClickListener(onCancelClickListener);
        ((Button) dialog.findViewById(R.id.button_load)).setOnClickListener(onLoadDataClickListener);

        listSites.addAll(loadSites());
        
        adapterSites = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, listSites);
        adapterSites.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSite.setAdapter(adapterSites);
        spinnerSite.setOnItemSelectedListener(onSiteSelectedListener);

        
        adapterBlocks = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, listBlocks);
        adapterBlocks.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBlock.setAdapter(adapterBlocks);
        spinnerBlock.setOnItemSelectedListener(onBlockSelectedListerner);

        adapterSites.notifyDataSetChanged();
        adapterBlocks.notifyDataSetChanged();
        
        return dialog;
    }

    private ArrayList<String> loadSites(){
        // Can change to loading from API call
        textResponseMessage.setVisibility(View.INVISIBLE);
        String[] _t = getActivity().getResources().getStringArray(R.array.sites);
        if(_t.length > 0)
            return new ArrayList<String>(Arrays.asList(_t));
        else {
            textResponseMessage.setText("Error loading sites.");
            textResponseMessage.setVisibility(View.VISIBLE);
        }

        return new ArrayList<String>();
    }

    private ArrayList<String> loadBlocksFromSiteCode(String siteCode){
        // Can change to loading from API call
        textResponseMessage.setVisibility(View.INVISIBLE);
        debugUtil.logMessage(TAG, "User wants data from: " + siteCode, run_environment);
        switch(siteCode){
            case "TRC":
                String[] _t = getActivity().getResources().getStringArray(R.array.TRC_blocks);
                if(_t.length > 0)
                    return new ArrayList<String>(Arrays.asList(_t));
                else break;
            default:
                break;
        }

        debugUtil.logMessage(TAG, "No sites", run_environment);
        textResponseMessage.setText("Error loading blocks.");
        textResponseMessage.setVisibility(View.VISIBLE);
        return new ArrayList<String>();        
    }

    private void makeDataRequest(){
        if(site == null || block == null){
            textResponseMessage.setText("Specify a site and a block to load data for.");
            textResponseMessage.setVisibility(View.VISIBLE);
            return;
        }

        textResponseMessage.setVisibility(View.GONE);
        progressBarQuery.setVisibility(View.VISIBLE);

        // build query for kakapo        
        asyncHttpClient.get(API_URL + "/fdc/download/" + site + "/" + block, new AsyncHttpResponseHandler(){
            
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response){
                String responseString = new String(response);
                debugUtil.logMessage(TAG, "onSuccess ["+statusCode+"]: response size<" + responseString.length() + ">", run_environment);
                if(statusCode == 200 && responseString.length() > 0){
                    progressBarQuery.setVisibility(View.GONE);
                    debugUtil.logMessage(TAG, "Response is: <" + responseString + ">", run_environment);
                    try {
                    JSONObject json = new JSONObject(responseString);
                    String path = json.getString("path");
                    debugUtil.logMessage(TAG, "File path <" + path + ">", run_environment);
                    saveIdentifiers(path);
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

    private void saveIdentifiers(String path){

        asyncHttpClient.get("http://dev.kakapo.pfr.co.nz:8500/"+path, new FileAsyncHttpResponseHandler(getActivity().getApplicationContext()){
            
            @Override
            public void onSuccess(int statusCode, Header[] headers, File response){
                debugUtil.logMessage(TAG, "File get code: " + statusCode, run_environment);
                
                if(statusCode == 200 && response!=null){
                    onIdentifierDataReceivedListener.writeIdentifierData(response);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, File response){
                debugUtil.logMessage(TAG, "File get code: " + statusCode, DebugUtil.LOG_LEVEL_ERROR, run_environment);
            }
        });

    }

    public void setConfig(String api, String env){
        API_URL = api;
        run_environment = env;
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

            site =  listSites.get(index);; 
            listBlocks.clear(); 
            listBlocks.add("Choose a block");      
            listBlocks.addAll(loadBlocksFromSiteCode(site));
            adapterBlocks.notifyDataSetChanged();
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