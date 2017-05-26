package echo.rootstockapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import echo.rootstockapp.DbContract.DbObservations;
import com.honeywell.aidc.*;
import com.honeywell.aidc.AidcManager.CreatedCallback;
import com.honeywell.aidc.BarcodeReader.BarcodeListener;
import echo.rootstockapp.DbContract.DbIdentifiers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * Entry point for the application, has a basic set of text views to hold info pulled from db.
 * Constructs and configures a scanner object for various barcode types.
 */

public class MainActivity extends AppCompatActivity {
    final String TAG = "echo.rootstock";

    private String API_URL;
    private String API_username;
    private String API_pw;
    private String run_environment;

    private DebugUtil debugUtil = new DebugUtil();
    private boolean dev = false;

    private BarcodeReader barcodeReader;
    private AidcManager aidcManager;
    private DbHelper databaseHelper;
    private Button buttonSave;

    private boolean hasScanner = false;
    private boolean managerCreated = false;
    private boolean dataEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(R.string.app_name); 

        if(!loadConfig()){
            Toast.makeText(this,"Could not load config, exiting.", Toast.LENGTH_LONG).show();
            finish();
        }        

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100); //Any number
            }
        
        createAidcManager();

        initializeDb();
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
        
        if(run_environment.equals("DEV")){
            dev = true;
        }

        debugUtil.logMessage(TAG, "configuration loaded: URL <" +
            API_URL + "> User <" + API_username + "> PW <" + API_pw + "> ENV <" + run_environment + ">", dev);
        
        return true;
    }

    private void createAidcManager(){
        AidcManager.create(this, new CreatedCallback() {
            @Override
            public void onCreated(AidcManager am){
                             
                aidcManager = am;
                if(aidcManager != null){
                    managerCreated = true;
                }

                barcodeReader = aidcManager.createBarcodeReader();  
               
                if(barcodeReader!=null){
                    try{
                    
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                                                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
                        //barcodeReader.setProperty(BarcodeReader.PROPERTY_MICRO_PDF_417_ENABLED, true);

                        Map<String,Object> properties = new HashMap<String,Object>();
                        properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_MICRO_PDF_417_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, true);
                       // properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
                        properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);

                        barcodeReader.setProperties(properties);

                    } catch (Exception e){
                        debugUtil.logMessage(TAG, "Could not set property: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, dev);
                    }

                    hasScanner = claimScanner();
                    if(hasScanner){
                        regsiterBarcodeListener();
                    }
                }
                      
            }
        });
    }

    private boolean claimScanner(){
        try{
            if(barcodeReader != null){
                barcodeReader.claim();
            } else if (barcodeReader == null){
                debugUtil.logMessage(TAG, "reader is null", dev);
                barcodeReader = aidcManager.createBarcodeReader();
                barcodeReader.claim();
            }
        } catch (ScannerUnavailableException se){
            debugUtil.logMessage(TAG, "Could not claim scanner.", DebugUtil.LOG_LEVEL_ERROR, dev);
            return false;
        }

        return true;
    }

    private void regsiterBarcodeListener(){

        barcodeReader.addBarcodeListener(new BarcodeListener(){

            @Override
            public void onBarcodeEvent(BarcodeReadEvent event){
                debugUtil.logMessage(TAG,"Got barcode read event: " + event.getBarcodeData(), DebugUtil.LOG_LEVEL_INFO, dev);
                databaseLookup(event.getBarcodeData());
            }

            public void onFailureEvent(BarcodeFailureEvent fevent){
                debugUtil.logMessage(TAG, "Barcode failure event", DebugUtil.LOG_LEVEL_ERROR, dev);
            }
        });
    }

    private void initializeDb(){
        databaseHelper = new DbHelper(getApplicationContext());
        //SQLiteDatabase db = databaseHelper.getWritableDatabase();
        //db.execSQL("DROP FROM observations");
        //db.execSQL("DROP TABLE " + DbObservations.OBSERVATIONS_TABLE_NAME);
        //db.execSQL("DELETE FROM identifiers");
        //db.execSQL(DbContract.DUMMY_DATA);
    }

    public void databaseLookup(String barcode){
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        
        String[] columns = {
            DbIdentifiers._ID,
            DbIdentifiers.IDENTIFIERS_BARCODE_TITLE,
            DbIdentifiers.IDENTIFIERS_TYPE_TITLE,
            DbIdentifiers.IDENTIFIERS_SITE_TITLE,
            DbIdentifiers.IDENTIFIERS_BLOCK_TITLE,
            DbIdentifiers.IDENTIFIERS_FPI_TITLE,
            DbIdentifiers.IDENTIFIERS_CULTIVAR_TITLE,
            DbIdentifiers.IDENTIFIERS_GRAFT_YEAR_TITLE

        };

        String columnFilter = DbIdentifiers.IDENTIFIERS_BARCODE_TITLE + " = ?";
        String[] columnValues = {barcode};

        Cursor c = db.query(
            DbIdentifiers.IDENTIFIERS_TABLE_NAME,
            columns,
            columnFilter,
            columnValues,
            null,
            null,
            null
        );

        debugUtil.logMessage(TAG, "Got rows from DB:" + c.getCount(), dev);
        if(c.getCount() != 1 && c.getCount() > 0){
            debugUtil.logMessage(TAG, "Got too many rows from db", DebugUtil.LOG_LEVEL_ERROR, dev);            
            return;
        }
        try {
            c.moveToNext();
            updateIdentifierFields(c);
        } catch (Exception e){
            Log.e(TAG, e.getLocalizedMessage());
        }

        columns = new String[]{
            DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE,
            DbObservations.OBSERVATIONS_VALUE_TITLE
        };

        columnFilter = DbObservations.OBSERVATIONS_CANE_ID_TITLE + " = ?";
        columnValues = new String[]{c.getString(c.getColumnIndexOrThrow(DbIdentifiers._ID))};

        final Cursor cur = db.query(
            DbObservations.OBSERVATIONS_TABLE_NAME,
            columns,
            columnFilter,
            columnValues,
            null,
            null,
            null
        );

        if(cur.getCount() > 0){
            dataEdit = true;
            debugUtil.logMessage(TAG,"Got " + cur.getCount() + " observations for cane id: " + columnValues[0], dev);
            final MeasurementText caneLengthText = (MeasurementText) findViewById(R.id.cane_length);
            final MeasurementText caneDiameterText = (MeasurementText) findViewById(R.id.cane_diameter);
            final String caneLengthId = caneLengthText.getMeasurementId();
            final String caneDiameterId = caneDiameterText.getMeasurementId();
            
            

            for(int i = 0; i < cur.getCount(); i++){
                cur.moveToNext();
                String measureId = cur.getString(cur.getColumnIndexOrThrow(DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE));
                final String measureValue = cur.getString(cur.getColumnIndexOrThrow(DbObservations.OBSERVATIONS_VALUE_TITLE));

                if(measureId.equals(caneLengthId)){ 
                    debugUtil.logMessage(TAG, "Index: " + i + " ID <" + measureId + "> length ID <" + caneLengthId +
                            "> = " + measureValue, DebugUtil.LOG_LEVEL_INFO, dev);
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            caneLengthText.setText(measureValue);
                        }
                    });                    
                }else if(measureId.equals(caneDiameterId)){
                    debugUtil.logMessage(TAG, "Index: " + i + " ID <" + measureId + "> diameter ID <" + caneDiameterId +
                        "> = " + measureValue, DebugUtil.LOG_LEVEL_INFO, dev);
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            caneDiameterText.setText(measureValue);
                        }
                    });                    
                }
                
            }         
        } else {
            dataEdit = false;
        }
    }

    public void saveData(View v){
       debugUtil.logMessage(TAG, "Is edit? " + dataEdit, dev);

        MeasurementText caneLengthText = (MeasurementText) findViewById(R.id.cane_length);
        MeasurementText caneDiameterText = (MeasurementText) findViewById(R.id.cane_diameter);

        final String caneLengthMeasurementId = caneLengthText.getMeasurementId();
        final String caneDiameterMeasurementId = caneDiameterText.getMeasurementId();

        String caneDiameterMeasurement = caneDiameterText.getText().toString();
        String caneLengthMeasurement = caneLengthText.getText().toString();
        String caneId = ((TextView) findViewById(R.id.id)).getText().toString();
        
        Date date = new Date();

        String metaData = "{'Date':'" + DateFormat.format("dd/mm/yyyy",date) + "','Time':'" + DateFormat.format("hh:mm",date) + "','User':'Garfield'}";

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        if(dataEdit){
            debugUtil.logMessage(TAG,"Updating values for cane " + caneId, dev);
            String queryLength = "UPDATE " + DbObservations.OBSERVATIONS_TABLE_NAME + " SET " + DbObservations.OBSERVATIONS_VALUE_TITLE + 
            " = " + caneLengthMeasurement + ", " +  DbObservations.OBSERVATIONS_METADATA_TITLE + " = \"" + metaData +
            "\" WHERE " + DbObservations.OBSERVATIONS_CANE_ID_TITLE + "=" + caneId + " AND " + 
            DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE + "=" + caneLengthMeasurementId;

            String queryDiameter = "UPDATE " + DbObservations.OBSERVATIONS_TABLE_NAME + " SET " + DbObservations.OBSERVATIONS_VALUE_TITLE + 
            " = " + caneDiameterMeasurement + ", " +  DbObservations.OBSERVATIONS_METADATA_TITLE + " = \"" + metaData +
            "\" WHERE " + DbObservations.OBSERVATIONS_CANE_ID_TITLE + "=" + caneId + " AND " + 
            DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE + "=" + caneDiameterMeasurementId;

            try{
                db.execSQL(queryLength);
                db.execSQL(queryDiameter);
            } catch (Exception e){
                debugUtil.logMessage(TAG, "Error updating " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, dev);
                return;
            }

            debugUtil.logMessage(TAG, "Data has been updated for cane " + caneId, dev);
        } else{
            debugUtil.logMessage(TAG,"Saving new data", DebugUtil.LOG_LEVEL_INFO, dev);
            ContentValues values = new ContentValues();

            // construct caneLength observation
            values.put(DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE,caneLengthText.getMeasurementId());
            values.put(DbObservations.OBSERVATIONS_CANE_ID_TITLE, caneId);
            values.put(DbObservations.OBSERVATIONS_VALUE_TITLE, caneLengthMeasurement);
            values.put(DbObservations.OBSERVATIONS_METADATA_TITLE, metaData);
            values.put(DbObservations.OBSERVATIONS_CHANGED_TITLE, true);

            long rowId = db.insert(DbObservations.OBSERVATIONS_TABLE_NAME, null, values);

            debugUtil.logMessage(TAG, "Saved length: " + rowId, dev);

            values.put(DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE, caneDiameterText.getMeasurementId());
            values.put(DbObservations.OBSERVATIONS_VALUE_TITLE, caneDiameterMeasurement);

            rowId = -1;
            rowId = db.insert(DbObservations.OBSERVATIONS_TABLE_NAME, null, values);

            debugUtil.logMessage(TAG, "Saved diameter: " + rowId, dev);
        }
    }

    public void copyDatabase(View v){
       debugUtil.logMessage(TAG, "Try to copy db", dev);
        try{
            File sd = Environment.getExternalStorageDirectory();
            File internal = Environment.getDataDirectory();

            if(sd.canWrite()){
                debugUtil.logMessage(TAG, "Can write", dev);
                debugUtil.logMessage(TAG, "Data path: " + internal.toString(), dev);
                File deviceDb = new File(internal,"/user/0/echo.rootstockapp/databases/HandHeld.db");
                File newDb = new File("/sdcard/HandHeld.db");

                if(deviceDb.exists()){
                   debugUtil.logMessage(TAG, "Db exists", dev);
                    FileChannel src = new FileInputStream(deviceDb).getChannel();
                    FileChannel dst = new FileOutputStream(newDb).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            } 
        } catch (Exception e){
            debugUtil.logMessage(TAG, "Error copying Db: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, dev);
        }
    }

    public void updateIdentifierFields(final Cursor c){
        final TextView textId = (TextView) findViewById(R.id.id);
        final TextView textBarcode = (TextView) findViewById(R.id.barcode);
        final TextView textType = (TextView) findViewById(R.id.type);
        final TextView textSite = (TextView) findViewById(R.id.site);
        final TextView textBlock = (TextView) findViewById(R.id.block);
        final TextView textFPI = (TextView) findViewById(R.id.FPI);
        final TextView textCultivar = (TextView) findViewById(R.id.cultivar);
        final TextView textGraftYear = (TextView) findViewById(R.id.graftyear);

        final long caneId = c.getLong(c.getColumnIndexOrThrow(DbIdentifiers._ID));
        final String barcode = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_BARCODE_TITLE));
        final String type = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_TYPE_TITLE));
        final String site = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_SITE_TITLE));
        final String block = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_BLOCK_TITLE));
        final String FPI = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_FPI_TITLE));
        final String cultivar = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_CULTIVAR_TITLE));
        final String graftYear = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_GRAFT_YEAR_TITLE));

       
        runOnUiThread(new Runnable() {
            @Override
            public void run(){
                ((MeasurementText) findViewById(R.id.cane_length)).setText("");
                ((MeasurementText) findViewById(R.id.cane_diameter)).setText("");
                textId.setText(Long.toString(caneId));
                textBarcode.setText(barcode);
                textType.setText(type);
                textSite.setText(site);
                textBlock.setText(block);
                textFPI.setText(FPI);
                textCultivar.setText(cultivar);
                textGraftYear.setText(graftYear);
            }
        });
        
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(barcodeReader != null){
            barcodeReader.release();
        }

        if(aidcManager != null){
            aidcManager.close();
        }

        if(databaseHelper != null){
            databaseHelper.close();
        }
    }
}