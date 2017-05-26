package echo.rootstockapp;

import android.provider.BaseColumns;
import echo.rootstockapp.DbContract.DbIdentifiers;
import echo.rootstockapp.DbContract.DbObservations;

public final class DbContract {

    public static final String DUMMY_DATA = "INSERT INTO identifiers (" + DbIdentifiers.IDENTIFIERS_BARCODE_TITLE + "," + 
        DbIdentifiers.IDENTIFIERS_TYPE_TITLE + "," + DbIdentifiers.IDENTIFIERS_SITE_TITLE + "," +
        DbIdentifiers.IDENTIFIERS_BLOCK_TITLE + "," + DbIdentifiers.IDENTIFIERS_FPI_TITLE + "," + 
        DbIdentifiers.IDENTIFIERS_CULTIVAR_TITLE  + "," + DbIdentifiers.IDENTIFIERS_GRAFT_YEAR_TITLE + ")" +
        "VALUES ('C.23138', 'Cane', 'TePuke', '1', 'G14.01-09-18a', 'R039', '14')," + 
               "('C.23035', 'Cane', 'TePuke', '1', 'G14.01-03-20a', 'Y097', '14')," +
               "('C.23424', 'Cane', 'TePuke', '3', 'G09.03-26-25b', 'MC089', '09')," +
               "('C.23306', 'Cane', 'TePuke', '2', 'G14.02-17-23a', 'G096', '14')," +
               "('C.23099', 'Cane', 'TePuke', '1', 'G14.01-07-08a', 'R037', '14')," +
               "('G14.01-09-11a', 'Vine', 'TePuke', '1', 'G14.01-09-11a', 'R45', '14')";

    private void DBContract() {}

    public static class DbIdentifiers implements BaseColumns {
        public static final String SQL_CREATE_IDENTIFIERS = "Create table " + DbIdentifiers.IDENTIFIERS_TABLE_NAME + 
            " (" + DbIdentifiers._ID + " Integer Primary Key," +
            DbIdentifiers.IDENTIFIERS_BARCODE_TITLE + " Text," +
            DbIdentifiers.IDENTIFIERS_TYPE_TITLE + " Text," +
            DbIdentifiers.IDENTIFIERS_SITE_TITLE + " Text," +
            DbIdentifiers.IDENTIFIERS_BLOCK_TITLE + " Text," +
            DbIdentifiers.IDENTIFIERS_FPI_TITLE + " Text," +
            DbIdentifiers.IDENTIFIERS_CULTIVAR_TITLE + " Text," +
            DbIdentifiers.IDENTIFIERS_GRAFT_YEAR_TITLE + " Text)";

        public static final String IDENTIFIERS_TABLE_NAME = "identifiers";
        public static final String IDENTIFIERS_BARCODE_TITLE = "barcode";
        public static final String IDENTIFIERS_TYPE_TITLE = "type";
        public static final String IDENTIFIERS_SITE_TITLE = "site";
        public static final String IDENTIFIERS_BLOCK_TITLE = "block";
        public static final String IDENTIFIERS_FPI_TITLE = "FPI";
        public static final String IDENTIFIERS_CULTIVAR_TITLE = "cultivar";
        public static final String IDENTIFIERS_GRAFT_YEAR_TITLE = "graft_year";
    }

    public static class DbObservations implements BaseColumns {
        public static final String SQL_CREATE_OBSERVATIONS = "Create table " + DbObservations.OBSERVATIONS_TABLE_NAME +
        " (" + DbObservations._ID + " Integer Primary Key," + 
        DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE + " Text," +
        DbObservations.OBSERVATIONS_COMPONENT_ID_TITLE + " Text," +
        DbObservations.OBSERVATIONS_CANE_ID_TITLE + " Text," +
        DbObservations.OBSERVATIONS_VALUE_TITLE + " Text," +
        DbObservations.OBSERVATIONS_METADATA_TITLE + " Text," + 
        DbObservations.OBSERVATIONS_CHANGED_TITLE + " Boolean)";

        public static final String OBSERVATIONS_TABLE_NAME = "observations";

        public static final String OBSERVATIONS_MEASUREMENT_ID_TITLE = "measurement_id"; // links to Kakapo meaasurement ID
        public static final String OBSERVATIONS_COMPONENT_ID_TITLE = "component_id"; // links to ID of Identifiers table
        public static final String OBSERVATIONS_CANE_ID_TITLE = "cane_id"; // links to ID of Identifiers table
        public static final String OBSERVATIONS_VALUE_TITLE = "value";
        public static final String OBSERVATIONS_METADATA_TITLE = "metadata";
        public static final String OBSERVATIONS_CHANGED_TITLE = "changed";
    }
}