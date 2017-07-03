package echo.rootstockapp;

import android.provider.BaseColumns;

public final class DbContract {

    public static class IdentifierColumnNames {
        public static final String _ID = "_id";
        public static final String BARCODE_TITLE = "barcode";
        public static final String TYPE_TITLE = "type";
        public static final String SITE_TITLE = "site";
        public static final String BLOCK_TITLE = "block";
        public static final String FPI_TITLE = "FPI";
        public static final String CULTIVAR_TITLE = "cultivar";
        public static final String GRAFT_YEAR_TITLE = "graft_year";

    }

    private void DBContract() {}

    public static class DbComponentIdentifiers implements BaseColumns {
        public static final String SQL_CREATE_TABLE = "Create table " + DbComponentIdentifiers.TABLE_NAME + 
            " (" + DbComponentIdentifiers._ID + " Integer Primary Key," +
            DbComponentIdentifiers.BARCODE_TITLE + " Text," +
            DbComponentIdentifiers.TYPE_TITLE + " Text," +
            DbComponentIdentifiers.SITE_TITLE + " Text," +
            DbComponentIdentifiers.BLOCK_TITLE + " Text," +
            DbComponentIdentifiers.FPI_TITLE + " Text," +
            DbComponentIdentifiers.CULTIVAR_TITLE + " Text," +
            DbComponentIdentifiers.GRAFT_YEAR_TITLE + " Text)";

        public static final String TABLE_NAME = "component_identifiers";
        public static final String BARCODE_TITLE = "barcode";
        public static final String TYPE_TITLE = "type";
        public static final String SITE_TITLE = "site";
        public static final String BLOCK_TITLE = "block";
        public static final String FPI_TITLE = "FPI";
        public static final String CULTIVAR_TITLE = "cultivar";
        public static final String GRAFT_YEAR_TITLE = "graft_year";
    }

    public static class DbCaneIdentifiers implements BaseColumns {
        public static final String SQL_CREATE_TABLE = "Create table " + DbCaneIdentifiers.TABLE_NAME + 
            " (" + DbCaneIdentifiers._ID + " Integer Primary Key," +
            DbCaneIdentifiers.BARCODE_TITLE + " Text," +
            DbCaneIdentifiers.TYPE_TITLE + " Text," +
            DbCaneIdentifiers.SITE_TITLE + " Text," +
            DbCaneIdentifiers.BLOCK_TITLE + " Text," +
            DbCaneIdentifiers.FPI_TITLE + " Text," +
            DbCaneIdentifiers.CULTIVAR_TITLE + " Text," +
            DbCaneIdentifiers.GRAFT_YEAR_TITLE + " Text)";

        public static final String TABLE_NAME = "cane_identifiers";
        public static final String BARCODE_TITLE = "barcode";
        public static final String TYPE_TITLE = "type";
        public static final String SITE_TITLE = "site";
        public static final String BLOCK_TITLE = "block";
        public static final String FPI_TITLE = "FPI";
        public static final String CULTIVAR_TITLE = "cultivar";
        public static final String GRAFT_YEAR_TITLE = "graft_year";
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