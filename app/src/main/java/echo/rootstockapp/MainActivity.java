package echo.rootstockapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import echo.rootstockapp.dialogs.LoadDataDialog;
import echo.rootstockapp.dialogs.NetworkLoginDialog;
import echo.rootstockapp.forms.BudBreakFragment;
import echo.rootstockapp.forms.CaneInfoFragment;


/*
 * Entry point for the application, displays the app login fragment on startup,
 * Loads config from file
 */

public class MainActivity extends Activity implements ScannerManager.BarcodeFoundListener {
    
    private final String TAG = MainActivity.class.getSimpleName();

    private String API_URL;
    private String run_environment;

    private int formIndex = -1;
    private int fragmentID = -1;

    private DebugUtil debugUtil = new DebugUtil();
    private ScannerManager scannerManager;

    private DbHelper databaseHelper;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.app_name);
        getActionBar().setElevation(0f);
        
        if(!loadConfig()){
            Toast.makeText(this,"Could not load config, exiting.", Toast.LENGTH_LONG).show();
            finish();
        }  

        scannerManager = new ScannerManager(getApplicationContext(), this);
        databaseHelper = new DbHelper(getApplicationContext());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100); //Any number
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET}, 101); //Any number
        }

        showFormSelectionDialog();
    }

    private boolean loadConfig(){
        try {
            API_URL = getResources().getString(R.string.API_URL);
            run_environment = getResources().getString(R.string.run_environment);

            SharedPreferences.Editor prefEditor = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE).edit();
            prefEditor.putString(getString(R.string.env), run_environment);
            prefEditor.putString(getString(R.string.api), API_URL);
            prefEditor.commit();

        } catch(Exception e){
            return false;
        }

        debugUtil.logMessage(TAG, "configuration loaded: API URL = <" +
                API_URL + ">, run environment = <" + run_environment + ">", run_environment);
        
        return true;
    }

    private void loadFragment(Fragment f){
        currentFragment = f;
        getFragmentManager().beginTransaction().replace(android.R.id.content, currentFragment).commit();
    }

    public void changeForm(){
        if(formIndex < 0){
            debugUtil.logMessage(TAG, "Form index invalid", run_environment);
            return;
        }
        try{                
            String[] formArray = getResources().getStringArray(R.array.forms);
            String formName = formArray[formIndex];
            debugUtil.logMessage(TAG,"User wants to load: <" + formName + ">", run_environment);
            switch(formName){
                case "Cane info":
                    getActionBar().setTitle(formName);
                    loadFragment(new CaneInfoFragment());
                    fragmentID = 0;
                    break;
                case "Bud break / Flowering":
                    loadFragment(new BudBreakFragment());
                    getActionBar().setTitle(formName);
                    fragmentID = 1;
                    break;
                default:
                    getActionBar().setTitle("No Form");
                    setContentView(R.layout.activity_main);
                    break;
            }
        } catch (Exception e){
            debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
        }
    }

    private void showFormSelectionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Form selection")
                .setSingleChoiceItems(R.array.forms, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index){
                        debugUtil.logMessage(TAG, "User selected: <" + index + ">", run_environment);
                        setFormIndex(index);
                    }
                });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id){
                debugUtil.logMessage(TAG, "User clicked: " + id, run_environment);
                changeForm();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                debugUtil.logMessage(TAG,"User clicked: " + id, run_environment);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void setFormIndex(int index) {
        debugUtil.logMessage(TAG, "Index changed: <" + formIndex+"> -> <" + index +">", run_environment);
        formIndex = index;
    }

    @Override
    public void onBarcodeFound(final String barcode){

        debugUtil.logMessage(TAG, "Looking up barcode: <" + barcode + ">", run_environment);
        List<String> identifier =  databaseHelper.lookupIdentifier(barcode);
        if(identifier == null) {
            runOnUiThread(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext(), "Could not find barcode " + barcode, Toast.LENGTH_SHORT).show();
                }
            });            
            return;
        }
        debugUtil.logMessage(TAG, "Identifier found (" + identifier.toString() + ")", run_environment);

        switch(fragmentID){
            case -1: return;
            case 0:
                ((CaneInfoFragment) currentFragment).onBarcodeFound(identifier);
                break;
            case 1:
                ((BudBreakFragment) currentFragment).onBarcodeFound(identifier);
                break;
            default:
                return;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        debugUtil.logMessage(TAG, "saving state", run_environment);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.menu, menu);

        debugUtil.logMessage(TAG, "run_environment == (" + run_environment + ")", run_environment);

        // only want to show this option if in development mode
        if (run_environment != null) {
            switch (run_environment) {
                case "DEV":
                    menu.findItem(R.id.menu_clear_db).setVisible(true);
                    break;
                default:
                    menu.findItem(R.id.menu_clear_db).setVisible(false);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_authenticate:
                debugUtil.logMessage(TAG,"User wants to authenticate", run_environment);
                NetworkLoginDialog n = new NetworkLoginDialog();
                n.show(getFragmentManager(), "PFR_ Login");
                return true;
            case R.id.menu_load_identifiers:
                LoadDataDialog d = LoadDataDialog.newInstance(LoadDataDialog.ACTIONS.IDENTIFIERS, "Load identifiers");
                d.show(getFragmentManager(), "Load identifiers");
                return true;
            case R.id.menu_change_form:
                showFormSelectionDialog();
                return true;
            case R.id.menu_load_observations:
                LoadDataDialog o = LoadDataDialog.newInstance(LoadDataDialog.ACTIONS.OBSERVATIONS, "Load observations");
                o.show(getFragmentManager(), "Load observations");

                return true;
            case R.id.menu_clear_db:
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                try {
                    db.beginTransaction();
                    db.execSQL("DELETE FROM " + DbContract.DbObservations.OBSERVATIONS_TABLE_NAME);
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
                } finally {
                    db.endTransaction();
                }
                return true;
        }

        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        scannerManager.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        scannerManager = new ScannerManager(getApplicationContext(), this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        scannerManager.onDestroy();

        if(databaseHelper != null){
            databaseHelper.close();
        }
    }
}