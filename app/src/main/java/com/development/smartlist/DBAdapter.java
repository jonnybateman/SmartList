package com.development.smartlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

class DBAdapter {

    private Context context;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    static final String DB_NAME = "smartlist_db";
    static final int DB_VERSION = 2;

    // Setup the table 'category'.
    static final String CATEGORY_TABLE = "categories";
    static final String C_CAT_ID = "cat_id";
    private static final String C_CATEGORY_NAME = "category_name";

    // String constant for the SQL syntax to create 'category' table.
    private static final String CREATE_CATEGORY_TABLE_SQL = "create table " + CATEGORY_TABLE + "(" +
            C_CAT_ID + " integer primary key autoincrement, " + C_CATEGORY_NAME +
            " text not null unique)";

    // Setup the table 'shop'.
    static final String SHOP_TABLE = "shops";
    private static final String C_SHOP_ID = "shop_id";
    private static final String C_SHOP_NAME = "shop_name";

    // String constant for the SQL syntax to create 'shop' table.
    private static final String CREATE_SHOP_TABLE_SQL = "create table " + SHOP_TABLE + "(" +
            C_SHOP_ID + " integer primary key autoincrement, " + C_SHOP_NAME +
            " text not null unique)";

    // Setup the table 'brand'.
    static final String BRAND_TABLE = "brands";
    private static final String C_BRAND_ID = "brand_id";
    private static final String C_BRAND_NAME = "brand_name";

    // String constant for the SQL syntax to create 'brand' table.
    private static final String CREATE_BRAND_TABLE_SQL = "create table " + BRAND_TABLE + "(" +
            C_BRAND_ID + " integer primary key autoincrement, " + C_BRAND_NAME +
            " text not null unique)";

    // Setup the table 'items'.
    static final String ITEMS_TABLE = "items";
    static final String C_ITEM_ID = "item_id";
    static final String C_ITEM_NAME = "item_name";
    static final String C_QUANTITY_UNIT = "quantity_unit";
    static final String C_BARCODE = "barcode";

    // String constant for the SQL syntax to create 'items' table.
    private static final String CREATE_ITEMS_TABLE_SQL = "create table " + ITEMS_TABLE + "(" +
            C_ITEM_ID + " integer primary key autoincrement, " + C_ITEM_NAME +
            " text not null unique, " + C_CAT_ID + " integer not null, " + C_QUANTITY_UNIT +
            " text not null, " +
            C_BARCODE + " text unique, foreign key (" +
            C_CAT_ID + ") references " + CATEGORY_TABLE + "(" + C_CAT_ID + ") on delete cascade)";

    // Setup the table 'item_info'
    static final String ITEM_INFO_TABLE = "item_info";
    private static final String C_LOCATION = "location";
    private static final String C_QUANTITY = "quantity";
    private static final String C_PRICE = "price";

    // String constant for the SQL syntax to create the 'item_info' table.
    private static final String CREATE_ITEM_INFO_TABLE_SQL = "create table " + ITEM_INFO_TABLE +
            "(" + C_ITEM_ID + " integer," + C_PRICE + " real," + C_SHOP_ID + " integer," + C_BRAND_ID +
            " integer," + C_LOCATION + " text," + C_QUANTITY + " text, primary key (" + C_ITEM_ID +
            "," + C_SHOP_ID + "," + C_LOCATION + "))";

    // Setup the table 'lists'.
    static final String LISTS_TABLE = "lists";
    private static final String C_LIST_ID = "list_id";
    private static final String C_LIST_NAME = "list_name";

    // String constant for the SQL syntax to create the 'lists' table.
    private static final String CREATE_LISTS_TABLE_SQL = "create table " + LISTS_TABLE + "(" +
            C_LIST_ID + " integer primary key autoincrement, " + C_LIST_NAME +
            " text not null unique)";

    // Setup the table 'list_items'
    static final String LIST_ITEMS_TABLE = "list_items";
    private static final String C_ITEM_CHECKED = "checked";

    // String constant for the SQL syntax to create the 'list_items' table.
    private static final String CREATE_LIST_ITEMS_TABLE_SQL = "create table " + LIST_ITEMS_TABLE +
            "(" + C_LIST_ID + " integer, " + C_ITEM_ID + " integer, " + C_ITEM_NAME + " text not null, " +
            C_CATEGORY_NAME + " text not null, " + C_QUANTITY + " text not null," +
            C_ITEM_CHECKED + " integer not null, primary key ("
            + C_LIST_ID + ", " + C_ITEM_ID + "))";

    // Setup the table 'locale'.
    private static final String LOCALE_TABLE = "locale";

    // String constant for the SQL syntax to create the 'locale' table.
    private static final String CREATE_LOCALE_TABLE_SQL = "create table " + LOCALE_TABLE +
            "(" + C_LOCATION + " text not null)";

    // Setup the table 'user'.
    static final String USER_TABLE = "user";
    static final String C_USER_ID = "user_id";
    static final String C_USER_NAME = "user_name";

    // String constant for the SQL syntax to create the 'user' table.
    private static final String CREATE_USER_TABLE_SQL = "create table " + USER_TABLE +
            "(" + C_USER_ID + " integer not null," + C_USER_NAME + " text not null)";

    // Initiate new instance of DBHelper class within constructor of DBAdapter class and save that
    // instance in a member variable. DBHelper instance used for opening and closing of database.
    DBAdapter(Context context) {
        this.context = context;
        dbHelper = new DBHelper();
    }

    // Private inner class named DBHelper which is a subclass of SQLiteOpenHelper class.
    // Helps us to manage the tasks of database creation and version management.
    public class DBHelper extends SQLiteOpenHelper {

        // Constructor of DBHelper class
        DBHelper() {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_CATEGORY_TABLE_SQL);
                db.execSQL(CREATE_SHOP_TABLE_SQL);
                db.execSQL(CREATE_BRAND_TABLE_SQL);
                db.execSQL(CREATE_ITEMS_TABLE_SQL);
                db.execSQL(CREATE_LISTS_TABLE_SQL);
                db.execSQL(CREATE_LIST_ITEMS_TABLE_SQL);
                db.execSQL(CREATE_ITEM_INFO_TABLE_SQL);
                db.execSQL(CREATE_LOCALE_TABLE_SQL);
                db.execSQL("insert into " + LOCALE_TABLE + " values('GBP')");
                db.execSQL(CREATE_USER_TABLE_SQL);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Called when the DB_VERSION number is different from the stored one.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int upgradeTo = oldVersion + 1;
            // Loop through each version to ensure user has all the latest updates.
            while (upgradeTo <= newVersion) {
                switch(upgradeTo) {
                    case 2:
                        db.execSQL(CREATE_CATEGORY_TABLE_SQL);
                        db.execSQL(CREATE_SHOP_TABLE_SQL);
                        db.execSQL(CREATE_BRAND_TABLE_SQL);
                        db.execSQL(CREATE_ITEMS_TABLE_SQL);
                        db.execSQL(CREATE_LISTS_TABLE_SQL);
                        db.execSQL(CREATE_LIST_ITEMS_TABLE_SQL);
                        db.execSQL(CREATE_ITEM_INFO_TABLE_SQL);
                        db.execSQL(CREATE_LOCALE_TABLE_SQL);
                        db.execSQL("insert into " + LOCALE_TABLE + " values('GBP')");
                        db.execSQL(CREATE_USER_TABLE_SQL);
                    case 3:
                        db.execSQL("drop table if exists " + LIST_ITEMS_TABLE);
                        db.execSQL(CREATE_LIST_ITEMS_TABLE_SQL);
                        break;
                }
                upgradeTo++;
            }
        }

        // Enable foreign key constraints in database
        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            if (!db.isReadOnly()) {
                db.setForeignKeyConstraintsEnabled(true);
            }
        }
    }

    // Open Database
    void open() {
        db = dbHelper.getWritableDatabase();
    }

    // Close Database
    void close() {
        db.close();
    }

    // Start transaction.
    void beginTransaction() {
        db.beginTransaction();
    }

    // Set the transaction() {
    void setTransaction() {
        db.setTransactionSuccessful();
    }

    // End transaction.
    void endTransaction() {
        db.endTransaction();
    }

    // Delete table data
    void deleteTableData(String tableName) {
        db.execSQL("delete from " + tableName);
    }

    // Insert a record into the supplied table name.
    long insertTableElement(String tableName, ContentValues values) {
        return db.insert(tableName, null, values);
    }

    /*
     * USER table methods
     */

    // Insert record into USER table. Declared package private
    // Returns -1 if error occurred, otherwise returns rowid of new record.
    long insertUser(int userId, String userName) {
        ContentValues values = new ContentValues();
        values.put(C_USER_ID, userId);
        values.put(C_USER_NAME, userName);
        return db.insert(USER_TABLE, null, values);
    }

    // Update the user record with new user name.
    long updateUserName(String userName) {
        ContentValues data = new ContentValues();
        data.put(C_USER_NAME, userName);
        return db.update(USER_TABLE, data, null, null);
    }

    int getUserId() {
        int userId = 0;
        Cursor cursor = db.rawQuery("select user_id from " + USER_TABLE, null);

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }

        cursor.close();
        return userId;
    }

    // Get the stored username for the database.
    String getUserName() {
        String userName = "";
        try (Cursor cursor = db.rawQuery("select user_name from " + USER_TABLE, null)) {

            if (cursor.moveToFirst()) {
                userName = cursor.getString(0);
            }

            return userName;
        }
    }

    /*
     * CATEGORIES table methods
     */

    // Get all categories
    ArrayList<String> getAllCategories() {
        ArrayList<String> categories = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("select " + C_CATEGORY_NAME + " from " + CATEGORY_TABLE, null)) {

            // looping through all rows and adding to categories array list
            if (cursor.moveToFirst()) {
                do {
                    categories.add(cursor.getString(cursor.getColumnIndex(DBAdapter.C_CATEGORY_NAME)));
                } while (cursor.moveToNext());
            }

            return categories;
        }
    }

    // Determine if category already exists in database or not. Declared package private
    Boolean categoryExists(String category) {

        boolean exists = false;
        try (Cursor cursor = db.rawQuery("select " + C_CATEGORY_NAME + " from " + CATEGORY_TABLE +
                    " where lower(" + C_CATEGORY_NAME + ") = lower('" + category + "')", null)) {

            if (cursor.getCount() > 0) {
                exists = true;
            }

            return exists;
        }
    }

    // Insert record into category table. Declared package private
    // Returns -1 if error occurred, otherwise returns rowid of new record.
    long insertCategory(String name) {
        ContentValues values = new ContentValues();
        values.put(C_CATEGORY_NAME, name);
        return db.insert(CATEGORY_TABLE, null, values);
    }

    // Delete selected category
    int deleteCategory(String category) {
        int number = 0;
        try {
            // Returns number of rows deleted.
            number = db.delete(CATEGORY_TABLE, "lower(" + C_CATEGORY_NAME + ") = lower('" +
                    category + "')", null);
            return number;
        }
        catch (SQLException e) {
            return number;
        }
    }

    // Return the category id from the category name.
    int getCategoryId(String category) {
        int catId = 0;

        try (Cursor cursor = db.rawQuery("select " + C_CAT_ID + " from " + CATEGORY_TABLE + " where " +
                    C_CATEGORY_NAME + "='" + category + "'", null)) {

            if (cursor.moveToFirst()) {
                catId = cursor.getInt(cursor.getColumnIndex(C_CAT_ID));
            }

            return catId;
        }
    }

    // Get the category name from the category id.
    String getCategoryName(int catId) {

        try (Cursor cursor = db.rawQuery("select " + C_CATEGORY_NAME + " from " + CATEGORY_TABLE + " where " +
                    C_CAT_ID + "=" + catId, null)) {
            String categoryName = "";

            if (cursor.moveToFirst()) {
                categoryName = cursor.getString(cursor.getColumnIndex(C_CATEGORY_NAME));
            }

            return categoryName;
        }
    }

    /*
     * ITEMS table methods
     */

    // Insert item into database.
    boolean insertItem(String item, int catId, String quantity_unit, String barcode) {
        try {
            String sql;
            if (barcode.length() > 0) {
                Log.d("db.insertItem","Insert with barcode");
                sql = "insert into " + ITEMS_TABLE + "(" + C_ITEM_NAME + "," + C_CAT_ID + "," +
                        C_QUANTITY_UNIT + "," + C_BARCODE + ") values ('" + item + "'," + catId + ",'" +
                        quantity_unit + "','" + barcode + "')";
            } else {
                Log.d("db.insertItem","Insert without barcode");
                sql = "insert into " + ITEMS_TABLE + "(" + C_ITEM_NAME + "," + C_CAT_ID + "," +
                        C_QUANTITY_UNIT + ") values ('" + item + "'," + catId + ",'" +
                        quantity_unit + "')";
            }
            db.execSQL(sql);
            return true;
        }
        catch (Exception e) {
            Log.d("db.insertItem",e.toString());
            return false;
        }
    }

    // Delete item from the database table ITEMS using item name
    int deleteItem(String itemName) {
        int number = 0;
        try {
            // Get the item id for the item being deleted.
            int itemId = getItemId(itemName);

            if (itemId > 0) {
                // Delete the item info for the item being deleted. Returns number of rows deleted.
                number = db.delete(ITEM_INFO_TABLE, C_ITEM_ID + "=" + itemId, null);

                // Delete the item from shopping lists.
                number = db.delete(LIST_ITEMS_TABLE, C_ITEM_ID + "=" + itemId, null);

                // Delete the item from the items table.
                number = db.delete(ITEMS_TABLE, C_ITEM_ID + "=" + itemId, null);
            }
            return number;
        }
        catch (Exception e) {
            return number;
        }
    }

    // Get items relating to a specific category.
    List<String> getCategoryItems(int categoryId) {

        List<String> items = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("select " + C_ITEM_NAME + " from " + ITEMS_TABLE +
                " where " + C_CAT_ID + "=" + categoryId, null)) {

            if (cursor.moveToFirst()) {
                do {
                    items.add(cursor.getString(cursor.getColumnIndex(C_ITEM_NAME)));
                } while (cursor.moveToNext());
            }

            return items;
        }
    }

    // Get the item's id using item name.
    int getItemId(String itemName) {

        int itemId = 0;
        try (Cursor cursor = db.rawQuery("select " + C_ITEM_ID + "," + C_QUANTITY_UNIT + " from " +
                ITEMS_TABLE + " where " + C_ITEM_NAME + "=" + "'" + itemName + "'", null)) {

            if (cursor.moveToFirst()) {
                itemId = cursor.getInt(cursor.getColumnIndex(C_ITEM_ID));
            }

            return itemId;
        }
    }

    // Return the item's stored quantity/unit flag.
    String getItemQtyUnitFlag(int itemId) {

        String flag = "";
        try (Cursor cursor = db.rawQuery("select " + C_QUANTITY_UNIT + " from " + ITEMS_TABLE +
                " where " + C_ITEM_ID + "=" + itemId, null)) {

            if (cursor.moveToFirst()) {
                flag = cursor.getString(cursor.getColumnIndex(C_QUANTITY_UNIT));
            }

            return flag;
        }
    }

    // Update the quantity/unit flag for the given item id.
    Boolean updateQtyUnitFlag(int itemId, String quantityUnitFlag) {
        try {
            ContentValues data = new ContentValues();
            data.put(C_QUANTITY_UNIT, quantityUnitFlag);
            db.update(ITEMS_TABLE, data, C_ITEM_ID + "=" + itemId, null);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    // Check to see if the item already exists in the items table.
    boolean checkItemExists(String itemName) {

        boolean exists = false;
        try (Cursor cursor = db.rawQuery("select * from " + ITEMS_TABLE + " where " + C_ITEM_NAME +
                " = '" + itemName + "'", null)) {

            if (cursor.moveToFirst()) {
                exists = true;
            }

            return exists;
        }
    }

    // Return all items from the items table.
    ArrayList<String> getAllItems() {

        ArrayList<String> items = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("select " + C_ITEM_NAME + " from " + ITEMS_TABLE, null)) {

            if (cursor.moveToFirst()) {
                do {
                    items.add(cursor.getString(cursor.getColumnIndex(C_ITEM_NAME)));
                } while (cursor.moveToNext());
            }

            return items;
        }
    }

    // Get item based on barcode.
    Object[] getItemFromBarcode(String barcode) {

        Cursor cursor = null;
        Object[] item = null;

        try {
            cursor = db.rawQuery("select " + C_ITEM_ID + "," + C_CAT_ID + "," + C_ITEM_NAME +
                    " from " + ITEMS_TABLE +
                    " where " + C_BARCODE + "='" + barcode + "'", null);

            if (cursor.moveToFirst()) {
                item = new Object[] {cursor.getInt(cursor.getColumnIndex(C_ITEM_ID)),
                        cursor.getInt(cursor.getColumnIndex(C_CAT_ID)),
                        cursor.getString(cursor.getColumnIndex(C_ITEM_NAME)),
                        barcode };
            }

            return item;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /*
     * ITEM_INFO table methods
     */

    // Return the info details count for a given item.
    int getItemInfoCount(int itemId, String location) {

        int rows = 0;
        try (Cursor cursor = db.rawQuery("select * from " + ITEM_INFO_TABLE + " where " + C_ITEM_ID +
                    "=" + itemId + " and " + C_LOCATION + "='" + location + "'", null)) {

            if (cursor.moveToFirst()) {
                rows = cursor.getCount();
            }

            return rows;
        }
    }

    // Get the item info for the item info dialog given an itemId.
    ArrayList<Object[]> getDialogItemInfo(int itemId, String location) {

        Object[] item;
        ArrayList<Object[]> items = new ArrayList<>();

        try (Cursor cursor = db.rawQuery("select null as _id," +
                                                    C_SHOP_NAME + "," +
                                                    C_BRAND_NAME + "," +
                                                    C_PRICE +
                                            " from " + ITEM_INFO_TABLE + " as iit inner join " +
                                                    SHOP_TABLE + " as st " +
                                                        "on iit." + C_SHOP_ID + "=st." + C_SHOP_ID + " left outer join " +
                                                    BRAND_TABLE + " as bt " +
                                                        "on iit." + C_BRAND_ID + "=bt." + C_BRAND_ID +
                                            " where iit." + C_ITEM_ID + "=" + itemId +
                                            " and iit." + C_LOCATION + "='" + location + "'",
                null)) {

            if (cursor.moveToFirst()) {
                do {
                    item = new Object[] {cursor.getString(cursor.getColumnIndex(C_SHOP_NAME)),
                                         cursor.getString(cursor.getColumnIndex(C_BRAND_NAME)),
                                         cursor.getFloat(cursor.getColumnIndex(C_PRICE))};
                    items.add(item);
                } while (cursor.moveToNext());
            }

            return items;
        }
    }

    // Update the item's quantities should the quantity_unit field be changed from quantity to unit.
    void updateItemQtys(int itemId) {
        try {
            // Update quantities for item in the item_info table.
            String sql = "update " + ITEM_INFO_TABLE + " set " + C_QUANTITY + "= '' where " +
                    C_ITEM_ID + "=" + itemId;
            db.execSQL(sql);

        } catch (Exception e) {
            Log.d("updateItemQuantities", "Exception:" + e.toString());
        }
    }

    // Check if the current item info already exists in database. (0 - not exists; 1 - exists/changed; 2 - exists)
    int itemInfoStatus(int itemId, int shopId, int brandId, float price, String location, String quantity) {

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * " +
                                "from " + ITEM_INFO_TABLE +
                                " where " + C_ITEM_ID + "=" + itemId +
                                " and " + C_SHOP_ID + "=" + shopId +
                                " and ifnull(" + C_BRAND_ID + ",0)=" + brandId +
                                " and ifnull(" + C_PRICE + ",0) =" + price +
                                " and " + C_LOCATION + "='" + location + "'" +
                                " and lower(ifnull(" + C_QUANTITY + ",''))=lower('" + quantity + "')", null);

            if (cursor.moveToFirst()) {
                return 2;
            }

            cursor = db.rawQuery("select * " +
                                "from " + ITEM_INFO_TABLE +
                                " where " + C_ITEM_ID + "=" + itemId +
                                " and " + C_SHOP_ID + "=" + shopId +
                                " and " + C_LOCATION + "='" + location + "'" +
                                " and (ifnull(" + C_BRAND_ID + ",0)!=" + brandId +
                                    " or ifnull(" + C_PRICE + ",0) !=" + price +
                                    " or lower(ifnull(" + C_QUANTITY + ",'')) !=" + "lower('" + quantity + "'))", null);

            if (cursor.moveToFirst()) {
                return 1;
            }

            return 0;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Insert item into into database.
    boolean insertItemInfo(int itemId, float price, int shopId, int brandId, String location,
                           String quantity, String qtyUnitFlag) {
        try {
            String sql = "insert into " + ITEM_INFO_TABLE +
                            "(" + C_ITEM_ID + "," + C_PRICE + "," + C_SHOP_ID + "," + C_BRAND_ID + "," +
                                C_LOCATION + "," + C_QUANTITY + ")" +
                        " values" +
                            "(" + itemId + "," + price + "," + shopId +
                                ",case when " + brandId + "> 0 then " + brandId + " else null end," +
                                "'" + location + "'" +
                                ",case when '" + qtyUnitFlag + "'='Q' then " + "'" + quantity + "' else '' end)";
            db.execSQL(sql);
            return true;
        }
        catch (Exception e) {
            Log.d("insertItemInfo","Exception:" + e.toString());
            return false;
        }
    }

    // Update the item info for a given item and shop.
    boolean updateItemInfo(int itemId, float price, int shopId, int brandId, String location,
                           String quantity) {
        try {
            String sql = "update " + ITEM_INFO_TABLE +
                        " set " + C_BRAND_ID + "=" + brandId + "," +
                                  C_PRICE + "=" + price + "," +
                                  C_QUANTITY + "='" + quantity +
                        "' where " + C_ITEM_ID + "=" + itemId +
                        " and " + C_SHOP_ID + "=" + shopId +
                        " and " + C_LOCATION + "='" + location + "'";
            db.execSQL(sql);
            return true;
        }
        catch (Exception e) {
            Log.d("updateItemInfo","Exception:" + e.toString());
            return false;
        }
    }

    // Get the shop, brand and item cost for the current item
    Object[] getItemInfo(int itemId, String location) {

        String brandName = "";
        Object[] itemInfo = null;
        try (Cursor cursor = db.rawQuery("select " + C_QUANTITY_UNIT + "," +
                                            "ifnull(" + C_SHOP_NAME + ",'') as " + C_SHOP_NAME + "," +
                                            "info." + C_PRICE + "," +
                                            "ifnull(" + C_SHOP_ID + ",0) as " + C_SHOP_ID + "," +
                                            "ifnull(" + C_BRAND_ID + ",0) as " + C_BRAND_ID + "," +
                                            C_QUANTITY + "," +
                                            C_BARCODE +
                                " from " + ITEMS_TABLE + " as items left outer join " +
                                        "(select " + C_SHOP_NAME + "," +
                                                    C_PRICE + "," +
                                                    "i." + C_SHOP_ID + " as " + C_SHOP_ID + "," +
                                                    C_BRAND_ID + "," +
                                                    C_ITEM_ID + "," +
                                                    C_QUANTITY +
                                        " from " + ITEM_INFO_TABLE + " as i inner join " +
                                                    SHOP_TABLE + " as s " +
                                        "on i." + C_SHOP_ID + "=" + "s." + C_SHOP_ID +
                                        " where i." + C_ITEM_ID + "=" + itemId +
                                        " and " + C_LOCATION + "='" + location + "') as info " +
                                "on items." + C_ITEM_ID + "=info." + C_ITEM_ID +
                                " where items." + C_ITEM_ID + "=" + itemId, null)) {

            if (cursor.moveToFirst()) {
                if (cursor.getInt(cursor.getColumnIndex(C_BRAND_ID)) > 0) {
                    // Obtain the brand name for the retrieved brand id.
                    brandName = getBrandName(cursor.getInt(cursor.getColumnIndex(C_BRAND_ID)));
                }
                itemInfo = new Object[] {cursor.getString(cursor.getColumnIndex(C_SHOP_NAME)),
                        brandName,
                        cursor.getFloat(cursor.getColumnIndex(C_PRICE)),
                        cursor.getInt(cursor.getColumnIndex(C_SHOP_ID)),
                        cursor.getInt(cursor.getColumnIndex(C_BRAND_ID)),
                        cursor.getString(cursor.getColumnIndex(C_QUANTITY_UNIT)),
                        cursor.getString(cursor.getColumnIndex(C_QUANTITY)),
                        cursor.getString(cursor.getColumnIndex(C_BARCODE))};
            }

            return itemInfo;
        }
    }

    // Get the item info data for the current list to populate the FragmentCompareList's table.
    ArrayList<Object[]> getItemInfoForItemList(String itemIds, String location) {
        Cursor cursor = null;
        ArrayList<Object[]> info = new ArrayList<>();
        try {
            String sql = "select " + ITEMS_TABLE + "." + C_ITEM_ID + " as " + C_ITEM_ID + "," +
                                    C_ITEM_NAME + "," + C_QUANTITY_UNIT + "," +
                                    "ifnull(" + C_SHOP_NAME + ",'') as " + C_SHOP_NAME + "," +
                                    "ifnull(" + C_BRAND_NAME + ",'') as " + C_BRAND_NAME + "," +
                                    "ifnull(data." + C_PRICE + ",0) as " + C_PRICE +
                        " from " + ITEMS_TABLE + " as " + ITEMS_TABLE + " inner join " + "" +
                            "(select " + C_ITEM_ID + "," + C_SHOP_NAME + "," + C_BRAND_NAME + "," +
                                        C_PRICE +
                            " from " + ITEM_INFO_TABLE + " as info " + "inner join " + SHOP_TABLE + " as shop " +
                                "on info." + C_SHOP_ID + "=shop." + C_SHOP_ID + " left outer join " +
                                BRAND_TABLE + " as brand " + "on info." + C_BRAND_ID + "=brand." + C_BRAND_ID +
                            " where info." + C_ITEM_ID + " in (" + itemIds + ")" +
                            " and " + C_LOCATION + "='" + location + "') as data " +
                        "on items." + C_ITEM_ID + "=data." + C_ITEM_ID + " " +
                        "where items." + C_ITEM_ID + " in (" + itemIds + ")" + " " +
                        "order by " + C_ITEM_NAME + "," + C_SHOP_NAME;

            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                do {
                    int itemId = cursor.getInt(cursor.getColumnIndex(C_ITEM_ID));
                    String itemName = cursor.getString(cursor.getColumnIndex(C_ITEM_NAME));
                    String qtyUnit = cursor.getString(cursor.getColumnIndex(C_QUANTITY_UNIT));
                    String shopName = cursor.getString(cursor.getColumnIndex(C_SHOP_NAME));
                    String brandName = cursor.getString(cursor.getColumnIndex(C_BRAND_NAME));
                    Float price = cursor.getFloat(cursor.getColumnIndex(C_PRICE));

                    info.add(new Object[] {itemId, itemName, qtyUnit, shopName, brandName, price});
                } while (cursor.moveToNext());
            }

            return info;

        } catch (Exception e){
            Log.d("getItemInfoForTable","Exception:" + e.toString());
            return null;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Get the brand, price and quantity for the current item and shop.
    Object[] getItemShopInfo(int itemId, int shopId, String location) {

        Object[] itemInfo = null;
        try
            (Cursor cursor = db.rawQuery("select " + C_PRICE + ", ifnull(i." + C_BRAND_ID + ",0) as " +
                    C_BRAND_ID + ", ifnull(" + C_BRAND_NAME + ", '') as " + C_BRAND_NAME + "," +
                    C_QUANTITY + " from " +
                    ITEM_INFO_TABLE + " as i left outer join " + BRAND_TABLE + " as b on ifnull(i." +
                    C_BRAND_ID + ",0)=b." + C_BRAND_ID + " where " + C_ITEM_ID + "=" + itemId +
                    " and " + C_SHOP_ID + "=" + shopId + " and " + C_LOCATION + "='" +
                    location + "'", null)) {

            if (cursor.moveToFirst()) {
                itemInfo = new Object[] {
                        cursor.getFloat(cursor.getColumnIndex(C_PRICE)),
                        cursor.getInt(cursor.getColumnIndex(C_BRAND_ID)),
                        cursor.getString(cursor.getColumnIndex(C_BRAND_NAME)),
                        cursor.getString(cursor.getColumnIndex(C_QUANTITY))};
            }

            return itemInfo;
        }
    }

    // Delete item info for the current item.
    boolean deleteItemInfo(int itemId, int shopId, String location) {
        try {
            return db.delete(ITEM_INFO_TABLE, C_ITEM_ID + "=? and " + C_SHOP_ID + "=?" + " and " +
                    C_LOCATION + "=?", new String[]{Integer.toString(itemId), Integer.toString(shopId),
                    location}) > 0;
        }
        catch (Exception e) {
            Log.d("deleteItemInfo","Exception:" + e.toString());
            return false;
        }
    }

    // Set the barcode to the current item.
    boolean updateItemBarcode(int itemId, String barcode) {

        try {
            ContentValues data = new ContentValues();
            data.put(C_BARCODE, barcode);

            return db.update(ITEMS_TABLE, data, C_ITEM_ID + "=" + itemId, null) > 0;

        } catch (Exception e) {
            Log.d("updateItemBarcode", "Exception:" + e.toString());
            return false;
        }
    }

    // Get the shop info to create the header column fields for the FragmentCompareList's table.
    ArrayList<String> getShopsForTable(String itemIds, String location) {

        ArrayList<String> shops = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("select distinct(" + C_SHOP_NAME + ") " +
                                                "from " + ITEM_INFO_TABLE + " as iit inner join " +
                                                        SHOP_TABLE + " as st " +
                                                        "on iit." + C_SHOP_ID + "=st." + C_SHOP_ID + " " +
                                                "where " + C_ITEM_ID + " in (" + itemIds + ") " +
                                                "and iit." + C_LOCATION + "='" + location + "' " +
                                                "order by 1", null)) {

            if (cursor.moveToFirst()) {
                do {
                    shops.add(cursor.getString(cursor.getColumnIndex(C_SHOP_NAME)));
                } while (cursor.moveToNext());
            }

            return shops;
        }
    }

    // Get the item info data for the current list to populate the FragmentCompareList's table.
    List<Object[]> getItemInfoForTable(String itemIds, String location) {

        List<Object[]> items = new ArrayList<>();

        try (Cursor cursor = db.rawQuery("select " + ITEMS_TABLE + "." + C_ITEM_ID + " as " + C_ITEM_ID + "," +
                                                         C_ITEM_NAME + "," + C_QUANTITY_UNIT + "," +
                                                        "ifnull(" + C_SHOP_NAME + ",'') as " + C_SHOP_NAME + "," +
                                                        "ifnull(" + C_BRAND_NAME + ",'') as " + C_BRAND_NAME + "," +
                                                        "ifnull(data." + C_PRICE + ",0) as " + C_PRICE +
                                            " from " + ITEMS_TABLE + " as " + ITEMS_TABLE +
                                                    " left outer join " + "" +
                                                    "(select " + C_ITEM_ID + "," +
                                                                C_SHOP_NAME + "," +
                                                                C_BRAND_NAME + "," +
                                                                C_PRICE +
                                                    " from " + ITEM_INFO_TABLE + " as info " +
                                                            "inner join " + SHOP_TABLE + " as shop " +
                                                            "on info." + C_SHOP_ID + "=shop." + C_SHOP_ID +
                                                            " left outer join " + BRAND_TABLE + " as brand " +
                                                            "on info." + C_BRAND_ID + "=brand." + C_BRAND_ID +
                                                    " where info." + C_ITEM_ID + " in (" + itemIds + ")" +
                                                    " and " + C_LOCATION + "='" + location + "') as data " +
                                                    "on items." + C_ITEM_ID + "=data." + C_ITEM_ID + " " +
                                            "where items." + C_ITEM_ID + " in (" + itemIds + ")" + " " +
                                            "order by " + C_ITEM_NAME + "," + C_SHOP_NAME, null)) {

            Log.d("getItemInfoForTable", "Cursor size:" + cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    Log.d("getItemInfoForTable", "Item:" + cursor.getString(cursor.getColumnIndex(C_ITEM_NAME)));
                    Object[] item = new Object[]{
                            cursor.getInt(cursor.getColumnIndex(C_ITEM_ID)),
                            cursor.getString(cursor.getColumnIndex(C_ITEM_NAME)),
                            cursor.getString(cursor.getColumnIndex(C_QUANTITY_UNIT)),
                            cursor.getString(cursor.getColumnIndex(C_SHOP_NAME)),
                            cursor.getString(cursor.getColumnIndex(C_BRAND_NAME)),
                            cursor.getFloat(cursor.getColumnIndex(C_PRICE))
                    };
                    items.add(item);

                } while (cursor.moveToNext());
            }

            return items;

        } catch (Exception e){
            Log.d("getItemInfoForTable","Exception:" + e.toString());
            return null;
        }
    }

    /*
     * SHOPS table methods
     */

    // Insert shop into database.
    // Returns -1 if error occurred, otherwise returns rowid of new record.
    long insertShop(String shop_name) {
        ContentValues values = new ContentValues();
        values.put(C_SHOP_NAME, shop_name);
        return db.insert(SHOP_TABLE, null, values);
    }

    // Delete shop from database
    int deleteShop(String shop) {
        int number = 0;
        try {
            // Returns number of rows deleted.
            number = db.delete(SHOP_TABLE, C_SHOP_NAME + "='" + shop + "'", null);
            return number;
        }
        catch (SQLException e) {
            return number;
        }
    }

    // Get all shops
    ArrayList<String> getAllShops() {

        ArrayList<String> shops = new ArrayList<>();

        // Add a blank record to the beginning of the list.
        shops.add("");

        try (Cursor cursor = db.rawQuery("select " + C_SHOP_NAME + " from " + SHOP_TABLE, null)) {

            // looping through all rows and adding to categories array list
            if (cursor.moveToFirst()) {
                do {
                    shops.add(cursor.getString(cursor.getColumnIndex(DBAdapter.C_SHOP_NAME)));
                } while (cursor.moveToNext());
            }

            return shops;
        }
    }

    // Get the shop names linked to a given set of item ids.
    ArrayList<String> getShopsNamesForItems(String itemIds, String location) {

        ArrayList<String> shops = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("select distinct(" + C_SHOP_NAME + ") " +
                                            "from " + ITEM_INFO_TABLE + " as iit inner join " +
                                                    SHOP_TABLE + " as st" +
                                                    " on iit." + C_SHOP_ID + "=st." + C_SHOP_ID +
                                            " where " + C_ITEM_ID + " in (" + itemIds + ")" +
                                            " and iit." + C_LOCATION + "='" + location + "'" +
                                            " order by 1", null)) {

            if (cursor.moveToFirst()) {
                do {
                    shops.add(cursor.getString(cursor.getColumnIndex(C_SHOP_NAME)));
                } while (cursor.moveToNext());
            }

            return shops;
        }
    }

    // Get the shop's id using shop name.
    int getShopId(String shopName) {

        int shopId = 0;
        try (Cursor cursor = db.rawQuery("select " + C_SHOP_ID + " from " + SHOP_TABLE +
                " where " + C_SHOP_NAME + "=" + "'" + shopName + "'", null)) {

            if (cursor.moveToFirst()) {
                shopId = cursor.getInt(cursor.getColumnIndex(C_SHOP_ID));
            }

            return shopId;
        }
    }

    /*
     * BRANDS table methods
     */

    // Insert brand into database.
    // Returns -1 if error occurred, otherwise returns rowid of new record.
    long insertBrand(String brand_name) {
        ContentValues values = new ContentValues();
        values.put(C_BRAND_NAME, brand_name);
        return db.insert(BRAND_TABLE, null, values);
    }

    // Delete brand from database
    int deleteBrand(String brand) {
        int number = 0;
        try {
            // Returns number of rows deleted.
            number = db.delete(BRAND_TABLE, C_BRAND_NAME + "='" + brand + "'", null);
            return number;
        }
        catch (SQLException e) {
            return number;
        }
    }

    // Get all brands
    ArrayList<String> getAllBrands() {

        ArrayList<String> brands = new ArrayList<>();

        // Add a blank record to the beginning of the list.
        brands.add("");

        try (Cursor cursor = db.rawQuery("select " + C_BRAND_NAME + " from " + BRAND_TABLE +
                " order by " + C_BRAND_NAME, null)) {

            // looping through all rows and adding to categories array list
            if (cursor.moveToFirst()) {
                do {
                    brands.add(cursor.getString(cursor.getColumnIndex(DBAdapter.C_BRAND_NAME)));
                } while (cursor.moveToNext());
            }

            return brands;
        }
    }

    // Get the brand's id using brand name.
    int getBrandId(String brandName) {

        int brandId = 0;
        try (Cursor cursor = db.rawQuery("select " + C_BRAND_ID + " from " + BRAND_TABLE +
                    " where " + C_BRAND_NAME + "=" + "'" + brandName + "'", null)) {

            if (cursor.moveToFirst()) {
                brandId = cursor.getInt(cursor.getColumnIndex(C_BRAND_ID));
            }

            return brandId;
        }
    }

    // Get the brand name using brand id.
    private String getBrandName(int brandId) {

        String brandName = null;
        try (Cursor cursor = db.rawQuery("select " + C_BRAND_NAME + " from " + BRAND_TABLE + " where " +
                    C_BRAND_ID + "=" + brandId, null)) {

            if (cursor.moveToFirst()) {
                brandName = cursor.getString(cursor.getColumnIndex(C_BRAND_NAME));
            }

            return brandName;
        }
    }

    /*
     * LISTS table methods
     */

    // Check if a given list name already exists.
    private Boolean checkListExists(String listName) {

        boolean exists = false;
        try (Cursor cursor = db.rawQuery("select " + C_LIST_NAME + " from " + LISTS_TABLE +
                    " where " + C_LIST_NAME + "='" + listName + "'", null)) {

            if (cursor.getCount() > 0) {
                exists = true;
            }

            return exists;
        }
    }

    // Get the list id for a given list name.
    int getListId(String listName) {

        try (Cursor cursor = db.rawQuery("select " + C_LIST_ID + " from " + LISTS_TABLE +
                    " where " + C_LIST_NAME + "='" + listName + "'", null)) {

            if (cursor.getCount() != 1) {
                return 0;
            }

            cursor.moveToFirst();
            return cursor.getInt(cursor.getPosition());
        }
    }

    // Get the list of items for a given list id.
    ArrayList<Item> loadList(int listId) {

        ArrayList<Item> list = new ArrayList<>();
        boolean checked;
        String sql = "select i." + C_ITEM_ID + " as item_id, " +
                                "l." + C_ITEM_NAME + " as item_name, " +
                                "c." + C_CAT_ID  + " as cat_id, " +
                                "l." + C_CATEGORY_NAME + " as category_name, " +
                                "ifnull(l." + C_QUANTITY + ",'') as qty_weight, " +
                                "l." + C_ITEM_CHECKED + " as item_checked " +
                    "from " + LIST_ITEMS_TABLE + " as l inner join " +
                            CATEGORY_TABLE + " as c ON l." + C_CATEGORY_NAME + "=c." + C_CATEGORY_NAME +
                            " inner join " + ITEMS_TABLE + " as i " +
                            "ON l." + C_ITEM_NAME + "=i." + C_ITEM_NAME +
                    " where l." + C_LIST_ID + "=" + listId;

        try (Cursor cursor = db.rawQuery(sql, null)) {

            if (cursor.moveToFirst()) {
                int itemPosition = 0;
                do {
                    if (cursor.getInt(cursor.getColumnIndex("item_checked")) == 0) {
                        checked = false;
                    }
                    else {
                        checked = true;
                    }

                    list.add(new Item(cursor.getInt(cursor.getColumnIndex("item_id")),
                            cursor.getString(cursor.getColumnIndex("item_name")),
                            cursor.getInt(cursor.getColumnIndex("cat_id")),
                            cursor.getString(cursor.getColumnIndex("category_name")),
                            cursor.getString(cursor.getColumnIndex("qty_weight")),
                            false,
                            checked,
                            false,
                            itemPosition));

                    itemPosition = itemPosition + 1;
                } while (cursor.moveToNext());
            }
        }

        return list;
    }

    // Save the current list of items.
    Boolean saveList(String listName, ArrayList<Item> items) {

        int checked;

        if (items.size() > 0) {
            // If the list already exists then delete it
            if (checkListExists(listName)) {
                deleteList(listName);
            }

            ContentValues listValues = new ContentValues();
            listValues.put(C_LIST_NAME, listName);

            try {
                db.beginTransaction();
                if (db.insert(LISTS_TABLE, null, listValues) > 0) {
                    // Get the id of the list that has just been inserted.
                    int listId = getListId(listName);

                    if (listId > 0) {

                        // Loop through the global items list and insert each one into the database.
                        for (Item i : items) {
                            Log.d("db.saveList", "Item:" + i.getItemName());
                            // SQLite does not store boolean values, need to convert.
                            if (i.getItemChecked()) {
                                checked = 1; // true
                            }
                            else {
                                checked = 0; // false
                            }

                            ContentValues itemValues = new ContentValues();
                            itemValues.put(C_LIST_ID, listId);
                            itemValues.put(C_ITEM_ID, i.getItemId());
                            itemValues.put(C_ITEM_NAME, i.getItemName());
                            itemValues.put(C_CATEGORY_NAME, i.getCatName());
                            itemValues.put(C_QUANTITY, i.getQuantity());
                            itemValues.put(C_ITEM_CHECKED, checked);

                            db.insert(LIST_ITEMS_TABLE, null, itemValues);
                        }
                        db.setTransactionSuccessful();
                    }
                    else {
                        throw new SQLException();
                    }
                }
            } catch (SQLException e) {
                Toast.makeText(this.context, "List not saved!", Toast.LENGTH_SHORT).show();
                return false;
            } finally {
                db.endTransaction();
            }

            return true;
        }
        else {
            return false;
        }
    }

    // Delete the list by list name.
    void deleteList(String listName) {

        // Delete list items from the list items table
        int listId = getListId(listName);
        Log.d("deleteList","listId:" + listId);
        if (listId > 0) {
            try {
                db.beginTransaction();
                int rowsDeleted = db.delete(LIST_ITEMS_TABLE, C_LIST_ID + "=" + listId, null);

                Log.d("deleteList","list items deleted:" + rowsDeleted);

                if (rowsDeleted > 0) {
                    // Delete the list record from the lists table.
                    rowsDeleted = db.delete(LISTS_TABLE, C_LIST_ID + "=" + listId, null);
                    if (rowsDeleted > 0) {
                        Log.d("deleteList","list deleted:" + rowsDeleted);
                        db.setTransactionSuccessful();
                    }
                }
            } catch (SQLException e) {
                Toast.makeText(this.context, "List not deleted!", Toast.LENGTH_SHORT).show();
            } finally {
                db.endTransaction();
            }
        }
        else {
            Toast.makeText(this.context, "List does not exist!", Toast.LENGTH_SHORT).show();
        }
    }

    // Get all the lists from the database table LISTS
    ArrayList<String> getListNames() {

        ArrayList<String> lists = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("select " + C_LIST_NAME + " from " + LISTS_TABLE, null)) {

            if (cursor.moveToFirst()) {
                do {
                    lists.add(cursor.getString(cursor.getColumnIndex(C_LIST_NAME)));
                } while (cursor.moveToNext());
            }
            return lists;
        }
        catch (SQLException e) {
            return lists;
        }
    }

    /*
     * Locale table methods
     */

    void setCurrency(String currency) {
        try {
            String sql = "update " + LOCALE_TABLE + " set " + C_LOCATION + "='" + currency + "'";
            db.execSQL(sql);
        }
        catch (Exception e) {
            Log.d("setLocale","Exception:" + e.toString());
        }
    }

    String getCurrency() {

        String currency = null;
        try (Cursor cursor = db.rawQuery("select " + C_LOCATION + " from " + LOCALE_TABLE,
                null)) {

            if (cursor.moveToFirst()) {
                currency = cursor.getString(0);
            }

            return currency;
        }
    }

    /*
     * Retrieve data for the passed table. Used for returning data to build backup XML file.
     */
    List<Object[]> getTableData(String tableName) {

        try (Cursor cursor = db.rawQuery("select * from " + tableName, null)){

            // Define the list of record objects.
            List<Object[]> records = new ArrayList<>();

            while (cursor.moveToNext()) {

                // Create record object to store the cursor's current row.
                Object[] record = new Object[cursor.getColumnCount()];

                for (int i=0; i<cursor.getColumnCount(); i++) {

                    // Determine the data type of the current column and add the value to the record object.
                    switch (cursor.getType(i)) {
                        case Cursor.FIELD_TYPE_INTEGER:
                            record[i] = cursor.getInt(i);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            // Replace any escape characters from the string.
                            String s = cursor.getString(i).replace("&", "&amp");
                            record[i] = s;
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            record[i] = cursor.getFloat(i);
                            break;
                    }
                }
                // Add the record to the record list.
                records.add(record);
            }

            return records;

        } catch (Exception e) {
            Log.d("getTableData","Exception:" + e.toString());
            return null;
        }
    }

    /*
     * Retrieve column names for a given table.
     */
    LinkedHashMap<String, String> getTableColumns(String table) {

        try (Cursor cursor = db.rawQuery("Pragma table_info(" + table + ")", null)) {

            LinkedHashMap<String, String> columns = new LinkedHashMap<>(); // <column_name, column_type>

            if (cursor.moveToFirst()) {

                do {
                    Log.d("getTableColumns","column:" + cursor.getString(cursor.getColumnIndex("name")));
                    columns.put(cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getString(cursor.getColumnIndex("type")));
                } while (cursor.moveToNext());
            }

            return columns;

        } catch (Exception e) {
            Log.d("getTableColumns","Exception:" + e.toString());
            return null;
        }
    }

}
