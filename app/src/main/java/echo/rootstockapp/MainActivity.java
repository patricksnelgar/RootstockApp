package echo.rootstockapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import echo.rootstockapp.dialogs.LoadDataDialog;
import echo.rootstockapp.dialogs.NetworkLoginDialog;
import echo.rootstockapp.forms.BudBreakFragment;
import echo.rootstockapp.forms.CaneInfoFragment;


/*
 * Entry point for the application, has a basic set of text views to hold info pulled from db.
 * Constructs and configures a scanner object for various barcode types.
 */

public class MainActivity extends AppCompatActivity implements AppLoginFragment.OnLoginVerifyListener, LoadDataDialog.OnIdentifierDataReceivedListener, ScannerManager.BarcodeFoundListener {
    
    private final String TAG = MainActivity.class.getSimpleName();

    public static String AUTHORIZATION_KEY ="ApiKey handheld:k7anf9hqphs0zjunodtlfgg3kozbt8lstufdsp2r257edvjr2d";
    
    private String API_URL;
    private String API_username;
    private String API_pw;
    private String run_environment;

    private int formIndex = -1;
    private int fragmentID = -1;

    private DebugUtil debugUtil = new DebugUtil();
    private ScannerManager scannerManager;

    private DbHelper databaseHelper;
    private Button buttonSave;

    private boolean dataEdit = false;
    private boolean lockMenu = true;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.app_name); 
        getSupportActionBar().setElevation(0f);
        
        if(!loadConfig()){
            Toast.makeText(this,"Could not load config, exiting.", Toast.LENGTH_LONG).show();
            finish();
        }  

        // If this is the first time running the app, load the PIN lock fragment
        if(savedInstanceState == null){
            loadFragment(new AppLoginFragment());
            invalidateOptionsMenu();            
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
    }

    private boolean loadConfig(){
        try {
            API_URL = getResources().getString(R.string.API_URL);
            API_username = getResources().getString(R.string.API_username);
            API_pw = getResources().getString(R.string.API_password);
            run_environment = getResources().getString(R.string.run_environment);

            SharedPreferences.Editor prefEditor = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE).edit();
            prefEditor.putString(getString(R.string.env), run_environment);
            prefEditor.putString(getString(R.string.api), API_URL);
            prefEditor.commit();

        } catch(Exception e){
            return false;
        }

        debugUtil.logMessage(TAG, "configuration loaded: URL <" +
            API_URL + "> User <" + API_username + "> PW <" + API_pw + "> ENV <" + run_environment + ">", run_environment);
        
        return true;
    }

    private void loadFragment(Fragment f){
        currentFragment = f;
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, currentFragment).commit();
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
                    getSupportActionBar().setTitle(formName);
                    loadFragment(new CaneInfoFragment());
                    fragmentID = 0;
                    break;
                case "Bud break / Flowering":
                    loadFragment(new BudBreakFragment());
                    getSupportActionBar().setTitle(formName);
                    fragmentID = 1;
                    break;
                default:
                    getSupportActionBar().setTitle("No Form");
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
        // App should not do anything if in the boot up lock screen
        if(lockMenu) return;

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
    public void onLoginVerify(boolean verified){
        lockMenu = !verified;
        if(verified){           
            // Load blank content so PIN screen is not shown again
            setContentView(R.layout.activity_main);
            showFormSelectionDialog();
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean writeIdentifierData(File f){
        debugUtil.logMessage(TAG, "user wants to write local db", run_environment);
        if(databaseHelper.insertIdentifiers(f)){
            return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.menu, menu);
        if(lockMenu){
            // Disable menu items that should not be accessible,
            // eg. PFR login
            menu.findItem(R.id.menu_load_identifiers).setEnabled(false);
            menu.findItem(R.id.menu_authenticate).setEnabled(false);
            menu.findItem(R.id.menu_change_form).setEnabled(false);
            menu.findItem(R.id.menu_load_observations).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_authenticate:
                debugUtil.logMessage(TAG,"User wants to authenticate", run_environment);
                NetworkLoginDialog n = new NetworkLoginDialog();
                n.show(getSupportFragmentManager(), "PFR_ Login");
                return true;
            case R.id.menu_load_identifiers:
                LoadDataDialog d = LoadDataDialog.newInstance(LoadDataDialog.ACTIONS.IDENTIFIERS, "Load identifiers");
                d.show(getSupportFragmentManager(), "Load identifiers");
                return true;
            case R.id.menu_change_form:
                showFormSelectionDialog();
                return true;
            case R.id.menu_load_observations:
                LoadDataDialog o = LoadDataDialog.newInstance(LoadDataDialog.ACTIONS.OBSERVATIONS, "Load observations");
                o.show(getSupportFragmentManager(), "Load observations");
                return true;
        }

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        // re-enable menu options once the app has been 'unlocked' via PIN
        menu.findItem(R.id.menu_authenticate).setEnabled(!lockMenu);
        menu.findItem(R.id.menu_load_identifiers).setEnabled(!lockMenu);
        menu.findItem(R.id.menu_change_form).setEnabled(!lockMenu);
        menu.findItem(R.id.menu_load_observations).setEnabled(!lockMenu);
        return true;
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