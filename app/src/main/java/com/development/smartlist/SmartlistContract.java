package com.development.smartlist;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

class SmartlistContract {

    // This is the symbolic name of your provider. To avoid conflicts with other providers, you
    // should use internet domain ownership as the basis of your provider authority.
    static final String AUTHORITY = "com.development.smartlist";

    // The content authority is used to create the base of all URIs which apps will use to contact
    // this content provider.
    static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    // A list of paths that is appended to the BASE_URI for each of the different tables.
    static final String TABLE_SQLITE_MASTER = "sqlite_master";
    static final String TABLE_USERS = DBAdapter.USER_TABLE;
    private static final String TABLE_CATEGORIES = DBAdapter.CATEGORY_TABLE;
    private static final String TABLE_SHOPS = DBAdapter.SHOP_TABLE;
    private static final String TABLE_BRANDS = DBAdapter.BRAND_TABLE;
    static final String TABLE_ITEMS = DBAdapter.ITEMS_TABLE;
    private static final String TABLE_ITEM_INFO = DBAdapter.ITEM_INFO_TABLE;
    private static final String TABLE_LISTS = DBAdapter.LISTS_TABLE;
    private static final String TABLE_LIST_ITEMS = DBAdapter.LIST_ITEMS_TABLE;

    /*
     * Create one class for each table that handles all information regarding the table schema
     * and the URIs related to it.
     */

    public static final class TableSqliteMaster implements BaseColumns {
        // Content URI represents the base location for the table.
        static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(TABLE_SQLITE_MASTER).build();

        // These are special type prefixes that specify if a URI returns a list of records or a
        // specific record.
        static final String TABLE_RECORDS = "vnd.android.cursor.dir/" + CONTENT_URI + "/" +
                TABLE_SQLITE_MASTER;
        public static final String TABLE_RECORD = "vnd.android.cursor.item/" + CONTENT_URI + "/" +
                TABLE_SQLITE_MASTER;

        // Define the table schema
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TBL_NAME = "tbl_name";
        public static final String COLUMN_ROOTPAGE = "rootpage";
        public static final String COLUMN_SQL = "sql";

        // Define a function to build a URI to find a specific user by its identifier.
        public static Uri buildMasterUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class TableUsers implements BaseColumns {
        // Content URI represents the base location for the table.
        static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(TABLE_USERS).build();

        // These are special type prefixes that specify if a URI returns a list of records or a
        // specific record.
        static final String TABLE_RECORDS = "vnd.android.cursor.dir/" + CONTENT_URI + "/" +
                TABLE_USERS;
        static final String TABLE_RECORD = "vnd.android.cursor.item/" + CONTENT_URI + "/" +
                TABLE_USERS;

        // Define the table schema
        static final String COLUMN_USER_ID = DBAdapter.C_USER_ID;
        static final String COLUMN_USER_NAME = DBAdapter.C_USER_NAME;

        // Define a function to build a URI to find a specific user by its identifier.
        static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    static final class TableItems implements BaseColumns {
        // Content URI represents the base location for the table.
        static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(TABLE_ITEMS).build();

        // These are special type prefixes that specify if a URI returns a list of records or a
        // specific record.
        static final String TABLE_RECORDS = "vnd.android.cursor.dir/" + CONTENT_URI + "/" +
                TABLE_ITEMS;
        static final String TABLE_RECORD = "vnd.android.cursor.item/" + CONTENT_URI + "/" +
                TABLE_ITEMS;

        // Define the table schema
        static final String COLUMN_ITEM_ID = DBAdapter.C_ITEM_ID;
        static final String COLUMN_ITEM_NAME = DBAdapter.C_ITEM_NAME;
        static final String COLUMN_CAT_ID = DBAdapter.C_CAT_ID;
        static final String COLUMN_QUANTITY_UNIT = DBAdapter.C_QUANTITY_UNIT;
        static final String COLUMN_BARCODE = DBAdapter.C_BARCODE;

        // Define a function to build a URI to find a specific user by its identifier.
        static Uri buildItemUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
