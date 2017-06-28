package echo.rootstockapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
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
import echo.rootstockapp.LoadDataDialog.OnIdentifierDataReceivedListener;
import java.io.File;

/*
 * Entry point for the application, has a basic set of text views to hold info pulled from db.
 * Constructs and configures a scanner object for various barcode types.
 */

public class MainActivity extends AppCompatActivity implements AppLoginFragment.OnLoginVerifyListener, LoadDataDialog.OnIdentifierDataReceivedListener{
    final String TAG = "echo.rootstock";

    private String API_URL;
    private String API_username;
    private String API_pw;
    private String run_environment;

    private int formIndex = -1;

    private DebugUtil debugUtil = new DebugUtil();
    private ScannerManager scannerManager;

    private DbHelper databaseHelper;
    private Button buttonSave;

    private boolean dataEdit = false;
    private boolean lockMenu = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.app_name); 

        if(!loadConfig()){
            Toast.makeText(this,"Could not load config, exiting.", Toast.LENGTH_LONG).show();
            finish();
        }  

        // If this is the first time running the app, load the PIN lock fragment
        if(savedInstanceState == null){
            loadFragment(new AppLoginFragment());
            invalidateOptionsMenu();            
        }

        scannerManager = new ScannerManager(getApplicationContext(), debugUtil, run_environment);
        databaseHelper = new DbHelper(getApplicationContext(), run_environment);

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
        } catch(Exception e){
            return false;
        }

        debugUtil.logMessage(TAG, "configuration loaded: URL <" +
            API_URL + "> User <" + API_username + "> PW <" + API_pw + "> ENV <" + run_environment + ">", run_environment);
        
        return true;
    }

    private void loadFragment(Fragment f){
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f).commit();
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
                    loadFragment(new CaneInfoFragment());
                    break;
                default:
                    setContentView(R.layout.activity_main);
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
            menu.findItem(R.id.menu_load_data).setEnabled(false);
            menu.findItem(R.id.menu_authenticate).setEnabled(false);
            menu.findItem(R.id.menu_change_form).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_authenticate:
                debugUtil.logMessage(TAG,"User wants to authenticate", run_environment);
                NetworkLoginDialog n = new NetworkLoginDialog();
                n.setConfig(API_URL, run_environment);
                n.show(getSupportFragmentManager(), "PFR_ Login");
                return true;
            case R.id.menu_load_data:
                LoadDataDialog d = new LoadDataDialog();
                d.setConfig(API_URL, run_environment);
                d.show(getSupportFragmentManager(), "Load data");
                break;
            case R.id.menu_change_form:
                showFormSelectionDialog();
                return true;
        }

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        // re-enable menu options once the app has been 'unlocked' via PIN
        menu.findItem(R.id.menu_authenticate).setEnabled(!lockMenu);
        menu.findItem(R.id.menu_load_data).setEnabled(!lockMenu);
        menu.findItem(R.id.menu_change_form).setEnabled(!lockMenu);
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