package echo.rootstockapp;

import android.provider.BaseColumns;

public final class DbContract {

    static class IdentifierColumnNames {
        static final String _ID = "_id";
        static final String BARCODE_TITLE = "barcode";
        static final String TYPE_TITLE = "type";
        static final String SITE_TITLE = "site";
        static final String BLOCK_TITLE = "block";
        static final String FPI_TITLE = "FPI";
        static final String CULTIVAR_TITLE = "cultivar";
        static final String GRAFT_YEAR_TITLE = "graft_year";

    }

    static class DbComponentIdentifiers implements BaseColumns {
        static final String TABLE_NAME = "component_identifiers";
        static final String BARCODE_TITLE = IdentifierColumnNames.BARCODE_TITLE;
        static final String TYPE_TITLE = IdentifierColumnNames.TYPE_TITLE;
        static final String SITE_TITLE = IdentifierColumnNames.SITE_TITLE;
        static final String BLOCK_TITLE = IdentifierColumnNames.BLOCK_TITLE;
        static final String FPI_TITLE = IdentifierColumnNames.FPI_TITLE;
        static final String CULTIVAR_TITLE = IdentifierColumnNames.CULTIVAR_TITLE;
        static final String GRAFT_YEAR_TITLE = IdentifierColumnNames.GRAFT_YEAR_TITLE;
        static final String SQL_CREATE_TABLE = "Create table " + DbComponentIdentifiers.TABLE_NAME +
            " (" + DbComponentIdentifiers._ID + " Integer Primary Key," +
            DbComponentIdentifiers.BARCODE_TITLE + " Text," +
            DbComponentIdentifiers.TYPE_TITLE + " Text," +
            DbComponentIdentifiers.SITE_TITLE + " Text," +
            DbComponentIdentifiers.BLOCK_TITLE + " Text," +
            DbComponentIdentifiers.FPI_TITLE + " Text," +
            DbComponentIdentifiers.CULTIVAR_TITLE + " Text," +
            DbComponentIdentifiers.GRAFT_YEAR_TITLE + " Text)";
    }

    static class DbCaneIdentifiers implements BaseColumns {
        static final String TABLE_NAME = "cane_identifiers";
        static final String BARCODE_TITLE = IdentifierColumnNames.BARCODE_TITLE;
        static final String TYPE_TITLE = IdentifierColumnNames.TYPE_TITLE;
        static final String SITE_TITLE = IdentifierColumnNames.SITE_TITLE;
        static final String BLOCK_TITLE = IdentifierColumnNames.BLOCK_TITLE;
        static final String FPI_TITLE = IdentifierColumnNames.FPI_TITLE;
        static final String CULTIVAR_TITLE = IdentifierColumnNames.CULTIVAR_TITLE;
        static final String GRAFT_YEAR_TITLE = IdentifierColumnNames.GRAFT_YEAR_TITLE;
        static final String SQL_CREATE_TABLE = "Create table " + DbCaneIdentifiers.TABLE_NAME +
            " (" + DbCaneIdentifiers._ID + " Integer Primary Key," +
            DbCaneIdentifiers.BARCODE_TITLE + " Text," +
            DbCaneIdentifiers.TYPE_TITLE + " Text," +
            DbCaneIdentifiers.SITE_TITLE + " Text," +
            DbCaneIdentifiers.BLOCK_TITLE + " Text," +
            DbCaneIdentifiers.FPI_TITLE + " Text," +
            DbCaneIdentifiers.CULTIVAR_TITLE + " Text," +
            DbCaneIdentifiers.GRAFT_YEAR_TITLE + " Text)";
    }

    static class DbObservations implements BaseColumns {
        static final String OBSERVATIONS_TABLE_NAME = "observations";
        static final String OBSERVATIONS_VINE_SITE_TITLE = "vine_site";              // Refers to the orchard code
        static final String OBSERVATIONS_MEASUREMENT_ID_TITLE = "measurement_id";    // The _ID of the measurement
        static final String OBSERVATIONS_COMPONENT_ID_TITLE = "component_id";        // Links to the _ID of DbComponentIdentifiers table
        static final String OBSERVATIONS_CANE_ID_TITLE = "cane_id";                  // Links to the _ID of DbCaneIdentifiers table
        static final String OBSERVATIONS_VALUE_TITLE = "value";
        static final String OBSERVATIONS_METADATA_TITLE = "metadata";
        static final String OBSERVATIONS_CHANGED_TITLE = "changed";
        static final String SQL_CREATE_OBSERVATIONS = "Create table " + DbObservations.OBSERVATIONS_TABLE_NAME +
                " (" + DbObservations._ID + " Integer Primary Key," +
        DbObservations.OBSERVATIONS_MEASUREMENT_ID_TITLE + " Text," +
        DbObservations.OBSERVATIONS_COMPONENT_ID_TITLE + " Text," +
        DbObservations.OBSERVATIONS_CANE_ID_TITLE + " Text," +
        DbObservations.OBSERVATIONS_VALUE_TITLE + " Text," +
                DbObservations.OBSERVATIONS_METADATA_TITLE + " Text," +
        DbObservations.OBSERVATIONS_CHANGED_TITLE + " Boolean)";
    }

    // Fill in the rest of this when functionality is implemented kakapo side
    public static class DbMeasurementInfo implements BaseColumns {
        public static final String SQL_CREATE_MEASUREMTENS = "Create table";

        public static final String MEASUREMENT_INFO_TABLE_NAME = "measurement_info";

    }
}