package echo.rootstockapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import echo.rootstockapp.DbContract.DbCaneIdentifiers;
import echo.rootstockapp.DbContract.DbComponentIdentifiers;
import echo.rootstockapp.DbContract.DbObservations;
import echo.rootstockapp.DbContract.IdentifierColumnNames;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DATABASAE_NAME = "HandHeld.db";
    private static final int DATABASE_VERSION = 11;
    private final String TAG = DbHelper.class.getSimpleName();
    private String run_environment;
    private DebugUtil debugUtil;
    private boolean dataEdit = false;

    private DbProgressListener dbProgressListener;

    public DbHelper(Context context, DbProgressListener l) {
        super(context, DATABASAE_NAME, null, DATABASE_VERSION);
        run_environment = context.getSharedPreferences(context.getString(R.string.pref_file), Context.MODE_PRIVATE).getString(context.getString(R.string.env), null);
        debugUtil = new DebugUtil();
        dbProgressListener = l;
    }

    public DbHelper(Context context) {
        super(context, DATABASAE_NAME, null, DATABASE_VERSION);
        run_environment = run_environment = context.getSharedPreferences(context.getString(R.string.pref_file), Context.MODE_PRIVATE).getString(context.getString(R.string.env), null);
        debugUtil = new DebugUtil();
    }

    public void insertIdentifiers(final File f){

        dbProgressListener.setProgressText("Writing to DB:");
        final SQLiteDatabase db = this.getWritableDatabase();

        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    int maxCount = getNumLinesInFile(f);
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    String[] headers = br.readLine().split(",");
                    debugUtil.logMessage(TAG, "Column headers: <" + Arrays.toString(headers) + ">", run_environment);

                    db.beginTransaction();

                    ContentValues _v = new ContentValues();

                    long startTime = System.currentTimeMillis();

                    int count = 0;
                    long rowID;
                    String line;

                    while(true){
                        line = br.readLine();
                        if(line == null) break;
                        String[] input = line.split(",",-1);

                        _v.put(IdentifierColumnNames._ID, input[0]);
                        _v.put(IdentifierColumnNames.BARCODE_TITLE, input[1]);
                        _v.put(IdentifierColumnNames.TYPE_TITLE, input[2]);
                        _v.put(IdentifierColumnNames.SITE_TITLE, input[3]);
                        _v.put(IdentifierColumnNames.BLOCK_TITLE, input[4]);
                        _v.put(IdentifierColumnNames.FPI_TITLE, input[5]);
                        _v.put(IdentifierColumnNames.CULTIVAR_TITLE, input[6]);
                        _v.put(IdentifierColumnNames.GRAFT_YEAR_TITLE, input[7]);



                        switch (input[2]){
                            case "cane":
                                rowID = db.insert(DbCaneIdentifiers.TABLE_NAME, null, _v);
                                if(rowID!=-1) count++;
                                break;
                            case "component":
                                rowID = db.insert(DbComponentIdentifiers.TABLE_NAME, null, _v);
                                if(rowID!=-1) count++;
                                break;
                        }
                        dbProgressListener.updateProgress(count, maxCount);
                    }
                    db.setTransactionSuccessful();
                    long endTime = System.currentTimeMillis();
                    debugUtil.logMessage(TAG, "time taken to insert (" + count + " / " + (maxCount-1) +") records: " + (endTime - startTime) + "ms", run_environment);
                    if(count != maxCount-1){
                        dbProgressListener.setResponseTextNegative("Failed to insert all records");
                    } else {
                        dbProgressListener.setResponseTextPositive("Done: (" + count + ") records loaded.");
                    }
                } catch (Exception e){
                    debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
                } finally {
                    db.endTransaction();
                }
            }
        }).start();

    }

    public void insertObservations(final File file){
        dbProgressListener.setProgressText("Inserting observations: ");
        final SQLiteDatabase db = this.getWritableDatabase();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    int maxCount = getNumLinesInFile(file);
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String[] headers = br.readLine().split(",");
                    debugUtil.logMessage(TAG, "Column headers: <" + Arrays.toString(headers) + ">", run_environment);

                    db.beginTransaction();

                    ContentValues _v = new ContentValues();

                    long startTime = System.currentTimeMillis();

                    int count = 0;
                    long rowID;
                    String line;

                    while (true) {
                        line = br.readLine();
                        if (line == null) break;
                        String[] input = line.split(",");
                        debugUtil.logMessage(TAG, "Line is <" + Arrays.toString(input) + ">", run_environment);

                        _v.put(DbObservations.OBSERVATIONS_VINE_SITE_TITLE, input[0]);
                        _v.put(DbObservations.OBSERVATIONS_COMPONENT_ID_TITLE, input[2]);
                        _v.put(DbObservations.OBSERVATIONS_CANE_ID_TITLE, input[4]);
                        _v.put(DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE, input[6]);
                        _v.put(DbObservations.OBSERVATIONS_VALUE_TITLE, input[8]);
                        _v.put(DbObservations.OBSERVATIONS_CHANGED_TITLE, false);


                        // This will NOT handle checking for an already existing observation against the same measure ID and cane / component ID
                        rowID = db.insert(DbObservations.OBSERVATIONS_TABLE_NAME, null, _v);
                        if (rowID != -1) count++;

                        dbProgressListener.updateProgress(count, maxCount);
                    }
                    db.setTransactionSuccessful();
                    long endTime = System.currentTimeMillis();
                    debugUtil.logMessage(TAG, "time taken to insert (" + count + " / " + (maxCount - 1) + ") records: " + (endTime - startTime) + "ms", run_environment);
                    if (count != maxCount - 1) {
                        dbProgressListener.setResponseTextNegative("Failed to insert all records");
                    } else {
                        dbProgressListener.setResponseTextPositive("Done: (" + count + ") records loaded.");
                    }
                } catch (Exception e){
                    debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
                } finally {
                    db.endTransaction();
                }
            }
        }).start();
    }

    public List<String> lookupIdentifier(String barcode){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
            IdentifierColumnNames._ID,
            IdentifierColumnNames.BARCODE_TITLE,
            IdentifierColumnNames.TYPE_TITLE,
            IdentifierColumnNames.SITE_TITLE,
            IdentifierColumnNames.BLOCK_TITLE,
            IdentifierColumnNames.FPI_TITLE,
            IdentifierColumnNames.CULTIVAR_TITLE,
            IdentifierColumnNames.GRAFT_YEAR_TITLE

        };

        String columnFilter = IdentifierColumnNames.BARCODE_TITLE + " = ?";
        String[] columnValues = {barcode};

        Cursor c;
        c = lookupCane(db, columns, columnFilter, columnValues);
        if(c == null) c = lookupComponent(db, columns, columnFilter, columnValues);

        if(c != null){
            try {
                c.moveToNext();
                return buildIdentifierReturnObject(c);
            } catch (Exception e){
                debugUtil.logMessage(TAG, "Error: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
            }
        }

        debugUtil.logMessage(TAG, "Failed to find barcode <" + barcode + "> in DB", DebugUtil.LOG_LEVEL_ERROR, run_environment);
        db.close();
        return null;
    }

    public List<String[]> getCaneObservationById(String _ID) {

        SQLiteDatabase db = this.getReadableDatabase();
        List<String[]> observations = new ArrayList<>();
        String[] columns = new String[]{
                DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE,
                DbObservations.OBSERVATIONS_VALUE_TITLE
        };

        String columnFilter = DbObservations.OBSERVATIONS_CANE_ID_TITLE + " = ?";
        String[] columnValues = new String[]{_ID};

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
            debugUtil.logMessage(TAG,"Got " + cur.getCount() + " observations for cane id: " + columnValues[0], run_environment);

            for(int i = 0; i < cur.getCount(); i++){
                cur.moveToNext();
                String[] _data = new String[2];
                _data[0] = cur.getString(cur.getColumnIndexOrThrow(DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE));
                _data[1] = cur.getString(cur.getColumnIndexOrThrow(DbObservations.OBSERVATIONS_VALUE_TITLE));
                observations.add(_data);
                debugUtil.logMessage(TAG, "Observation loaded: id (" + _data[0] + ") value (" + _data[1] + ")", run_environment);
            }

            db.close();
            return observations;
        } else {
            debugUtil.logMessage(TAG, "No observatiosn found for ID (" + _ID + ")", run_environment);
            db.close();
            return null;
        }

    }

    private Cursor lookupCane(SQLiteDatabase database, String[] columns, String filter, String[] values){
        Cursor _t = database.query(
            DbContract.DbCaneIdentifiers.TABLE_NAME,
            columns,
            filter,
            values,
            null,
            null,
            null);

        debugUtil.logMessage(TAG, "Retrieved (" + _t.getCount() + ") rows from cane table", run_environment);
        if(_t.getCount() == 1) return _t;
        else return null;
    }

    private Cursor lookupComponent(SQLiteDatabase database, String[] columns, String filter, String[] values){
        Cursor _t = database.query(
            DbContract.DbComponentIdentifiers.TABLE_NAME,
            columns,
            filter,
            values,
            null,
            null,
            null);

        debugUtil.logMessage(TAG, "Retrieved (" + _t.getCount() + ") rows from components table", run_environment);
        if(_t.getCount() == 1) return _t;
        else return null;
    }

    private List<String> buildIdentifierReturnObject(Cursor _c){
        List<String> _l = new ArrayList<String>();
        for(int index = 0; index < _c.getColumnCount(); index++)
            _l.add(_c.getString(index));

        return _l;
    }

    public boolean saveCaneData(List<String> data){

        SQLiteDatabase db = this.getWritableDatabase();

        debugUtil.logMessage(TAG, "User wants to save data: (" + data.toString() + ")", run_environment);

        return false;
    }

    private int getNumLinesInFile(final File file){
        try {
            LineNumberReader lr = new LineNumberReader(new FileReader(file));
            lr.skip(Long.MAX_VALUE);
            return lr.getLineNumber();
        } catch (Exception fe){
            debugUtil.logMessage(TAG, "getNumLinesInFile(): File not found", DebugUtil.LOG_LEVEL_ERROR, run_environment);
        }

        return -1;
    }

    /*
    public void saveData(View v){
       debugUtil.logMessage(TAG, "Is edit? " + dataEdit, run_environment);

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
            debugUtil.logMessage(TAG,"Updating values for cane " + caneId, run_environment);
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
                debugUtil.logMessage(TAG, "Error updating " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
                return;
            }

            debugUtil.logMessage(TAG, "Data has been updated for cane " + caneId, run_environment);
        } else{
            debugUtil.logMessage(TAG,"Saving new data", DebugUtil.LOG_LEVEL_INFO, run_environment);
            ContentValues values = new ContentValues();

            // construct caneLength observation
            values.put(DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE,caneLengthText.getMeasurementId());
            values.put(DbObservations.OBSERVATIONS_CANE_ID_TITLE, caneId);
            values.put(DbObservations.OBSERVATIONS_VALUE_TITLE, caneLengthMeasurement);
            values.put(DbObservations.OBSERVATIONS_METADATA_TITLE, metaData);
            values.put(DbObservations.OBSERVATIONS_CHANGED_TITLE, true);

            long rowId = db.insert(DbObservations.OBSERVATIONS_TABLE_NAME, null, values);

            debugUtil.logMessage(TAG, "Saved length: " + rowId, run_environment);

            values.put(DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE, caneDiameterText.getMeasurementId());
            values.put(DbObservations.OBSERVATIONS_VALUE_TITLE, caneDiameterMeasurement);

            rowId = -1;
            rowId = db.insert(DbObservations.OBSERVATIONS_TABLE_NAME, null, values);

            debugUtil.logMessage(TAG, "Saved diameter: " + rowId, run_environment);
        }
    }

    public void copyDatabase(View v){
       debugUtil.logMessage(TAG, "Try to copy db", run_environment);
        try{
            File sd = Environment.getExternalStorageDirectory();
            File internal = Environment.getDataDirectory();

            if(sd.canWrite()){
                debugUtil.logMessage(TAG, "Can write", run_environment);
                debugUtil.logMessage(TAG, "Data path: " + internal.toString(), run_environment);
                File deviceDb = new File(internal,"/user/0/echo.rootstockapp/databases/HandHeld.db");
                File newDb = new File("/sdcard/HandHeld.db");

                if(deviceDb.exists()){
                   debugUtil.logMessage(TAG, "Db exists", run_environment);
                    FileChannel src = new FileInputStream(deviceDb).getChannel();
                    FileChannel dst = new FileOutputStream(newDb).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e){
            debugUtil.logMessage(TAG, "Error copying Db: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
        }
    }

    public void updateIdentifierFields(final Cursor c){
        final TextView textId = (TextView) findViewById(R.id.id);
        final TextView textBarcode = (TextView) findViewById(R.id.barcode);
        //final TextView textType = (TextView) findViewById(R.id.type);
        //final TextView textSite = (TextView) findViewById(R.id.site);
        //final TextView textBlock = (TextView) findViewById(R.id.block);
        final TextView textFPI = (TextView) findViewById(R.id.FPI);
        final TextView textCultivar = (TextView) findViewById(R.id.cultivar);
        //final TextView textGraftYear = (TextView) findViewById(R.id.graftyear);

        final long caneId = c.getLong(c.getColumnIndexOrThrow(DbIdentifiers._ID));
        final String barcode = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_BARCODE_TITLE));
        //final String type = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_TYPE_TITLE));
        //final String site = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_SITE_TITLE));
        //final String block = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_BLOCK_TITLE));
        final String FPI = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_FPI_TITLE));
        final String cultivar = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_CULTIVAR_TITLE));
        //final String graftYear = c.getString(c.getColumnIndexOrThrow(DbIdentifiers.IDENTIFIERS_GRAFT_YEAR_TITLE));


        runOnUiThread(new Runnable() {
            @Override
            public void run(){
                ((MeasurementText) findViewById(R.id.cane_length)).setText("");
                ((MeasurementText) findViewById(R.id.cane_diameter)).setText("");
                textId.setText(Long.toString(caneId));
                textBarcode.setText(barcode);
                //textType.setText(type);
                //textSite.setText(site);
                //textBlock.setText(block);
                textFPI.setText(FPI);
                textCultivar.setText(cultivar);
                //textGraftYear.setText(graftYear);
            }
        });

    }

    */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbContract.DbObservations.SQL_CREATE_OBSERVATIONS);
        db.execSQL(DbContract.DbCaneIdentifiers.SQL_CREATE_TABLE);
        db.execSQL(DbContract.DbComponentIdentifiers.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        debugUtil.logMessage(TAG, "Upgrading DB", run_environment);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.DbCaneIdentifiers.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.DbComponentIdentifiers.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.DbObservations.OBSERVATIONS_TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        debugUtil.logMessage(TAG, "Downgrading DB", run_environment);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.DbCaneIdentifiers.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.DbComponentIdentifiers.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.DbObservations.OBSERVATIONS_TABLE_NAME);
        onCreate(db);
    }

    public interface DbProgressListener {
        void updateProgress(int progress, int total);

        void setProgressText(String message);

        void setResponseTextPositive(String message);

        void setResponseTextNegative(String message);
    }
}