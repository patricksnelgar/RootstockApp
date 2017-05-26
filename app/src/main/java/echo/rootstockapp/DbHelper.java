package echo.rootstockapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import echo.rootstockapp.DbContract.DbIdentifiers;
import echo.rootstockapp.DbContract.DbObservations;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 6;
    public static final String DATABASAE_NAME = "HandHeld.db";

    public DbHelper(Context context) {
        super(context, DATABASAE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbContract.DbIdentifiers.SQL_CREATE_IDENTIFIERS);
        db.execSQL(DbContract.DbObservations.SQL_CREATE_OBSERVATIONS);
        db.execSQL(DbContract.DUMMY_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DELETE FROM identifiers");
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.DbIdentifiers.IDENTIFIERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.DbObservations.OBSERVATIONS_TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.DbIdentifiers.IDENTIFIERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.DbObservations.OBSERVATIONS_TABLE_NAME);
        onCreate(db);
    }
}